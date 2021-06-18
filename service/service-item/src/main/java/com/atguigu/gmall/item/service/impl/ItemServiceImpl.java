package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.search.client.SearchFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/19 20:54
 * @Description:  Service-item调用Service-product微服务获取商品详情信息
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    SearchFeignClient searchFeignClient;

    //加入线程池执行异步任务 优化缓存功能
    @Override
    public Map<String, Object> item(Long skuId) {
      //  long start = System.currentTimeMillis();
        Map<String,Object> map = new HashMap<>();

        CompletableFuture<SkuInfo> completableFutureSku = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                //获取sku信息
                SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
                return skuInfo;
            }
        },executor);

        CompletableFuture<Void> completableFutureView = completableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //商品分类信息
                BaseCategoryView baseCategoryView = productFeignClient.getCategoryViewByCategory3Id(skuInfo.getCategory3Id());
                map.put("categoryView", baseCategoryView);
            }
        },executor);

        CompletableFuture<Void> completableFutureImage = completableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //获取图片信息
                List<SkuImage> skuImages = productFeignClient.getImageBySkuId(skuId);
                skuInfo.setSkuImageList(skuImages);
                map.put("skuInfo", skuInfo);
            }
        },executor);

        CompletableFuture<Void> completableFutureSaleAttr = completableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //获取销售属性
                List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListBySpuId(skuInfo.getSpuId(), skuId);
                map.put("spuSaleAttrList", spuSaleAttrList);
            }
        },executor);

        CompletableFuture<Void> completableFuturePrice = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                //商品价格
                BigDecimal price = productFeignClient.getSkuPriceBySkuId(skuId);
                map.put("price", price);
            }
        },executor);

        CompletableFuture<Void> completableFutureValuesJson = completableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //通过spuId查询对应的sku销售属性值
                List<Map<String, Object>> valueMap = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());

                Map<String, String> jsonMap = new HashMap<>();
                for (Map<String, Object> objectMap : valueMap) {
                    Integer sku_id = (Integer) objectMap.get("sku_id");
                    String saleAttrValues = (String) objectMap.get("saleAttrValues");
                    jsonMap.put(saleAttrValues, sku_id + "");
                }
                //封装成为Json数据
                String valuesSkuJson = JSON.toJSONString(jsonMap);
                map.put("valuesSkuJson", valuesSkuJson);
            }
        },executor);

        CompletableFuture.allOf(completableFutureSku,completableFutureView,completableFutureImage,completableFutureSaleAttr,completableFuturePrice,completableFutureValuesJson).join();

//        long end = System.currentTimeMillis();
//        System.out.println("方法执行了：" + (end - start));

        //更新热度值
        searchFeignClient.hotScore(skuId);

        return map;
    }

    //back
    private Map<String, Object> getStringObjectMap(Long skuId) {
        long start = System.currentTimeMillis();
        Map<String,Object> map = new HashMap<>();
        //获取sku信息
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);

        //商品分类
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryViewByCategory3Id(skuInfo.getCategory3Id());

        //获取图片信息
        List<SkuImage> skuImages = productFeignClient.getImageBySkuId(skuId);
        skuInfo.setSkuImageList(skuImages);

        //获取销售属性
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListBySpuId(skuInfo.getSpuId(),skuId);

        //商品价格
        BigDecimal price =productFeignClient.getSkuPriceBySkuId(skuId);

        //通过spuId查询销售属性值
        List<Map<String,Object>> valueMap = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());

        Map<String, String> jsonMap = new HashMap<>();
        for (Map<String, Object> objectMap : valueMap) {
            Integer sku_id = (Integer) objectMap.get("sku_id");
            String saleAttrValues = (String) objectMap.get("saleAttrValues");
            jsonMap.put(saleAttrValues,sku_id+"");
        }
        //封装成为Json数据
        String valuesSkuJson = JSON.toJSONString(jsonMap);

        map.put("skuInfo",skuInfo);
        map.put("categoryView",baseCategoryView);
        map.put("price",price);
        map.put("spuSaleAttrList",spuSaleAttrList);
        map.put("valuesSkuJson", valuesSkuJson);
        long end = System.currentTimeMillis();
        System.out.println("方法执行了：" + (end - start));
        return map;
    }
}
