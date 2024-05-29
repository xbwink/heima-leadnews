package com.heima.model.wemedia.dtos;

import com.heima.model.wemedia.pojos.WmNews;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-29 10:09
 * @vesion 1.0
 */
@Data
public class WmNewsSuPublishDto {

    private Integer id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 频道id
     */
    private Integer channelId;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 标签
     */
    private String labels;


    /**
     * 定时发布时间
     */
    private Date publishTime;

    /**
     * 封面图片
     */
    private List<String> images;

    /**
     * 草稿or发布
     */
    private Short status;

    /**
     * 封面类型
     * 1 单图
     * 3 三图
     * 0 无图
     * -1 自动 （默认单图）
     */
    private Short type;


}
