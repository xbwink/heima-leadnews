package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author xb
 * @description TODO
 * @create 2024-05-22 15:27
 * @vesion 1.0
 */
public interface TaskService {

    /**
     * 添加任务
     * @param task 任务对象
     * @return 任务id
     */
    public long addTask(Task task);

    /**
     * 取消任务
     * @param taskId 任务id
     */
    public boolean cancelTask(Long taskId);

    /**
     * 拉取任务
     * @param taskType 类型
     * @param taskPriority 优先级
     * @return
     */
    public Task pullTask(Integer taskType,Integer taskPriority);

    /**
     * 未来数据定时刷新 (每分钟执行一次)
     */
    public void refresh();

}
