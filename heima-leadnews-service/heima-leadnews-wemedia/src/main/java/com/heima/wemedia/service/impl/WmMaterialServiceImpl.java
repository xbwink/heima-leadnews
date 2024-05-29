package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.WmUserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-25 14:50
 * @vesion 1.0
 */
@Slf4j
@Service
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;


    /**
     * 上传图片
     *
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult imageUpload(MultipartFile multipartFile) {
        // 1、检查参数
        if(multipartFile == null || multipartFile.getSize() == 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2、上传图片到minio中
        // 文件名使用：uuid＋原文件后缀名
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = uuid + suffix;
        String filePath = null;
        try {
             filePath = fileStorageService.uploadImgFile("", fileName, multipartFile.getInputStream());
            log.info("上传文件到minio成功，filePath:{}",filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 3、保存到数据库
        WmMaterial wmMaterial = new WmMaterial();
        // 从ThreadLocal取出userId
        WmUser user = WmThreadLocalUtil.getUser();
        wmMaterial.setUserId(user.getId());
        wmMaterial.setUrl(filePath);
        wmMaterial.setType(Short.valueOf("0"));
        wmMaterial.setIsCollection(Short.valueOf("0"));
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);

        // 4、返回结果
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 查询素材列表
     *
     * @param wmMaterialDto
     * @return
     */
    @Override
    public ResponseResult selectList(WmMaterialDto wmMaterialDto) {
        //1. 检查参数
        wmMaterialDto.checkParam();

        //2.分页查询
        LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
        Short isCollection = wmMaterialDto.getIsCollection();
        // 是否收藏
        wrapper.eq(isCollection != null,WmMaterial::getIsCollection,isCollection);
        // 按照用户查询
        WmUser user = WmThreadLocalUtil.getUser();
        wrapper.eq(user.getId()!= null,WmMaterial::getUserId,user.getId());
        // 按照时间倒序查询
        wrapper.orderByDesc(WmMaterial::getCreatedTime);

        Page<WmMaterial> materialPage = new Page<>(wmMaterialDto.getPage(), wmMaterialDto.getSize());
        Page<WmMaterial> materialIPage = page(materialPage,wrapper);

        // 构建返回结果
        ResponseResult responseResult = new PageResponseResult
                (wmMaterialDto.getPage(),wmMaterialDto.getSize(),(int)materialIPage.getTotal());
        responseResult.setData(materialIPage.getRecords());
        return responseResult;
    }

}
