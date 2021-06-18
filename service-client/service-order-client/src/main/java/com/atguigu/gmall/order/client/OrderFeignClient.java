package com.atguigu.gmall.order.client;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/6 14:33
 * @Description:
 */
@FeignClient(value = "service-order")
public interface OrderFeignClient {

    @RequestMapping("api/order/getDetailList")
    List<OrderDetail> getDetailList();

    @RequestMapping("api/order/genTradeNo")
    String genTradeNo();

    @RequestMapping("api/order/getOrderInfoById/{orderId}")
    OrderInfo getOrderInfoById(@PathVariable("orderId") Long orderId);

}
