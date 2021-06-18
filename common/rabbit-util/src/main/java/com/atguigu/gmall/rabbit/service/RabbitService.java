package com.atguigu.gmall.rabbit.service;

import java.util.concurrent.TimeUnit;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/9 11:17
 * @Description:
 */
public interface RabbitService {
    void sendMessage(String s, String s1, String s2);

    void SendDeadLetterMessages(String exchange_dead, String routing_dead_1, String s, int i, TimeUnit seconds);

    void SendDelayMessages(String exchange_delay, String routing_delay, String message, int i, TimeUnit seconds);
}
