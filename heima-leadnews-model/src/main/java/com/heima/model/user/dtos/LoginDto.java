package com.heima.model.user.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xb
 * @description app端用户登录参数
 * @create 2024-04-17 15:21
 * @vesion 1.0
 */
@Data
public class LoginDto {

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号",required = true)
    private String phone;
    /**
     * 密码
     */
    @ApiModelProperty(value = "密码",required = true)
    private String password;

}
