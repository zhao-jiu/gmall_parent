package com.atguigu.gmall.mq.receive;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/8 14:47
 * @Description:
 */
@Component
public class ReceiveTest {

    /**
     * 消费延迟队列
     * @param channel
     * @param message
     * @param str
     * @throws IOException
     */
    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void receiveDelay(Channel channel, Message message,String str) throws IOException {
        System.out.println("消费延迟队列");
        System.out.println(message.getBody());
        System.out.println(str);
        //签收
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    /**
     * 消费死信队列
     * @param channel
     * @param message
     * @param str
     * @throws IOException
     */
    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void receiveDeadLetter(Channel channel, Message message,String str) throws IOException {
        System.out.println("消费死信队列");
        System.out.println(message.getBody());
        System.out.println(str);
        //签收
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(durable = "true",autoDelete = "false",value = "test.exchange"),
            value = @Queue(value = "test.queue",durable = "true",autoDelete = "false"),
            key = {"test.routingKey"}
    ))
    public void receive(Channel channel, Message message,String str) throws IOException {
        System.out.println(message.getBody());
        System.out.println(str);
        //签收
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
