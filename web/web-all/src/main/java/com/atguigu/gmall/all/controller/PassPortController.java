package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @author 赵赳
 * @CreateTime: 2021/4/29 18:26
 * @Description:
 */
@Controller
public class PassPortController {

    @RequestMapping("login.html")
    public String login(String originUrl, Model model){

        //将网关传递的原始请求路径传递到页面中
        model.addAttribute("originUrl",originUrl);

        return "login";
    }
}
