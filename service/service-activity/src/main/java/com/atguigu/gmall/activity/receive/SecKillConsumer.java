package com.atguigu.gmall.activity.receive;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.activity.service.SecKillService;
import com.atguigu.gmall.model.user.UserRecode;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/12 19:02
 * @Description:
 */
@Component
public class SecKillConsumer {

    @Autowired
    SecKillService secKillService;

    //监听消息
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "exchange.direct.seckill.user",durable = "true"),
            value = @Queue(value = "queue.seckill.user",durable = "true"),
            key = {"seckill.user"}
    ))
    public void receive(Channel channel, Message message, String str){

        UserRecode recode = JSON.parseObject(str, UserRecode.class);

        System.out.println(recode.getUserId()+"号用户，抢购了"+recode.getSkuId()+"号商品");

        //下单
        secKillService.seckillOrder(recode);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
