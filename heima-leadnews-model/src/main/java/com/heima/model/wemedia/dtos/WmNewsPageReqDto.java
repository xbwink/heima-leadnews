package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.Date;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-28 14:16
 * @vesion 1.0
 */
@Data
public class WmNewsPageReqDto extends PageRequestDto {

    /**
     * 状态
     */
    private Integer status;
    /**
     * 关键字
     */
    private String keyword;
    /**
     * 频道id
     */
    private Integer channelId;
    private Date beginPubDate;
    private Date endPubDate;

}
