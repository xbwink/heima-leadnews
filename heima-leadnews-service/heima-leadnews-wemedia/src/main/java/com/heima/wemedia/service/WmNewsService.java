package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.dtos.WmNewsSuPublishDto;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-28 14:11
 * @vesion 1.0
 */
public interface WmNewsService {

    /**
     * 获取文章内容列表
     * @param dto
     * @return
     */
    ResponseResult getNewsList(WmNewsPageReqDto dto);

    /**
     * 发布文章
     * @param dto
     * @return
     */
    ResponseResult submitNews(WmNewsSuPublishDto dto);

    /**
     * 根据文章id查询文章详细信息
     * @param id
     * @return
     */
    ResponseResult selectInfoById(Integer id);

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    ResponseResult downOrUp(WmNewsDto dto);
}
