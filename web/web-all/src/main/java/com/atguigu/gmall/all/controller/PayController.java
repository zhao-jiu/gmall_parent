package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/6 15:17
 * @Description:
 */
@Controller
public class PayController {

    @Autowired
    OrderFeignClient orderFeignClient;

    /**
     * 支付成功后跳转支付成功页面
     * @return
     */
    @RequestMapping("payment/success.html")
    public String paySuccess(){

        return "payment/success";
    }

    /**
     * 跳转支付页面
     * @param orderId 订单号，页面传递
     * @param model
     * @return
     */
    @RequestMapping("pay.html")
    public String trade(Long orderId, Model model){

        //通过userId获取订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(orderId);

        model.addAttribute("orderInfo",orderInfo);

        return "payment/pay";
    }
}
