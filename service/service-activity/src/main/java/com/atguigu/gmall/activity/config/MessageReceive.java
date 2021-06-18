package com.atguigu.gmall.activity.config;

import org.springframework.stereotype.Component;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/11 16:04
 * @Description:
 */
@Component
public class MessageReceive {

    /**
     * 监听发布的库存状态信息
     * @param message
     */
    public void receiveMessage(String message){

        System.out.println("接受到的消息：-->"+message);
        //将发布的库存状态存储在服务器中
        String strForRedis = message.replaceAll("\"", "");

        String[] split = strForRedis.split(":");
        String skuId = split[0];
        String status = split[1];

        CacheHelper.put(skuId,status);
    }

}
