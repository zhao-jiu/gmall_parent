package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/1 14:48
 * @Description:
 */
public interface UserService {
    Map<String, Object> verify(String token);

    UserInfo login(UserInfo userInfo);

    List<UserAddress> getUserAddressByUserId(String userId);
}
