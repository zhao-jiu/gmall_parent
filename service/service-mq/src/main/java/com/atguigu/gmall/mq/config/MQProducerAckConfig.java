package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/8 14:44
 * @Description:
 */
@Component
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);   //指定ConfirmCallback
        rabbitTemplate.setReturnCallback(this);    //指定ReturnCallback
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        System.out.println("确认消息");
    }

    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        System.out.println("回调消息");
    }
}
