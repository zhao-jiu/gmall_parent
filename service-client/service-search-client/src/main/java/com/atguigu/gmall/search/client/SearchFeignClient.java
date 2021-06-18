package com.atguigu.gmall.search.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/24 13:53
 * @Description:
 */
@FeignClient(value = "service-search")
public interface SearchFeignClient {

    @RequestMapping("api/list/cancelSale/{skuId}")
    void cancelSale(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/list/onSale/{skuId}")
    void onSale(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/list/getBaseCategoryList")
    List<JSONObject> getBaseCategoryList();

    @RequestMapping("api/list/search")
    SearchResponseVo search(@RequestBody SearchParam searchParam);

    @RequestMapping("api/list/hotScore/{skuId}")
    void hotScore(@PathVariable("skuId") Long skuId);
}
