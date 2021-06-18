package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 14:16
 * @Description: 查询商品分类
 */
@RestController
@RequestMapping("/admin/product")
@CrossOrigin
public class CategoryApiController {

    @Autowired
    CategoryService categoryService;


    @GetMapping("/getCategory1")
    public Result getCategory1(){

        List<BaseCategory1> category1List = categoryService.getCategory1();

        return Result.ok(category1List);
    }

    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable("category1Id") Long category1Id){

        List<BaseCategory2> category2List = categoryService.getCategory2(category1Id);

        return Result.ok(category2List);
    }

    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable("category2Id") Long category2Id){

        List<BaseCategory3> category3List = categoryService.getCategory3(category2Id);

        return Result.ok(category3List);
    }


}
