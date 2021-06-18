package com.atguigu.gmall.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/21 13:54
 * @Description:
 */
@Configuration
public class ThreadPoolConfig {

    //全局线程池配置
    @Bean
    public ThreadPoolExecutor threadPool(){
        return new ThreadPoolExecutor(50,
                500,
                5,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000));
    }
}
