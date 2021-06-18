package com.atguigu.gmall.user.client;

import com.atguigu.gmall.model.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/1 14:36
 * @Description:
 */
@FeignClient(value = "service-user")
public interface UserFeignClient {

    @RequestMapping("api/user/passport/verify/{token}")
    Map<String, Object> verify(@PathVariable("token") String token);

    @RequestMapping("api/user/passport/getUserAddressByUserId/{userId}")
    List<UserAddress> getUserAddressByUserId(@PathVariable("userId") String userId);
}
