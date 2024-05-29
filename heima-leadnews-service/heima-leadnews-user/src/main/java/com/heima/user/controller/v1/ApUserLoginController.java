package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xb
 * @description app端用户登录控制器
 * @create 2024-04-17 15:18
 * @vesion 1.0
 */


@RestController
@RequestMapping("/api/v1/login")
@Api(tags = "app端用户登录")
public class ApUserLoginController {

    @Autowired
    private ApUserService apUserService;

    @ApiOperation("用户登录")
    @PostMapping("/login_auth")
    public ResponseResult login(@RequestBody LoginDto loginDto){
        return apUserService.login(loginDto);
    }

}
