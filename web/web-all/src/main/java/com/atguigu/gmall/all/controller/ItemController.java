package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/19 16:06
 * @Description:
 */
@Controller
public class ItemController {


    @Autowired
    ItemFeignClient itemFeignClient;

    @GetMapping("{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model){
        Map<String,Object> resultMap = itemFeignClient.item(skuId);
        model.addAllAttributes(resultMap);
        return "item/index";
    }


}
