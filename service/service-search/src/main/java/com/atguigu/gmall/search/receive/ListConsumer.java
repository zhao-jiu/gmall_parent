package com.atguigu.gmall.search.receive;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.service.SearchService;
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
 * @CreateTime: 2021/5/14 16:01
 * @Description:
 */
@Component
public class ListConsumer {

    @Autowired
    SearchService searchService;

    /**
     * 上架商品
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "exchange.direct.goods",durable = "true"),
            value = @Queue(value = "queue.goods.upper",durable = "true"),
            key = {"goods.upper"}
    ))
    public void upperGoods(Channel channel, Message message, String str){

       Long skuId = JSON.parseObject(str, Long.class);

        if(skuId!=null){
            searchService.onSale(skuId);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    /**
     * 下架商品
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "exchange.direct.goods",durable = "true"),
            value = @Queue(value = "queue.goods.lower",durable = "true"),
            key = {"goods.lower"}
    ))
    public void lowerGoods(Channel channel, Message message, String str){

        Long skuId = JSON.parseObject(str, Long.class);

        if(skuId!=null){
            searchService.cancelSale(skuId);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
