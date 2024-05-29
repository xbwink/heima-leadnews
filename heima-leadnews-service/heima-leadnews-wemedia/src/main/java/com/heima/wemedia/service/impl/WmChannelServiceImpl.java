package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-28 11:12
 * @vesion 1.0
 */
@Service
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {

    /**
     * 查询频道列表
     *
     * @return
     */
    @Override
    public ResponseResult getChannelList() {
        LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<WmChannel>().orderByAsc(WmChannel::getOrd);
        List<WmChannel> list = list(wrapper);
        return ResponseResult.okResult(list);
    }

}
