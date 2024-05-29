package com.heima.schedule.service.impl;

import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author xb
 * @description TODO
 * @create 2024-05-22 16:04
 * @vesion 1.0
 */
@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class TaskServiceImplTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void addTask() {
        for (int i = 1; i <= 5; i++) {
            Task task = new Task();
            task.setTaskType(100+i);
            task.setPriority(500);
            task.setParameters("qwq ovo eva".getBytes());
            task.setExecuteTime(new Date().getTime() + 5000 * i);
            long l = taskService.addTask(task);
            System.out.println(l);
        }

    }

    @Test
    public void cancelTask(){
        taskService.cancelTask(1793463681934802946l);
    }

    @Test
    public void pullTask(){
        taskService.pullTask(100,50);
    }

}
