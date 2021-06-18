package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseCategoryView;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 14:29
 * @Description:
 */
public interface CategoryService {
    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long category1Id);

    List<BaseCategory3> getCategory3(Long category2Id);

    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    List<BaseCategoryView> getBaseCategoryList();

}
