package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/24 13:58
 * @Description:
 */
public interface SearchService {
    void cancelSale(Long skuId);

    void onSale(Long skuId);

    void creatGoods();

    List<JSONObject> getBaseCategoryList();

    SearchResponseVo search(SearchParam searchParam);

    void hotScore(Long skuId);
}
