package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/1 14:48
 * @Description:
 */
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public Map<String, Object> verify(String token) {

        Map<String, Object> resultMap = new HashMap<>();

        //从redis中查询用户信息
        String userId = (String) redisTemplate.opsForValue().get("user:" + token);

        if(StringUtils.isEmpty(userId)){
            //鉴权失败
            resultMap.put("isSuccess",false);
        }else {
            //鉴权成功
            resultMap.put("userId", userId);
            resultMap.put("isSuccess",true);
        }

        return resultMap;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {

        String loginName = userInfo.getLoginName();
        String passwd = userInfo.getPasswd();
        String encryptPasswd = MD5.encrypt(passwd);

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name",loginName);
        queryWrapper.eq("passwd",encryptPasswd);
        UserInfo userInfoResult = userInfoMapper.selectOne(queryWrapper);

        if(userInfoResult!=null){
            //登录成功,生成token
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            //存储到信息redis中
            redisTemplate.opsForValue().set("user:"+token,userInfoResult.getId()+"");
            //返回用户信息
            userInfo.setPasswd("");
            userInfo.setToken(token);
            userInfo.setId(userInfoResult.getId());
            userInfo.setNickName(userInfoResult.getNickName());
        }

        return userInfo;
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(String userId) {
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id",userId);

        return userAddressMapper.selectList(queryWrapper);

    }


}
