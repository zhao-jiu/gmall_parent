package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/28 18:07
 * @Description:
 */
@FeignClient(value = "service-cart")
public interface CartFeignCilent {

    @RequestMapping("api/cart/addCart/{skuId}/{skuNum}")
    CartInfo addCart(@PathVariable("skuId") Long skuId,@PathVariable("skuNum")  Integer skuNum);

    @RequestMapping("api/cart/getCartInfoByUser/{userId}")
    List<CartInfo> getCartInfoByUser(@PathVariable("userId") String userId);
}
