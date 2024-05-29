package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-26 14:47
 * @vesion 1.0
 */
public class WmMaterialDto extends PageRequestDto {

    /**
     * 1 收藏
     * 0 未收藏
     */
    private Short isCollection;

    public Short getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(Short isCollection) {
        this.isCollection = isCollection;
    }
}
