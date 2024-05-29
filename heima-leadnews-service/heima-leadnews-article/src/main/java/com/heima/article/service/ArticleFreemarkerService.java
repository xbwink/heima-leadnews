package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

/**
 * @author xb
 * @description TODO
 * @create 2024-05-21 10:53
 * @vesion 1.0
 */
public interface ArticleFreemarkerService {

    /**
     * 生成静态文件上传到minIO中
     * @param apArticle
     * @param content
     */
    void buildArticleToMinIO(ApArticle apArticle, String content);

}
