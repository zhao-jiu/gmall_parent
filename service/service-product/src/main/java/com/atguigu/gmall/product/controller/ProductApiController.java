package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.CategoryService;
import com.atguigu.gmall.product.service.SkuService;
import com.atguigu.gmall.product.service.SpuService;
import com.atguigu.gmall.product.service.TrademarkService;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/19 20:49
 * @Description:
 */
@RestController
@RequestMapping("api/product")
//@CrossOrigin
public class ProductApiController {

    @Autowired
    SkuService skuService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    SpuService spuService;

    @Autowired
    TrademarkService trademarkService;

    @RequestMapping("getBaseCategoryList")
    List<BaseCategoryView> getBaseCategoryList(){
        List<BaseCategoryView> list =  categoryService.getBaseCategoryList();
        return list;
    }

    @RequestMapping("getSearchAttrList/{spuId}")
    List<SearchAttr> getSearchAttrList(@PathVariable("spuId") Long skuId){
        return skuService.getSearchAttrList(skuId);
    }

    @RequestMapping("getTrademarkById/{tmId}")
    BaseTrademark getTrademarkById(@PathVariable("tmId") Long tmId){
        return  trademarkService.getTrademarkById(tmId);
    }

    @RequestMapping("getSaleAttrValuesBySpu/{spuId}")
    List<Map<String, Object>> getSaleAttrValuesBySpu(@PathVariable("spuId")Long spuId){
        List<Map<String, Object>> map = skuService.getSaleAttrValuesBySpu(spuId);
        return map;
    }

    @RequestMapping("getSpuSaleAttrListBySpuId/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrListBySpuId(@PathVariable("spuId")Long spuId,@PathVariable("skuId")Long skuId){
        return spuService.getSpuSaleAttrListBySpuId(spuId,skuId);
    }

    @RequestMapping("getSkuPriceBySkuId/{skuId}")
    BigDecimal getSkuPriceBySkuId(@PathVariable("skuId")Long skuId){
        return skuService.getSkuPriceBySkuId(skuId);
    }

    @RequestMapping("getCategoryViewByCategory3Id/{category3Id}")
    BaseCategoryView getCategoryViewByCategory3Id(@PathVariable("category3Id") Long category3Id){
        return categoryService.getCategoryViewByCategory3Id(category3Id);
    }

    @RequestMapping("getImageBySkuId/{skuId}")
    List<SkuImage> getImageBySkuId(@PathVariable("skuId") Long skuId){
        List<SkuImage> skuImageList = skuService.getImageBySkuId(skuId);
        return skuImageList;
    }

    @RequestMapping("getSkuInfoById/{skuId}")
    SkuInfo getSkuInfoById(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo = skuService.getSkuInfoById(skuId);
        return skuInfo;
    }
}
