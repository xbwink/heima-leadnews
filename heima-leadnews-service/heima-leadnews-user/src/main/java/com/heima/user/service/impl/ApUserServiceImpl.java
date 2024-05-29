package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-17 15:24
 * @vesion 1.0
 */
@Slf4j
@Service
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    /**
     * app端登录
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        // 用户登录
        if(StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())){
            // 1.根据手机号查询用户
            LambdaQueryWrapper<ApUser> wrapper = new LambdaQueryWrapper<>();
            ApUser dbUser = getOne(wrapper.eq(ApUser::getPhone, dto.getPhone()));
            if(dbUser == null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户不存在");
            }
            // 2.比对密码 密码+盐
            String password = dto.getPassword();
            String salt = dbUser.getSalt();
            String pwd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if(!pwd.equals(dbUser.getPassword())){
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            // 3.返回数据 jwt
            HashMap<String, Object> map = new HashMap<>();
            String token = AppJwtUtil.getToken(dbUser.getId().longValue());
            map.put("token",token);
            dbUser.setSalt("");
            dbUser.setPassword("");
            map.put("user",dbUser);
            return ResponseResult.okResult(map);
        }

        // 游客登录 同样返回token  id = 0
        HashMap<String, Object> map = new HashMap<>();
        map.put("token",AppJwtUtil.getToken(0L));
        return ResponseResult.okResult(map);
    }

}
