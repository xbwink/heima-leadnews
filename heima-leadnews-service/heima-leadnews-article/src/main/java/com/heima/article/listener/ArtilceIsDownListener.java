package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.WmNewsMessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xb
 * @description TODO
 * @create 2024-05-29 14:36
 * @vesion 1.0
 */
@Slf4j
@Component
public class ArtilceIsDownListener {

    @Autowired
    private ApArticleConfigService apArticleConfigService;


    /**
     * 监听消息修改文章配置
     * @param message
     */
    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void articleIsDown(String message){
       if(StringUtils.isNotBlank(message)){
           Map map = JSON.parseObject(message, Map.class);
           apArticleConfigService.articleEnable(map);
           log.info("article端文章配置修改，articleId={}",map.get("articleId"));
       }
    }

}
