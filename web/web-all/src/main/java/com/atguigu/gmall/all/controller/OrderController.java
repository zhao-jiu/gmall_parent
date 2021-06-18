package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/1 17:36
 * @Description:
 */
@Controller
public class OrderController {

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    OrderFeignClient orderFeignClient;

    /***
     * 跳转订单结算页面
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("trade.html")
    public String trade(HttpServletRequest request, Model model){

        String userId = request.getHeader("userId");
        //获取用户地址信息
        List<UserAddress> addresses = userFeignClient.getUserAddressByUserId(userId);
        //获取商品详情信息
        List<OrderDetail> detailArrayList = orderFeignClient.getDetailList();
        //自动生成交易校验码，用于防止表单重复提交
        String  tradeNo = orderFeignClient.genTradeNo();

        model.addAttribute("userAddressList",addresses);
        model.addAttribute("detailArrayList",detailArrayList);
        model.addAttribute("totalAmount",getTotalAmount(detailArrayList));
        model.addAttribute("totalNum",getTotalNum(detailArrayList));
        model.addAttribute("tradeNo",tradeNo);
        return "order/trade";
    }

    //获取商品总数
    private Integer getTotalNum(List<OrderDetail> detailArrayList) {

        Integer totalNum = 0;

        if(detailArrayList!=null && detailArrayList.size()>0) {
            for (OrderDetail orderDetail : detailArrayList) {
                totalNum += orderDetail.getSkuNum();
            }
        }
        return totalNum;
    }
    //获取商品总价
    private BigDecimal getTotalAmount(List<OrderDetail> detailArrayList) {
        BigDecimal totalAmount = new BigDecimal("0");

        if(detailArrayList!=null && detailArrayList.size()>0){
            for (OrderDetail orderDetail : detailArrayList) {
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                totalAmount = totalAmount.add(orderPrice);
            }
        }
        return totalAmount;
    }

}
