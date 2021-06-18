package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignCilent;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/27 20:06
 * @Description: 购物车
 */
@Controller
public class CartController {

    @Autowired
    CartFeignCilent cartFeignCilent;

    // /cart/cart.html

    @RequestMapping("cart/cart.html")
    public String toCart(){

        return "cart/index";
    }

    @RequestMapping("addCart.html")
    public String addCart(Long skuId,Integer skuNum){

        //调用service添加购物车
        CartInfo cartInfo = cartFeignCilent.addCart(skuId,skuNum);

        return "redirect:http://cart.gmall.com/cart/addCart.html";
    }


}
