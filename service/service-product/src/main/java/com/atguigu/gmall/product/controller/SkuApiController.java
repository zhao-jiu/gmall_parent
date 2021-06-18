package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/19 0:35
 * @Description:
 */
@RestController
@RequestMapping("/admin/product")
@CrossOrigin
public class SkuApiController {


    @Autowired
    SkuService skuService;
    //下架
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){

        skuService.cancelSale(skuId);

        return  Result.ok();
    }

    //上架
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){

         skuService.onSale(skuId);

        return  Result.ok();
    }


    //skuInfo 列表
    @GetMapping("list/{page}/{limit}")
    public Result list(@PathVariable("page") Long page,@PathVariable("limit") Long limit){

        IPage<SkuInfo> skuInfoIPage = skuService.list(page, limit);

        return  Result.ok(skuInfoIPage);
    }

    //保存skuInfo
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){

        skuService.saveSkuInfo(skuInfo);

        return Result.ok();
    }
}
