package com.heima.article.service;

import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-18 14:45
 * @vesion 1.0
 */
public interface ApArticleService {

    /**
     * 根据参数加载文章列表
     * @param loadtype 1为加载更多  2为加载最新
     * @param dto
     * @return
     */
    ResponseResult load(Short loadtype, ArticleHomeDto dto);

    /**
     * 新增或修改文章
     * @param dto
     * @return
     */
    ResponseResult saveArticle(ArticleDto dto);
}
