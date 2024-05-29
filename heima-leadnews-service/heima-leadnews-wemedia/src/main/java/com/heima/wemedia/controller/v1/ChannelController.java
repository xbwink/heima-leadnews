package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-25 14:45
 * @vesion 1.0
 */

@Api(tags = "自媒体端频道控制器")
@Slf4j
@RestController
@RequestMapping("/api/v1/channel")
public class ChannelController {

    @Autowired
    private WmChannelService wmChannelService;

    @ApiOperation("图片上传")
    @GetMapping("/channels")
    public ResponseResult channelList(){

        return wmChannelService.getChannelList();
    }


}
