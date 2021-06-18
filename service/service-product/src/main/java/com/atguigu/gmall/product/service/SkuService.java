package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/19 2:14
 * @Description:
 */
public interface SkuService {
    void saveSkuInfo(SkuInfo skuInfo);

    IPage<SkuInfo> list(Long page, Long limit);

    void cancelSale(Long skuId);

    void onSale(Long skuId);

    SkuInfo getSkuInfoById(Long skuId);

    List<SkuImage> getImageBySkuId(Long skuId);

    BigDecimal getSkuPriceBySkuId(Long skuId);


    List<Map<String, Object>> getSaleAttrValuesBySpu(Long spuId);


    List<SearchAttr> getSearchAttrList(Long skuId);
}
