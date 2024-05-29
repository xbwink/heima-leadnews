package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-18 14:36
 * @vesion 1.0
 */

@Api(tags = "app端文章首页")
@RequestMapping("/api/v1/article")
@RestController
public class ArticleHomeController {

    @Autowired
    private ApArticleService service;

    @ApiOperation("加载首页文章")
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto dto) {
        return service.load(ArticleConstants.LOADTYPE_LOAD_MORE,dto);
    }

    @ApiOperation("加载更多文章")
    @PostMapping("/loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto dto) {
        return service.load(ArticleConstants.LOADTYPE_LOAD_MORE,dto);
    }

    @ApiOperation("刷新文章")
    @PostMapping("/loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto dto) {
        return service.load(ArticleConstants.LOADTYPE_LOAD_NEW,dto);

    }

}
