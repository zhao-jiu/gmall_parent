package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/28 18:19
 * @Description:
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RedisTemplate redisTemplate;

    /***
     * 添加购物车
     * @param skuId 商品id
     * @param skuNum 商品数量
     * @param userId 用户id
     * @return 购物车信息数据
     */
    @Override
    public CartInfo addCart(Long skuId, Integer skuNum, String userId) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuNum(skuNum);
        //通过skuId查询sku数据
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        //获取最新的price
        BigDecimal priceBySkuId = productFeignClient.getSkuPriceBySkuId(skuId);
        cartInfo.setSkuPrice(priceBySkuId);
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(skuNum)));
        cartInfo.setSkuName(skuInfo.getSkuName());
        //添加到数据库中
        cartInfo = addToDb(cartInfo);

        //同步redis缓存
        redisTemplate.opsForHash().put("user:"+userId+":cart",skuId+"",cartInfo);

        return cartInfo;
    }

    /**
     * 添加或修改数据到数据库中备份数据
     * @param cartInfo
     * @return
     */
    private CartInfo addToDb(CartInfo cartInfo) {
        String userId = cartInfo.getUserId();
        Long skuId = cartInfo.getSkuId();

        //添加和修改一起 查询数据库
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("sku_id", skuId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(queryWrapper);

        if (cartInfoExist != null) {
            //修改购物车信息
            Integer skuNum = cartInfo.getSkuNum() + cartInfoExist.getSkuNum();
            cartInfoExist.setSkuNum(skuNum);
            BigDecimal skuPrice = cartInfo.getSkuPrice();
            cartInfoExist.setCartPrice(skuPrice.multiply(new BigDecimal(skuNum)));
            cartInfoMapper.updateById(cartInfoExist);

            //将值赋给cartInfo存储最新的到redis中
            cartInfo.setId(cartInfoExist.getId());
            cartInfo.setSkuNum(cartInfoExist.getSkuNum());
            cartInfo.setCartPrice(cartInfoExist.getCartPrice());
            cartInfo.setSkuPrice(skuPrice);
        } else {
            //添加到数据库中
            cartInfoMapper.insert(cartInfo);
        }
        return cartInfo;
    }

    /**
     * 购物车列表查询
     */
    @Override
    public List<CartInfo> cartList(String userId) {
        //优先查询缓存
        List<CartInfo> cartInfos = (List<CartInfo>) redisTemplate.opsForHash().values("user:" + userId + ":cart");

        if (cartInfos.size() == 0) {
            //从数据库拿到中备份数据
            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            List<CartInfo> cartInfoBack = cartInfoMapper.selectList(queryWrapper);

            //同步存储到redis中
            Map<String, Object> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfoBack) {
                BigDecimal skuPriceBySkuId = productFeignClient.getSkuPriceBySkuId(cartInfo.getSkuId());
                cartInfo.setSkuPrice(skuPriceBySkuId);
                map.put(cartInfo.getSkuId() + "", cartInfo);
            }
            //备份回到redis中
            redisTemplate.opsForHash().putAll("user:" + userId + ":cart", map);
        }
        return cartInfos;
    }

    //修改选中状态
    @Override
    public void checkCart(Long skuId, Integer isChecked, String userId) {

        //从redis中查询数据
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get("user:"+userId+":cart",skuId+"");

        if(cartInfo!=null){
            //修改缓存中的选中状态
            cartInfo.setIsChecked(isChecked);
            redisTemplate.opsForHash().put("user:"+userId+":cart",skuId+"",cartInfo);
        }

        if(cartInfo!=null){
            //同步数据库
            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sku_id",skuId);
            queryWrapper.eq("user_id",userId);
            CartInfo cartInfo1 = cartInfoMapper.selectOne(queryWrapper);
            cartInfo1.setIsChecked(isChecked);
            cartInfoMapper.updateById(cartInfo1);
        }
    }

    @Override
    public List<CartInfo> getCartInfoByUser(String userId) {

        List<CartInfo> cartInfos = (List<CartInfo>) redisTemplate.opsForHash().values("user:" + userId + ":cart");

        return cartInfos;
    }


}
