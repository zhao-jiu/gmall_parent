package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SkuService;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.atguigu.gmall.search.client.SearchFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/19 2:15
 * @Description:
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    SearchFeignClient searchFeignClient;

    @Autowired
    RabbitService rabbitService;

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //保存sku
        skuInfoMapper.insert(skuInfo);
        Long skuId = skuInfo.getId();

        //添加image中间表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insert(skuImage);
        }

        //添加平台属性中间表
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }

        //添加销售属性中间表
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }
    }

    /**
     * 下架
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        //修改Mysql中的信息
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        //  同步数据的到电商搜索引擎
       // searchFeignClient.cancelSale(skuId);

        //发送mq消息异步通知es下架商品
        rabbitService.sendMessage("exchange.direct.goods","goods.lower", JSON.toJSONString(skuId));
    }

    /**
     * 上架
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        //修改Mysql中的信息
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);

        // 同步数据的到电商搜索引擎
       // searchFeignClient.onSale(skuId);

        //发送mq消息异步通知es上架商品
        rabbitService.sendMessage("exchange.direct.goods","goods.upper",JSON.toJSONString(skuId));
    }

    //缓存
    //分布式锁测试
    @GmallCache
    @Override
    public SkuInfo getSkuInfoById(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

        return skuInfo;
    }

    @GmallCache
    @Override
    public List<SkuImage> getImageBySkuId(Long skuId) {
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(queryWrapper);

        return skuImageList;
    }

    @Override
    public BigDecimal getSkuPriceBySkuId(Long skuId) {
        return skuInfoMapper.selectById(skuId).getPrice();
    }

    @GmallCache
    @Override
    public List<Map<String, Object>> getSaleAttrValuesBySpu(Long spuId) {
        return  skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
    }

    @Override
    public List<SearchAttr> getSearchAttrList(Long skuId) {
        return   baseAttrInfoMapper.getSearchAttrList(skuId);
    }

    @Override
    public IPage<SkuInfo> list(Long page, Long limit) {
        IPage<SkuInfo> skuInfoIPage = new Page<>(page,limit);

        skuInfoMapper.selectPage(skuInfoIPage,null);

        return skuInfoIPage;
    }

    public SkuInfo getSkuInfoByIdBack(Long skuId) {
        //从redis中查询数据
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX);

        if (skuInfo == null) {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            //加入分布式锁
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX, uuid, 1, TimeUnit.SECONDS);

            if (ok) {//上锁成功
                // 查数据库
                skuInfo = skuInfoMapper.selectById(skuId);
                if (skuInfo != null) {
                    //存储到redis中
                    redisTemplate.opsForValue().set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX, skuInfo);
                }else{
                    //如果取不存在的key则加入缓存请求等待10秒
                    redisTemplate.opsForValue().set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX, new SkuInfo(),10,TimeUnit.SECONDS);
                }

//                // 释放锁
//               String uuidCheck = (String) redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX);
//                if(StringUtils.isNotEmpty(uuid) && uuid.equals(uuidCheck)){
//                redisTemplate.delete(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX);
//                }

                // lua脚本释放锁  保证删除的原子性
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 设置lua脚本返回的数据类型
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                // 设置lua脚本返回类型为Long
                redisScript.setResultType(Long.class);
                redisScript.setScriptText(script);
                redisTemplate.execute(redisScript, Arrays.asList("sku:" + skuId + ":lock"),uuid);

            } else {
                //没有拿到锁
                //自旋
                return getSkuInfoById(skuId);
            }
        }
        return skuInfo;
    }
}
