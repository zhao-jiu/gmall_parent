package com.atguigu.gmall.test.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 14:10
 * @Description:
 */
@RestController
public class TestController {

    @GetMapping("/testApi")
    public String testApi(){
        return "testApi";
    }
}
