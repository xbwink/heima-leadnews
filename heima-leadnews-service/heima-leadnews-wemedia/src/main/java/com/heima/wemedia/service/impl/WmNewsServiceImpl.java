package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.dtos.WmNewsSuPublishDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.heima.model.common.enums.AppHttpCodeEnum.PARAM_REQUIREPARAM_REQUIRE;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-28 14:12
 * @vesion 1.0
 */
@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Autowired
    private WmNewsTaskService wmNewsTaskService;
    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Override
    public ResponseResult getNewsList(WmNewsPageReqDto dto) {
        // 1.构建查询条件
        dto.checkParam();
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        // 根据用户查询
        Integer userId = WmThreadLocalUtil.getUser().getId();
        wrapper.eq(userId!=null,WmNews::getUserId,userId);
        // 文章状态
        wrapper.eq(dto.getStatus() != null,WmNews::getStatus,dto.getStatus());
        // 关键字模糊查询
        wrapper.like(StringUtils.isNotBlank(dto.getKeyword()),WmNews::getTitle,dto.getKeyword());
        // 频道列表
        wrapper.eq(dto.getChannelId()!=null,WmNews::getChannelId,dto.getChannelId());
        // 发布日期
        if(dto.getBeginPubDate()!=null && dto.getEndPubDate() != null){
            wrapper.between(WmNews::getPublishTime,dto.getBeginPubDate(),dto.getEndPubDate());
        }
        // 发布时间倒序
        wrapper.orderByDesc(WmNews::getPublishTime);

        // 2.分页查询
        Page<WmNews> wmNewsPage = new Page<>(dto.getPage(), dto.getSize());
        Page<WmNews> page = page(wmNewsPage,wrapper);

        // 3.构建返回结果
        ResponseResult pageResponseResult = new PageResponseResult
                ((int)page.getCurrent(),(int)page.getSize(),(int)page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }

    /**
     * 发布文章
     *
     * @param dto
     * @return
     */
//    @Transactional
    @Override
    public ResponseResult submitNews(WmNewsSuPublishDto dto) {
        // 拷贝属性
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto,wmNews);

        // 是否存在id
        if(dto.getId() == null){
            // 新增文章

            // 1.根据封面类型处理图片
            handleImage(dto, wmNews);
            wmNews.setCreatedTime(new Date());
            // 设置当前登录用户id
            wmNews.setUserId(WmThreadLocalUtil.getUser().getId());

            if(dto.getStatus() == 1){} wmNews.setSubmitedTime(new Date());
            // 插入后找传入的参数，就能找到新增加元素的id
            // 2.执行添加操作
            this.save(wmNews);

            // 3.如果当前为提交则还需绑定对应素材关系
            bindImageRelation(dto, wmNews);

        } else {
            // 修改文章
            // 1.删除关联已关联的素材关系
            LambdaQueryWrapper<WmNewsMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WmNewsMaterial::getNewsId,wmNews.getId());
            wmNewsMaterialMapper.delete(wrapper);

            // 2.重新处理图片并绑定关系
            handleImage(dto,wmNews);
            bindImageRelation(dto,wmNews);

            // 3.执行修改操作
            if(dto.getStatus() == 1) wmNews.setSubmitedTime(new Date());
            this.updateById(wmNews);
        }

        // 4.文章审核
//        wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        if(wmNews.getStatus() != 0){
            wmNewsTaskService.addNewsToTask(wmNews.getId(),wmNews.getPublishTime());
        }

        return ResponseResult.okResult(wmNews);
    }

    /**
     * 绑定图片素材与WmNews关系
     * @param dto
     * @param wmNews
     */
    private void bindImageRelation(WmNewsSuPublishDto dto, WmNews wmNews) {
        if(dto.getStatus() == 1){
            // 遍历图片路径集合绑定关系
            for (int i = 0; i < dto.getImages().size(); i++) {
                WmNewsMaterial wmNewsMaterial = new WmNewsMaterial();
                // 根据图片路径找到素材id
                LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(WmMaterial::getUrl, dto.getImages().get(i));
                WmMaterial wmMaterial = wmMaterialMapper.selectOne(wrapper);
                // 判断素材是否有效
                if(wmMaterial == null){
                    // 手动抛出异常，进行数据回滚
                    throw new CustomException(AppHttpCodeEnum.MATERIAL_FAIL);
                }
                wmNewsMaterial.setNewsId(wmNews.getId());
                wmNewsMaterial.setMaterialId(wmMaterial.getId());
                wmNewsMaterial.setOrd((short)i);
                if(dto.getType() == -1){
                    wmNewsMaterial.setType((short)0);
                }else {
                    wmNewsMaterial.setType((short)1);
                }
                wmNewsMaterialMapper.insert(wmNewsMaterial);
            }
        }
    }

    /**
     * 根据封面类型处理图片
     * @param dto
     * @param wmNews
     */
    private static void handleImage(WmNewsSuPublishDto dto, WmNews wmNews) {
        // 封面为自动
        if(dto.getType() == -1){
            // 转换为json对象取出图片地址
            JSONArray jsonArray = JSON.parseArray(dto.getContent());
            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;
                if (jsonObject.containsKey("type") && jsonObject.getString("type").equals("image")) {
                    String imageUrl = jsonObject.getString("value");
                    // 设置为单图文章
                    wmNews.setType((short) 1);
                    dto.getImages().add(imageUrl);
                }
            }
            // 默认为第一张图
            wmNews.setImages(dto.getImages().get(0));
        // 封面为三图或单图
        } else if (dto.getType() == 3 || dto.getType() == 1){
            List<String> images = dto.getImages();
            // 将图片地址用逗号分隔
            String imagesString = images.stream().filter(s -> s != null).collect(Collectors.joining(","));
            wmNews.setImages(imagesString);
        }
    }

    /**
     * 根据文章id查询文章详细信息
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult selectInfoById(Integer id) {
        WmNews wmNews = this.getById(id);
        return ResponseResult.okResult(wmNews);
    }

    /**
     * 文章上下架
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        // 1.检查id
        if(dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIREPARAM_REQUIRE,"文章id不能为空");
        }

        // 2.查询文章
        WmNews wmNews = getById(dto.getId());
        if(wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }

        //3.判断文章是否已发布
        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"当前文章不是发布状态，不能上下架");
        }

        // 4.修改文章enable
        if(dto.getEnable() != null && dto.getEnable() > -1 && dto.getEnable() < 2){
            update(Wrappers.<WmNews>lambdaUpdate().set(WmNews::getEnable,dto.getEnable())
                    .eq(WmNews::getId,wmNews.getId()));

            // 5.使用kafka发送消息通知article端修改文章配置
            if(wmNews.getArticleId() != null){
                HashMap<String,Object> map = new HashMap();
                map.put("articleId",wmNews.getArticleId());
                map.put("enable",wmNews.getEnable());
                kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC,JSON.toJSONString(map));
            }

        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }
}
