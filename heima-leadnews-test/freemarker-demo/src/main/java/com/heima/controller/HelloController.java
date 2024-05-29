package com.heima.controller;

import com.heima.entity.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-19 9:24
 * @vesion 1.0
 */
@Controller
public class HelloController {

    @GetMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","hutao");
        Student student = new Student();
        student.setName("心海");
        student.setAge(21);
        model.addAttribute("stu",student);
        return "01-basic";
    }

}
