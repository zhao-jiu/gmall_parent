package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.atguigu.gmall.mq.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/8 14:40
 * @Description:
 */
@RestController
@RequestMapping("api/mq")
public class MQApiController {

    @Autowired
    RabbitService rabbitService;

    @RequestMapping("SendDelayMessages")
    public Result  SendDelayMessages(){
        //发送消息
        rabbitService.SendDelayMessages(DelayedMqConfig.exchange_delay,DelayedMqConfig.routing_delay,"hello rabbitmq!",5,TimeUnit.SECONDS);

        return Result.ok();
    }

    @RequestMapping("SendDeadLetterMessages")
    public Result  SendDeadLetterMessages(){
        //发送消息
        rabbitService.SendDeadLetterMessages(DeadLetterMqConfig.exchange_dead,DeadLetterMqConfig.routing_dead_1,"hello rabbitmq!",5,TimeUnit.SECONDS);

        return Result.ok();
    }

    @RequestMapping("testSendMessages")
    public Result  SendMessages(){
        //发送消息
        rabbitService.sendMessage("test.exchange","test.routingKey","hello rabbitmq!");

        return Result.ok();
    }
}
