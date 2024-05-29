package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-25 14:49
 * @vesion 1.0
 */
public interface WmMaterialService extends IService<WmMaterial> {

    /**
     * 上传图片
     * @param multipartFile
     * @return
     */
    ResponseResult imageUpload(MultipartFile multipartFile);

    /**
     * 查询素材列表
     * @param wmMaterialDto
     * @return
     */
    ResponseResult selectList(WmMaterialDto wmMaterialDto);
}
