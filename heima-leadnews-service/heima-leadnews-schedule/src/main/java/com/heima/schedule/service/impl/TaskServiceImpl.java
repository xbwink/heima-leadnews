package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author xb
 * @description TODO
 * @create 2024-05-22 15:28
 * @vesion 1.0
 */
@Slf4j
@Transactional
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 添加任务
     *
     * @param task 任务对象
     * @return 任务id
     */
    @Override
    public long addTask(Task task) {
        // 1.添加任务到数据库
        if (addTaskToDb(task)) {
            // 2.添加任务到redis
            addTaskToRedis(task);
        }
        return task.getTaskId();
    }

    /**
     * 取消任务
     *
     * @param taskId 任务id
     */
    @Override
    public boolean cancelTask(Long taskId) {
        // 1.根据taskId删除任务，更新任务日志
        Task task = uodateDb(taskId,2);

        if(task != null){
            // 2.删除redis中对应任务
            removeTaskFromRedis(task);
            return true;
        }
        return false;
    }

    /**
     * 拉取任务
     *
     * @param taskType     类型
     * @param taskPriority 优先级
     * @return
     */
    @Override
    public Task pullTask(Integer taskType, Integer taskPriority) {
        String key = taskType + "_" + taskPriority;
        Task task = null;
        try {
            String task_json = cacheService.lRightPop("topic_" +key);
            if(StringUtils.isNotBlank(task_json)){
                task = JSON.parseObject(task_json, Task.class);
                // 更新数据库信息
                uodateDb(task.getTaskId(),1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("task pull exception");
        }
        return task;
    }

    /**
     * 未来数据定时刷新 (每分钟执行一次)
     */
    @Scheduled(cron = "0 */1 * * * ?")
    @Override
    public void refresh() {
        // 加锁
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if(StringUtils.isNotBlank(token)){
            log.info("未来数据定时刷新---定时任务");

            // 获取未来数据的keys
            Set<String> futureKeys = cacheService.scan("future_*");
            for (String futureKey : futureKeys) {

                String topicKey = "topic_" + futureKey.split("future_")[1];
                // 获取当前需要消费的任务数据
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                if(!tasks.isEmpty()){
                    // 将任务添加到消费者队列中
                    cacheService.refreshWithPipeline(futureKey,topicKey,tasks);
                    log.info("成功的将" + futureKey + "下的当前需要执行的任务数据刷新到" + topicKey + "下");
                }
            }

        }

    }

    /**
     * 数据库任务同步到redis
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct // 微服务启动时执行一次
    public void reloadData() {
        // 1.清除redis中的缓存
        Set<String> topicKeys = cacheService.scan("topic_*");
        Set<String> futureKeys = cacheService.scan("future_*");
        cacheService.delete(topicKeys);
        cacheService.delete(futureKeys);

        // 2.查询数据库中执行时间小于未来5分钟的数据
        LambdaQueryWrapper<Taskinfo> wrapper = new LambdaQueryWrapper<>();
        //获取5分钟之后的时间毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        List<Taskinfo> list = taskinfoMapper.selectList(wrapper.lt(Taskinfo::getExecuteTime, calendar.getTime()));

        // 3.新增任务到redis
        for (Taskinfo taskinfo : list) {
            Task task = new Task();
            BeanUtils.copyProperties(taskinfo,task);
            task.setExecuteTime(taskinfo.getExecuteTime().getTime());
            addTaskToRedis(task);
        }
        log.info("数据库数据同步到缓存");
    }

    /**
     * 删除redis中对应任务
     * @param task
     */
    private void removeTaskFromRedis(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove("topic_"+key,0,JSON.toJSONString(task));
        } else {
            cacheService.zRemove("future_"+key,JSON.toJSONString(task));
        }
    }

    /**
     * 根据taskId删除任务，更新任务日志
     * @param taskId
     * @return
     */
    private Task uodateDb(Long taskId,Integer status) {
        Task task = null;
        try {
            taskinfoMapper.deleteById(taskId);

            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status); // 设置状态为取消
            taskinfoLogsMapper.updateById(taskinfoLogs);
            task = new Task();

            BeanUtils.copyProperties(taskinfoLogs,task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (Exception e) {
            log.error("task cancel exception taskid={}",taskId);
        }
        return task;
    }

    /**
     * 添加任务到redis
     *
     * @param task
     */
    private void addTaskToRedis(Task task) {
        // redis的key为类型+优先级
        String key = task.getTaskType() + "_" + task.getPriority();
        //获取5分钟之后的时间毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis();
        // 如果当前任务的执行时间<=当前时间存入list
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lLeftPush("topic_" + key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() <= nextScheduleTime) {
            // 如果任务的执行时间>=当前时间 && 小于等于未来5分钟存入zset
            cacheService.zAdd("future_" + key, JSON.toJSONString(task), task.getExecuteTime());
        }
    }

    /**
     * 添加任务到数据库
     *
     * @param task
     */
    private boolean addTaskToDb(Task task) {
        try {
            // 保存任务表
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            // 设置taskid
            task.setTaskId(taskinfo.getTaskId());

            // 保存任务日志表
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            taskinfoLogs.setVersion(1); //乐观锁
            taskinfoLogs.setStatus(0); //默认状态
            taskinfoLogsMapper.insert(taskinfoLogs);
            return true;
        } catch (BeansException e) {
            e.printStackTrace();
        }
        return false;
    }

}
