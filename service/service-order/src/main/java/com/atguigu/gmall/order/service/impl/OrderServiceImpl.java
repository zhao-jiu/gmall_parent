package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.client.CartFeignCilent;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.ware.WareOrderTask;
import com.atguigu.gmall.model.ware.WareOrderTaskDetail;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Ordering;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/6 14:39
 * @Description:
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartFeignCilent cartFeignCilent;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RabbitService rabbitService;

    @Override
    public List<OrderDetail> getDetailList(String userId) {

        List<OrderDetail> orderDetails = new ArrayList<>();
        //查询到的购物车数据
        List<CartInfo> cartInfos = cartFeignCilent.getCartInfoByUser(userId);

        if(cartInfos!=null && cartInfos.size()>0){
            //封装数据
            for (CartInfo cartInfo : cartInfos) {
                if(cartInfo.getIsChecked()==1){
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setSkuId(cartInfo.getSkuId());
                    orderDetail.setSkuName(cartInfo.getSkuName());
                    orderDetail.setSkuNum(cartInfo.getSkuNum());
                    orderDetail.setImgUrl(cartInfo.getImgUrl());
                    orderDetail.setOrderPrice(cartInfo.getCartPrice());

                    orderDetails.add(orderDetail);
                }

            }
        }

        return orderDetails;
    }

    /**
     *  提交订单，生成订单信息
     * @param userId 用户id
     * @param orderInfo 订单信息，页面传递
     *
     */
    @Override
    public Boolean submitOrder(String userId, OrderInfo orderInfo) {
        //检验价格和库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        BigDecimal totalAmount = getTotalAmount(orderDetailList);

        //价格或库存信息变动，返回false
        if(null==totalAmount){
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        //保存订单
        orderInfo.setUserId(Long.parseLong(userId));
        //订单总价
        orderInfo.setTotalAmount(totalAmount);
        //订单创建时间
        orderInfo.setCreateTime(new Date());
        //设置订单超时
        orderInfo.setExpireTime(calendar.getTime());
        //订单图片地址
        orderInfo.setImgUrl(orderDetailList.get(0).getImgUrl());
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
        //交易状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
        orderInfo.setOrderComment("商品详情信息");
        //外部交易号，用户支付宝支付
        orderInfo.setOutTradeNo("gmall"+System.currentTimeMillis()+new Random().nextInt(1000));
        //添加订单表信息
        orderInfoMapper.insert(orderInfo);

        //添加订单详情表信息
        Long orderId = orderInfo.getId();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insert(orderDetail);

            //删除购物车对应的sku信息，参数userId，skuId
        }
        return true;
    }

    /**
     * 校验价格和库存信息，返回商品总金额
     * @param orderDetailList
     * @return
     */
    private BigDecimal getTotalAmount(List<OrderDetail> orderDetailList) {
        BigDecimal totalAmount = new BigDecimal("0");

        if(orderDetailList!=null &&orderDetailList.size()>0){
            for (OrderDetail orderDetail : orderDetailList) {
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                BigDecimal skuPrice = orderPrice.divide(new BigDecimal(orderDetail.getSkuNum() + ""));

                //调用商品服务查询当前商品价格
                BigDecimal skuPriceCurrent = productFeignClient.getSkuPriceBySkuId(orderDetail.getSkuId());
                //比较价格信息
                int i = skuPrice.compareTo(skuPriceCurrent);
                //校验价格
                if(i!=0){
                    return null;
                }

                //校验库存,调用库存系统
                if(1==0){
                    return null;
                }
                totalAmount = totalAmount.add(orderPrice);
            }
        }
        return totalAmount;
    }

    @Override
    public String genTradeNo(String userId) {

        //生成tradeNo ,存储的到redis中用于校验数据，帮助表单的重复提交
        String tradeCode = UUID.randomUUID().toString().replaceAll("-", "");

        redisTemplate.opsForValue().set("user:"+userId+":tradeCode",tradeCode,30, TimeUnit.MINUTES);

        return tradeCode;
    }

    @Override
    public Boolean checkTradeNo(String tradeNo, String userId) {

        //从redis中获取tradeCode进行校验
        String result = (String) redisTemplate.opsForValue().get("user:"+userId+":tradeCode");

        if(!StringUtils.isEmpty(result)){
            //判断tradeNo
            redisTemplate.delete("user:"+userId+":tradeCode");
            return true;
        }else {
            return false;
        }

    }

    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);

        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("order_id",orderId);

        List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper);

        if(orderDetails!=null&&orderDetails.size()>0){
            orderInfo.setOrderDetailList(orderDetails);
        }

        return orderInfo;
    }

    @Override
    public void updateStatus(OrderInfo orderInfo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",orderInfo.getOutTradeNo());
        orderInfoMapper.update(orderInfo,queryWrapper);
    }


    @Override
    public void sendWareStockMessage(OrderInfo orderInfo) {
        //拆单
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",orderInfo.getOutTradeNo());
        OrderInfo orderInfoDb = orderInfoMapper.selectOne(queryWrapper);

        if(orderInfoDb!=null){
            WareOrderTask wareOrderTask = new WareOrderTask();
            wareOrderTask.setConsignee(orderInfoDb.getConsignee());
            wareOrderTask.setConsigneeTel(orderInfoDb.getConsigneeTel());
            wareOrderTask.setCreateTime(new Date());
            wareOrderTask.setDeliveryAddress(orderInfoDb.getDeliveryAddress());
            wareOrderTask.setPaymentWay(orderInfoDb.getPaymentWay());
            wareOrderTask.setTrackingNo(orderInfoDb.getTrackingNo());
            wareOrderTask.setOrderComment(orderInfoDb.getOrderComment());

            QueryWrapper<OrderDetail> detailQueryWrapper = new QueryWrapper<>();
            detailQueryWrapper.eq("order_id",orderInfoDb.getId());
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailQueryWrapper);

            List<WareOrderTaskDetail> wareOrderTaskDetails = new ArrayList<>();
            for (OrderDetail orderDetail : orderDetails) {
                WareOrderTaskDetail wareOrderTaskDetail = new WareOrderTaskDetail();
                wareOrderTaskDetail.setSkuId(orderDetail.getSkuId()+"");
                wareOrderTaskDetail.setSkuName(orderDetail.getSkuName());
                wareOrderTaskDetail.setSkuNum(orderDetail.getSkuNum());
                wareOrderTaskDetails.add(wareOrderTaskDetail);
            }
            wareOrderTask.setDetails(wareOrderTaskDetails);
            //发送消息通知库存系统
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,MqConst.ROUTING_WARE_STOCK, JSON.toJSONString(wareOrderTask));
        }

    }

    @Override
    public Page<OrderInfo> orderPage(Long pageNo, Long pageSize) {

        Page<OrderInfo> page = new Page<>(pageNo,pageSize);

        orderInfoMapper.selectPage(page,null);

        List<OrderInfo> records = page.getRecords();
        for (OrderInfo record : records) {
            Long orderId = record.getId();
            QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id",orderId);
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper);
            record.setOrderDetailList(orderDetails);
        }

        return page;
    }
}
