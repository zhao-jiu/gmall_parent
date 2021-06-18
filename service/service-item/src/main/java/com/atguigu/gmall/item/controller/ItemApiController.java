package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/19 18:47
 * @Description:
 */
@RestController
@RequestMapping("api/item")
@CrossOrigin
public class ItemApiController {


    @Autowired
    ItemService itemService;

    @RequestMapping("item/{skuId}")
    Map<String, Object> item(@PathVariable("skuId") Long skuId){
        Map<String, Object> map = itemService.item(skuId);
        return map;
    }
}
