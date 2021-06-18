package com.atguigu.gmall.all;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/12 20:28
 * @Description:
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages= {"com.atguigu.gmall"})
@EnableDiscoveryClient
@ComponentScan("com.atguigu.gmall")
public class WebAllApplication {

    public static void main(String[] args) {
            SpringApplication.run(WebAllApplication.class,args);
        }
}
