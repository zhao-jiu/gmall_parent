package com.atguigu.gmall.product.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.cache.GmallCache;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 14:29
 * @Description:
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;


    @Override
    public List<BaseCategory1> getCategory1() {
        return   baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category1_id",category1Id);
        return baseCategory2Mapper.selectList(queryWrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category2_id",category2Id);
        return baseCategory3Mapper.selectList(queryWrapper);
    }

    @GmallCache
    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        QueryWrapper<BaseCategoryView> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);
        return baseCategoryViewMapper.selectOne(queryWrapper);
    }

    @Override
    public List<BaseCategoryView> getBaseCategoryList() {

        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);

        return baseCategoryViews;
    }


}
