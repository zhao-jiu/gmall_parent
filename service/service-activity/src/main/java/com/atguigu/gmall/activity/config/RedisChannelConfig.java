package com.atguigu.gmall.activity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisChannelConfig {


    /**
     * 注入订阅主题
     *
     * @param connectionFactory redis 链接工厂
     * @param listenerAdapter   消息监听适配器
     * @return 订阅主题对象
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("seckillpush"));
        return container;
    }

    /**
     * 返回消息监听器
     *
     * @param receiver 创建接收消息对象
     * @return
     */
    @Bean
    MessageListenerAdapter listenerAdapter(MessageReceive receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
        //注入操作数据的template
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

}