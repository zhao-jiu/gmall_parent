package com.atguigu.gmall.item.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/19 18:55
 * @Description:
 */
@FeignClient(value = "service-item")
public interface ItemFeignClient {
    @RequestMapping("api/item/item/{skuId}")
    Map<String, Object> item(@PathVariable("skuId") Long skuId);
}
