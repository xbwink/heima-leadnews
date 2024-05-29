package com.heima.model.wemedia.dtos;

import lombok.Data;

@Data
public class WmNewsDto {

    private Integer id;
    /**
    * 是否上架  0 下架  1 上架
    */
    private Short enable;

}
