package com.atguigu.gmall.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.activity.config.CacheHelper;
import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SecKillService;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/11 15:47
 * @Description:
 */
@Service
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitService rabbitService;

    @Override
    public void seckillGoodsPublish(String skuId) {

        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        SeckillGoods seckillGoods = seckillGoodsMapper.selectOne(queryWrapper);

        if (seckillGoods != null && seckillGoods.getNum() > 0) {
            for (int i = 0; i < seckillGoods.getNum(); i++) {
                redisTemplate.opsForList().leftPush("seckill:stock:", seckillGoods.getSkuId());
            }
            //存储库存信息
            redisTemplate.boundHashOps("seckill:goods").put(seckillGoods.getSkuId() + "", seckillGoods);
            //发布库存状态
            redisTemplate.convertAndSend("seckillpush", seckillGoods.getSkuId() + ":1");
        }

    }

    @Override
    public List<SeckillGoods> findAll() {

        List<SeckillGoods> seckillGoods = (List<SeckillGoods>) redisTemplate.boundHashOps("seckill:goods").values();

        return seckillGoods;
    }

    @Override
    public SeckillGoods getItem(String skuId) {

        SeckillGoods seckillGood = (SeckillGoods) redisTemplate.boundHashOps("seckill:goods").get(skuId);

        return seckillGood;
    }

    @Override
    public Map<String,Object> seckillOrder(String userId, String skuId) {
        Map<String,Object> map =new HashMap<>();
        //加入分布式锁 削峰
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("seckill:user:" + userId, uuid, 10, TimeUnit.SECONDS);
        String isSuccess = "isSuccess";
        if(lock){
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(Long.parseLong(skuId));
            //发送抢购消息队列
            rabbitService.sendMessage("exchange.direct.seckill.user","seckill.user", JSON.toJSONString(userRecode));
            map.put("isSuccess","isSuccess");
        }else {
            map.put("isSuccess","fail");
        }
        return map;
    }

    @Override
    public void seckillOrder(UserRecode userRecode) {
        String userId = userRecode.getUserId();

        //抢购商品
        Integer num = (Integer) redisTemplate.opsForList().rightPop("seckill:stock:");

        if(num!=null){
            //商品未售罄
            OrderRecode orderRecode = new OrderRecode();
            orderRecode.setNum(1);
            orderRecode.setUserId(userId);
            //获取抢购商品数据
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.opsForHash().get("seckill:goods", userRecode.getSkuId()+"");
            orderRecode.setSeckillGoods(seckillGoods);
            //生成预订单
            redisTemplate.opsForHash().put("seckill:orders",userId+"",orderRecode);
        }else {
            // 抢购失败，库存售罄
            redisTemplate.convertAndSend("seckillpush",userRecode.getSkuId()+":0");
        }
    }


    //查询是否抢购成功
    @Override
    public OrderRecode checkOrderRecode(String userId) {
        OrderRecode orderRecode = (OrderRecode) redisTemplate.opsForHash().get("seckill:orders",userId+"");
        return orderRecode;
    }

    //查询是否已经下单
    @Override
    public Map<String, Object> checkExistOrder(String userId) {
        String orderId = (String) redisTemplate.opsForHash().get("seckill:orders:users", userId+"");
        Map<String, Object> map = new HashMap<>();
        map.put("orderId",orderId);
        return map;
    }

    @Override
    public void genSeckillOrder(String userId, String orderId) {

        //正式生成订单，存储订单号
        redisTemplate.opsForHash().put("seckill:orders:users",userId+"",orderId);

        //删除预订单
        redisTemplate.opsForHash().delete("seckill:orders",userId);
    }


}
