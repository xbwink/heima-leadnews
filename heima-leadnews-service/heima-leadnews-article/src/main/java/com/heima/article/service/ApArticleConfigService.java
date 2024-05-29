package com.heima.article.service;

import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;

import java.util.Map;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-18 14:45
 * @vesion 1.0
 */
public interface ApArticleConfigService {


    /**
     * 文章上下架
     * @param map
     */
    void articleEnable(Map map);
}
