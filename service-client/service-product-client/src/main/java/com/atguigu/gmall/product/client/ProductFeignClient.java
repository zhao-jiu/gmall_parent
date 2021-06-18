package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/19 20:48
 * @Description:
 */
@FeignClient(value = "service-product")
public interface ProductFeignClient {

    @RequestMapping("api/product/getSkuInfoById/{skuId}")
    SkuInfo getSkuInfoById(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getImageBySkuId/{skuId}")
    List<SkuImage> getImageBySkuId(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getCategoryViewByCategory3Id/{category3Id}")
    BaseCategoryView getCategoryViewByCategory3Id(@PathVariable("category3Id") Long category3Id);

    @RequestMapping("api/product/getSkuPriceBySkuId/{skuId}")
    BigDecimal getSkuPriceBySkuId(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getSpuSaleAttrListBySpuId/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrListBySpuId(@PathVariable("spuId")Long spuId,@PathVariable("skuId")Long skuId);

    @RequestMapping("api/product/getSaleAttrValuesBySpu/{spuId}")
    List<Map<String, Object>> getSaleAttrValuesBySpu(@PathVariable("spuId")Long spuId);

    @RequestMapping("api/product/getTrademarkById/{tmId}")
    BaseTrademark getTrademarkById(@PathVariable("tmId") Long tmId);

    @RequestMapping("api/product/getSearchAttrList/{spuId}")
    List<SearchAttr> getSearchAttrList(@PathVariable("spuId") Long skuId);

    @RequestMapping("api/product/getBaseCategoryList")
    List<BaseCategoryView> getBaseCategoryList();
}
