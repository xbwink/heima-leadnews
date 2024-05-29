package com.heima.article;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.JsonArray;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-19 15:13
 * @vesion 1.0
 */

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleFreemarkerTest {

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ApArticleMapper apArticleMapper;

    @Test
    public void createStaticUrlTest() throws Exception {
        // 1.获取文章内容
        LambdaQueryWrapper<ApArticleContent> wrapper = new LambdaQueryWrapper<>();
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(wrapper.eq(ApArticleContent::getArticleId, "1383827787629252610"));

        // 2.通过freemarker生产html文件
        Template template = configuration.getTemplate("article.ftl");
        Map<String, Object> map = new HashMap<>();
        map.put("content", JSONArray.parseArray(apArticleContent.getContent()));
        template.process(map,new FileWriter("d:/test.html"));

        // 3.将html文件上传至minio中
        FileInputStream fileInputStream = new FileInputStream("d:/test.html");
        String path = fileStorageService.uploadHtmlFile("", "test.html", fileInputStream);
        // 4.修改ap_article表static_url字段
        ApArticle apArticle = apArticleMapper.selectById(apArticleContent.getArticleId());
        apArticle.setStaticUrl(path);
        apArticleMapper.updateById(apArticle);

    }

}
