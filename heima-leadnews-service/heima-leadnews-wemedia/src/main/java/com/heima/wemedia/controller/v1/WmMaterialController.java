package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.wemedia.service.WmMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-25 14:45
 * @vesion 1.0
 */

@Api(tags = "自媒体端文章素材控制器")
@Slf4j
@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    @ApiOperation("图片上传")
    @PostMapping("/upload_picture")
    public ResponseResult imageUpload(MultipartFile multipartFile){

        return wmMaterialService.imageUpload(multipartFile);
    }

    @ApiOperation("素材列表查询")
    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmMaterialDto wmMaterialDto){
        return wmMaterialService.selectList(wmMaterialDto);
    }

}
