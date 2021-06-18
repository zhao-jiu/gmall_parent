package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/28 18:19
 * @Description:
 */
public interface CartService {
    CartInfo addCart(Long skuId, Integer skuNum, String userId);

    List<CartInfo> cartList(String userId);

    void checkCart(Long skuId, Integer isChecked, String userId);

    List<CartInfo> getCartInfoByUser(String userId);

}
