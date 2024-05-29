package com.heima.article.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xb
 * @description TODO
 * @create 2024-05-21 10:55
 * @vesion 1.0
 */
@Service
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {


    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ApArticleMapper apArticleMapper;

    /**
     * 生成静态文件上传到minIO中
     *
     * @param apArticle
     * @param content
     */
    @Async
    @Override
    public void buildArticleToMinIO(ApArticle apArticle, String content) {

        // 1.通过freemarker生产html文件
        StringWriter out = new StringWriter();
        Template template = null;
        try {
            template = configuration.getTemplate("article.ftl");
            Map<String, Object> map = new HashMap<>();
            map.put("content", JSONArray.parseArray(content));
            template.process(map,out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 2.将html文件上传至minio中
        InputStream in = new ByteArrayInputStream(out.toString().getBytes());
        String path = fileStorageService.uploadHtmlFile("", apArticle.getId() + ".html", in);
        // 3.修改ap_article表static_url字段
        apArticle.setStaticUrl(path);
        apArticleMapper.updateById(apArticle);
    }
}
