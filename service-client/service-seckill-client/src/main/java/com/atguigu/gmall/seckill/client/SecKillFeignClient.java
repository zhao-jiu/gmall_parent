package com.atguigu.gmall.seckill.client;

import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/11 18:42
 * @Description:
 */
@FeignClient(value = "service-activity")
public interface SecKillFeignClient {

    @RequestMapping("api/activity/seckill/findAll")
    List<SeckillGoods> findAll();

    @RequestMapping("api/activity/seckill/getItem/{skuId}")
    SeckillGoods getItem(@PathVariable("skuId") String skuId);

    @RequestMapping("api/activity/seckill/getStatus/{skuId}")
    String getStatus(@PathVariable("skuId") String skuId);
}
