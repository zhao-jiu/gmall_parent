package com.atguigu.gmall.mq.service.impl;

import com.atguigu.gmall.mq.service.RabbitService;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/9 11:18
 * @Description:
 */
@Service
public class RabbitServiceImpl implements RabbitService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     *  发送普通消息
     * @param exchange
     * @param routing
     * @param message
     */
    @Override
    public void sendMessage(String exchange, String routing, String message) {

        rabbitTemplate.convertAndSend(exchange,routing,message);
    }

    /**
     * 发送死信消息
     * @param exchange_dead
     * @param routing_dead_1
     * @param message
     * @param i
     * @param seconds
     */
    @Override
    public void SendDeadLetterMessages(String exchange_dead, String routing_dead_1, String message, int i, TimeUnit seconds) {
        rabbitTemplate.convertAndSend(exchange_dead,routing_dead_1,message, messagePostProcessor ->{
            messagePostProcessor.getMessageProperties().setExpiration(i*1000+"");
            return messagePostProcessor;
        });
    }

    /**
     * 发送延迟消息
     * @param exchange_delay
     * @param routing_delay
     * @param message
     * @param i
     * @param seconds
     */
    @Override
    public void SendDelayMessages(String exchange_delay, String routing_delay, String message, int i, TimeUnit seconds) {
        rabbitTemplate.convertAndSend(exchange_delay,routing_delay,message,message1 -> {

            message1.getMessageProperties().setDelay(i*1000);

            return message1;
        });
    }
}
