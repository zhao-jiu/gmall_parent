package com.atguigu.gmall.search.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/23 18:12
 * @Description:
 */
@RestController
@RequestMapping("api/list")
public class SearchApiController {

    @Autowired
    SearchService searchService;

    @RequestMapping("hotScore/{skuId}")
    void hotScore(@PathVariable("skuId") Long skuId){
        searchService.hotScore(skuId);
    }

    @RequestMapping("search")
    SearchResponseVo search(@RequestBody SearchParam searchParam){
        SearchResponseVo responseVo = searchService.search(searchParam);
        return responseVo;
    }

    @RequestMapping("getBaseCategoryList")
    List<JSONObject> getBaseCategoryList(){
        return  searchService.getBaseCategoryList();
    }

    @RequestMapping("cancelSale/{skuId}")
    void cancelSale(@PathVariable("skuId") Long skuId){
        searchService.cancelSale(skuId);
    }

    @RequestMapping("onSale/{skuId}")
    void onSale(@PathVariable("skuId") Long skuId){
        searchService.onSale(skuId);
    }

    @RequestMapping("creatGoods")
    public Result creatGoods(){
        searchService.creatGoods();
        return Result.ok();
    }

//    @RequestMapping("/testES")
//    public Result testES(){
//        Map<String, Object> mapping = restTemplate.getMapping("users", "_doc");
//        return Result.ok(mapping);
//    }
}
