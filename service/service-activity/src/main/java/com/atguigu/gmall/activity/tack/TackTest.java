package com.atguigu.gmall.activity.tack;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/11 15:59
 * @Description:
 */
@EnableScheduling
@Component
public class TackTest {

//    @Scheduled(cron = "1/5 * * * * ?")
//    public void task(){
//        System.out.println("执行定时任务！！");
//    }
}
