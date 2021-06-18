package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/6 14:36
 * @Description:
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    OrderService orderService;

   // auth/1/10
    @RequestMapping("auth/{pageNo}/{pageSize}")
    public Result orderPage(@PathVariable("pageNo") Long pageNo,@PathVariable("pageSize") Long pageSize){

        Page<OrderInfo> page = orderService.orderPage(pageNo,pageSize);

        return Result.ok(page);
    }

    @RequestMapping("getOrderInfoById/{orderId}")
    OrderInfo getOrderInfoById(@PathVariable("orderId") Long orderId){

        OrderInfo orderInfo = orderService.getOrderInfoById(orderId);

        return orderInfo;

    }

    /**
     * 生成交易校验码，防止表单重复提交
     * @param request
     * @return
     */
    @RequestMapping("genTradeNo")
    String genTradeNo(HttpServletRequest request){

        String userId = request.getHeader("userId");

        return orderService.genTradeNo(userId);

    }

    /**
     * 提交订单
     * @param tradeNo
     * @param request
     * @param orderInfo
     * @return
     */
    @RequestMapping("/auth/submitOrder")
    Result submitOrder(String tradeNo, HttpServletRequest request, @RequestBody OrderInfo orderInfo){

        String userId = request.getHeader("userId");
        //校验结算码，防止表单重复提交
        Boolean result = orderService.checkTradeNo(tradeNo,userId);

        if(result){
            Boolean isSubmit = orderService.submitOrder(userId, orderInfo);
            if(isSubmit){
                //返回orderId，用户后续支付宝支付操作
                return  Result.ok(orderInfo.getId());
            }else {
                return Result.fail("价格或库存发送了变化，交易失败");
            }
        }else {
            return Result.fail();
        }

    }

    /**
     * 获取商品详情列表
     * @param request
     * @return
     */
    @RequestMapping("getDetailList")
    List<OrderDetail> getDetailList(HttpServletRequest request){

        String userId = request.getHeader("userId");

        return  orderService.getDetailList(userId);
    }



}
