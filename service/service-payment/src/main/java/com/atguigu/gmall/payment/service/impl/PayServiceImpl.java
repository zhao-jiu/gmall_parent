package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.mapperr.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PayService;
import com.atguigu.gmall.rabbit.config.DelayedMqConfig;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/7 14:45
 * @Description:
 */
@Service
public class PayServiceImpl implements PayService {

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    RabbitService rabbitService;

    /**
     * 调用支付宝接口
     * @param orderId 订单id用于查询订单信息
     * @return 返回支付页面
     */
    @Override
    public String tradePagePay(Long orderId) {

        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(orderId);

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        //必选参数
        Map<Object, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);//支付金额
        map.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());
        String jsonString = JSON.toJSONString(map);

        //设置支付宝回调地址
        request.setReturnUrl(AlipayConfig.return_payment_url);
        request.setNotifyUrl(AlipayConfig.notify_payment_url);

        request.setBizContent(jsonString);

        AlipayTradePagePayResponse response = null;
        try {
            //调用支付宝接口返回支付页面
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //返回支付页面
        return response.getBody();
    }

    /**
     * 创建支付信息
     * @param paymentInfo 支付信息
     */
    @Override
    public void save(PaymentInfo paymentInfo) {
        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(paymentInfo.getOrderId());

        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setPaymentType(PaymentType.ALIPAY.toString());
        paymentInfoMapper.insert(paymentInfo);
    }

    /**
     * 支付成功后修改订单状态
     * @param paymentInfo 支付信息
     */
    @Override
    public void updatePayStatus(PaymentInfo paymentInfo) {

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("out_trade_no",paymentInfo.getOutTradeNo());

        //发送消息队列通知订单系统修改订单状态
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,JSON.toJSONString(paymentInfo));

        paymentInfoMapper.update(paymentInfo,queryWrapper);

    }

    @Override
    public void sendPayCheckQueue(Map<String, Object> map) {

        Integer count = (Integer) map.get("count");

        count --;

        map.put("count",count);
        //发送延迟队列延迟
        rabbitService.SendDelayMessages(DelayedMqConfig.exchange_delay,DelayedMqConfig.routing_delay,JSON.toJSONString(map), 10,TimeUnit.SECONDS);
    }

    @Override
    public Map<String, Object> payCheck(String outTradeNo) {

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();


        //必选参数
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        String jsonString = JSON.toJSONString(map);

        //设置支付宝回调地址
        request.setReturnUrl(AlipayConfig.return_payment_url);
        request.setNotifyUrl(AlipayConfig.notify_payment_url);

        request.setBizContent(jsonString);

        AlipayTradeQueryResponse response = null;
        try {
            //调用支付宝接口返回支付页面
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if(response.isSuccess()){
            map.put("tradeStatus",response.getTradeStatus());
            map.put("tradeNo",response.getTradeNo());
            map.put("callbackContent",response.getBody());
        }else {
            map.put("tradeStatus","交易未创建");
        }

        return map;
    }

    @Override
    public String checkPayStatus(String out_trade_no) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",out_trade_no);
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(queryWrapper);
        if(paymentInfo!=null){
            return paymentInfo.getPaymentStatus();
        }else {
            return null;
        }

    }


}
