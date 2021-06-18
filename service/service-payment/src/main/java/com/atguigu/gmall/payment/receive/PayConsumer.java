package com.atguigu.gmall.payment.receive;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.service.PayService;
import com.atguigu.gmall.rabbit.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/10 17:05
 * @Description:
 */
@Component
public class PayConsumer {

    @Autowired
    PayService payService;

    @Autowired
    OrderFeignClient orderFeignClient;

    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void a(Channel channel, Message message, String str) throws IOException {

        System.out.println("延迟检测支付结果");
        System.out.println(str);

        Map map = JSON.parseObject(str, Map.class);

        Integer orderId = (Integer) map.get("orderId");

        //调用支付订单查询接口检测订单支付状态
        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(Long.parseLong(orderId+""));
        Map<String, Object> statusMap = payService.payCheck(orderInfo.getOutTradeNo());

        String tradeStatus = (String) statusMap.get("tradeStatus");

        if (tradeStatus.equals("交易未创建") || tradeStatus.equals("WAIT_BUYER_PAY")) {
            Integer count = (Integer) map.get("count");
            if (count > 0) {
                payService.sendPayCheckQueue(map);
            }else {
                System.out.println("消费结束，循环结束");
            }
        }else {
            //交易完成修改状态
            String tradeNo = (String) statusMap.get("tradeNo");
            String callbackContent = (String) statusMap.get("callbackContent");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus(tradeStatus);
            paymentInfo.setTradeNo(tradeNo);
            paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
            paymentInfo.setCallbackContent(callbackContent);
            paymentInfo.setCallbackTime(new Date());
            //幂等性检查
            String payStatus = payService.checkPayStatus(orderInfo.getOutTradeNo());
            if(!StringUtils.isEmpty(payStatus) && !payStatus.equals("TRADE_SUCCESS")){
                payService.updatePayStatus(paymentInfo);
            }
        }

        //签收
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
