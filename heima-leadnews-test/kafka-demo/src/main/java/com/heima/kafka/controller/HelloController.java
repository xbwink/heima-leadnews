package com.heima.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xb
 * @description TODO
 * @create 2024-05-27 15:38
 * @vesion 1.0
 */
@RestController
public class HelloController {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @GetMapping("/hello")
    public String hello(){
        User user = new User();
        user.setUsername("xiaowang");
        user.setAge(18);

        kafkaTemplate.send("user-topic", JSON.toJSONString(user));
        return "ok";
    }

}
