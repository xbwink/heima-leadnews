package com.heima.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.pojo.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author xb
 * @description TODO
 * @create 2024-05-28 10:59
 * @vesion 1.0
 */
@Component
public class HelloListener {

    @KafkaListener(topics = "user-topic")
    public void hello(String message){
        User user = JSON.parseObject(message, User.class);
        System.out.println(user);
    }

}
