package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/28 18:17
 * @Description:
 */
@RestController
@RequestMapping("api/cart")
//@CrossOrigin
public class CartApiController {

    @Autowired
    CartService cartService;


    @RequestMapping("getCartInfoByUser/{userId}")
    List<CartInfo> getCartInfoByUser(@PathVariable("userId") String userId){

        return cartService.getCartInfoByUser(userId);
    }

    /**
     * 改变购物车选中状态
     * @param skuId
     * @param isChecked
     * @return
     */
    @RequestMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable("skuId") Long skuId,@PathVariable("isChecked") Integer isChecked, HttpServletRequest request){

        String userId = request.getHeader("userId");
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userTempId");
        }

        cartService.checkCart(skuId,isChecked,userId);

        return Result.ok();
    }

    /***
     * 购物车列表
     * @param request
     * @return
     */
    @RequestMapping("cartList")
    public Result cartList(HttpServletRequest request){

        String userId = request.getHeader("userId");
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userTempId");
        }
        List<CartInfo> cartInfos = cartService.cartList(userId);

        return Result.ok(cartInfos);
    }

    /**
     * 页面添加购物车
     * @param skuId
     * @param skuNum
     * @return
     */
    @RequestMapping("addCart/{skuId}/{skuNum}")
    CartInfo addCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum")  Integer skuNum,
                     HttpServletRequest request){

        String userId = request.getHeader("userId");
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userTempId");
        }
        CartInfo cartInfo = cartService.addCart(skuId,skuNum,userId);

        return cartInfo;
    }

    /**
     * 购物车加减数量
     * @param skuId
     * @param skuNum
     * @return
     */
    @RequestMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum")  Integer skuNum,
                            HttpServletRequest request){

        String userId = request.getHeader("userId");
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userTempId");
        }

        cartService.addCart(skuId,skuNum,userId);

        return Result.ok();
    }

}
