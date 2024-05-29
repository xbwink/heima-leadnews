package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.apis.article.IArticleClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xb
 * @description TODO
 * @create 2024-05-17 14:13
 * @vesion 1.0
 */

@Slf4j
@Service
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private IArticleClient articleClient;
    @Autowired
    private WmUserMapper wmUserMapper;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;
    @Autowired
    private FileStorageService fileStorageService;


    /**
     * 自媒体文章审核
     *
     * @param id 自媒体文章id
     */
    @Override
    @Async//标明当前方法是一个异步方法
    public void autoScanWmNews(Integer id) {
        // 1.查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews == null){
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }

        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            // 2.审核文本
            //从内容中提取纯文本内容和图片
            Map<String,Object> textAndImages = handleTextAndImages(wmNews);
            String content = String.valueOf(textAndImages.get("content"));
            // 审核处理敏感词
            if(!handleSensitiveScan(content,wmNews)) return;

            // 3.审核图片
            // 自动处理图片中的敏感词
            List<String> images = (List<String>) textAndImages.get("images");
            for (String image : images) {
                try {
                    byte[] bytes = fileStorageService.downLoadFile(image);
                    //从byte[]转换为butteredImage
                    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                    BufferedImage imageFile = ImageIO.read(in);
                    //创建Tesseract对象
                    ITesseract tesseract = new Tesseract();
                    //设置字体库路径
                    tesseract.setDatapath("E:\\study\\workspace\\tessdata");
                    //中文识别
                    tesseract.setLanguage("chi_sim");
                    //执行ocr识别
                    String result = tesseract.doOCR(imageFile);
                    //替换回车和tal键  使结果为一行
                    result = result.replaceAll("\\r|\\n","-").replaceAll(" ","");
                    System.out.println("识别的结果为："+result);
                    // 审核处理敏感词
                    if(!handleSensitiveScan(result,wmNews)) return;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // 4.审核成功,保存app端文章数据
            ArticleDto dto = new ArticleDto();
            // 属性拷贝
            BeanUtils.copyProperties(wmNews,dto);
            // 文章的作者
            dto.setAuthorId(Long.valueOf(wmNews.getUserId()));
            WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
            dto.setAuthorName(wmUser.getName());
            // 频道
            WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
            dto.setChannelName(wmChannel.getName());
            // 布局
            dto.setLayout(wmNews.getType());
            // 设置文章id
            if(wmNews.getArticleId() != null){
                dto.setId(wmNews.getArticleId());
            }
            // 创建时间
            dto.setCreatedTime(new Date());
            ResponseResult responseResult = articleClient.saveArticle(dto);
            if(!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }
            //回填article_id
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews,(short) 9,"审核成功");
        }

    }


    /**
     * 修改文章内容
     * @param wmNews
     * @param status
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 1。从自媒体文章的内容中提取文本和图片
     * 2.提取文章的封面图片
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {

        //存储纯文本内容
        StringBuilder stringBuilder = new StringBuilder();

        List<String> images = new ArrayList<>();

        //1。从自媒体文章的内容中提取文本和图片
        if(StringUtils.isNotBlank(wmNews.getContent())){
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")){
                    stringBuilder.append(map.get("value"));
                }

                if (map.get("type").equals("image")){
                    images.add((String) map.get("value"));
                }
            }
        }
        //2.提取文章的封面图片
        if(StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content",stringBuilder.toString());
        resultMap.put("images",images);
        return resultMap;

    }

    /**
     * 审核处理敏感词
     * @param content
     * @param wmNews
     * @return
     */
    public boolean handleSensitiveScan(String content,WmNews wmNews){
        // 1.查询所有的敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(new LambdaQueryWrapper<WmSensitive>().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        // 2.初始化关键字典库
        SensitiveWordUtil.initMap(sensitiveList);

        // 3.进行校验
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size()>0){
            updateWmNews(wmNews,(short)2,"当前文章中存在违规内容："+map);
            return false;
        }
        return true;
    }

    //转换图片为png格式
    public static String convertPng(String url) {
        String tarFilePath = url.substring(0, url.lastIndexOf(".")) + ".png";
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(url));
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.white, null);
            ImageIO.write(newBufferedImage, "png", new File(tarFilePath));
        } catch (IOException e) {
            return "";
        }
        return tarFilePath;
    }
}
