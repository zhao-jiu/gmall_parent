package com.atguigu.gmall.test;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 14:09
 * @Description:
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ServiceTestApplication {
    public static void main(String[] args) {
            SpringApplication.run(ServiceTestApplication.class,args);
        }
}
