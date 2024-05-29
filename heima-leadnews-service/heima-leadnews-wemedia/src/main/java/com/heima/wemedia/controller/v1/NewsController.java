package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.dtos.WmNewsSuPublishDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-25 14:45
 * @vesion 1.0
 */

@Api(tags = "自媒文章内容控制器")
@Slf4j
@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    @Autowired
    private WmNewsService wmNewsService;

    @ApiOperation("查询文章列表")
    @PostMapping ("/list")
    public ResponseResult getNewsList(@RequestBody WmNewsPageReqDto dto){

        return wmNewsService.getNewsList(dto);
    }

    @ApiOperation("发布文章")
    @PostMapping ("/submit")
    public ResponseResult submit(@RequestBody WmNewsSuPublishDto dto){
        return wmNewsService.submitNews(dto);
    }

    @ApiOperation("文章详情")
    @GetMapping ("/one/{id}")
    public ResponseResult getInfo(@PathVariable("id") Integer id){
        return wmNewsService.selectInfoById(id);
    }

    @ApiOperation("文章上下架")
    @PostMapping ("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDto dto){
        return wmNewsService.downOrUp(dto);
    }


}
