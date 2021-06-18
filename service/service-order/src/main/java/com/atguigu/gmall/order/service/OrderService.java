package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/6 14:39
 * @Description:
 */
public interface OrderService {
    List<OrderDetail> getDetailList(String userId);

    Boolean submitOrder(String userId, OrderInfo orderInfo);

    String genTradeNo(String userId);

    Boolean checkTradeNo(String tradeNo, String userId);

    OrderInfo getOrderInfoById(Long orderId);

    void updateStatus(OrderInfo orderInfo);

    void sendWareStockMessage(OrderInfo orderInfo);

    Page<OrderInfo> orderPage(Long pageNo, Long pageSize);

}
