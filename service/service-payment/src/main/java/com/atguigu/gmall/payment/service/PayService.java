package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/7 14:45
 * @Description:
 */
public interface PayService {
    String tradePagePay(Long orderId);

    void save(PaymentInfo paymentInfo);

    void updatePayStatus(PaymentInfo paymentInfo);

    void sendPayCheckQueue(Map<String, Object> map);

    Map<String, Object> payCheck(String outTradeNo);

    String checkPayStatus(String out_trade_no);

}
