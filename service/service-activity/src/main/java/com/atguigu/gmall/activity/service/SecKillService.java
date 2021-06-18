package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;

import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/11 15:46
 * @Description:
 */
public interface SecKillService {

    void seckillGoodsPublish(String skuId);

    List<SeckillGoods> findAll();

    SeckillGoods getItem(String skuId);

    Map<String,Object> seckillOrder(String userId, String skuId);

    void seckillOrder(UserRecode userRecode);

    OrderRecode checkOrderRecode(String userId);

    Map<String, Object> checkExistOrder(String userId);

    void genSeckillOrder(String userId, String orderId);
}
