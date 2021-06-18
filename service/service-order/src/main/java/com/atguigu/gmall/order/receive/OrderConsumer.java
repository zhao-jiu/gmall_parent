package com.atguigu.gmall.order.receive;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/10 19:43
 * @Description:
 */
@Component
public class OrderConsumer {

    @Autowired
    OrderService orderService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(durable = "true",autoDelete = "false",value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY,durable = "true",autoDelete = "false"),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void receive(Channel channel, Message message, String str) throws IOException {

        System.out.println("订单系统消费");
        PaymentInfo paymentInfo = JSON.parseObject(str, PaymentInfo.class);
        //支付完成，修改订单状态
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        orderInfo.setTrackingNo(paymentInfo.getTradeNo());
        orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
        orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());

        orderService.updateStatus(orderInfo);

        //发送消息，通知库存系统锁定库存
        orderService.sendWareStockMessage(orderInfo);

        //签收
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
