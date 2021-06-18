package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/1 14:46
 * @Description:
 */
@RestController
@RequestMapping("api/user/passport")
public class UserApiController {

    @Autowired
    UserService userService;

    @RequestMapping("getUserAddressByUserId/{userId}")
    List<UserAddress> getUserAddressByUserId(@PathVariable("userId") String userId, HttpServletRequest request){

        String userIdTest = request.getHeader("userId");

        List<UserAddress> addresses = userService.getUserAddressByUserId(userId);
        return addresses;
    }

    //login
    @RequestMapping("login")
    Result login(@RequestBody UserInfo userInfo){

        UserInfo userInfoResult =userService.login(userInfo);

        if(!StringUtils.isEmpty(userInfoResult.getToken())){
            //登录成功
            return Result.ok(userInfoResult);
        }else {
            //登录失败
            return Result.fail("用户名或密码错误");
        }
    }


    @RequestMapping("verify/{token}")
    Map<String, Object> verify(@PathVariable("token") String token){

        Map<String, Object> map = userService.verify(token);

        return map;
    }
}
