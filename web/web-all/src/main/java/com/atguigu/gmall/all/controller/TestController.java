package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/12 20:29
 * @Description:
 */
@Controller
public class TestController {

    @GetMapping("/testWeb")
    public String testWeb(Model model){
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("元素"+i);
        }
        model.addAttribute("hello","hello Thymeleaf");
        model.addAttribute("list",list);
        return "testWeb";
    }

}
