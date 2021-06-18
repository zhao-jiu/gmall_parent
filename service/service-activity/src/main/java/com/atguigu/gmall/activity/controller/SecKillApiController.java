package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.config.CacheHelper;
import com.atguigu.gmall.activity.service.SecKillService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/11 15:42
 * @Description:
 */
@RestController
@RequestMapping("api/activity/seckill")
public class SecKillApiController {

    @Autowired
    SecKillService secKillService;


    @RequestMapping("/auth/submitOrder")
    public Result submitOrder(HttpServletRequest request){

        String userId = request.getHeader("userId");
        // 生成正式订单,调用order服务，写入订单信息
        String orderId = "1";

        //生成订单，返回订单号
        secKillService.genSeckillOrder(userId,orderId);

        return Result.ok(orderId);
    }


    @RequestMapping("/auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") String skuId, HttpServletRequest request){

        String userId = request.getHeader("userId");

        Map<String,Object> orderMap =secKillService.checkExistOrder(userId);
        String orderId = (String) orderMap.get("orderId");
        if(!StringUtils.isEmpty(orderId)){
            //下单成功
            return Result.build(null,ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }

        OrderRecode orderRecode = secKillService.checkOrderRecode(userId);
        if(orderRecode!=null){
            //抢购成功
            return Result.build(null,ResultCodeEnum.SECKILL_SUCCESS);
        }

        String status = CacheHelper.get(skuId);
        if(StringUtils.isEmpty(skuId) || "0".equals(status)){
            //商品售罄
            return Result.build(null,ResultCodeEnum.SECKILL_FINISH);
        }

        //正在排队
        return Result.build(null,ResultCodeEnum.SECKILL_RUN);
    }

    /**
     * 秒杀下单
     * @param skuId
     * @param skuIdStr
     * @param request
     * @return
     */
    @RequestMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") String skuId,String skuIdStr ,HttpServletRequest request){

        String userId = request.getHeader("userId");
        //检验合法性
        String encryptStr = MD5.encrypt(userId + skuId);

        if(encryptStr.equals(skuIdStr)){
            //去下单
            Map<String,Object> map = secKillService.seckillOrder(userId,skuId);
            String isSuccess = (String) map.get("isSuccess");
            if(isSuccess.equals("isSuccess")){
                return Result.ok();
            }else {
                return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
            }

        }else {
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }

    }


    @RequestMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable("skuId") String skuId, HttpServletRequest request){

        String userId = request.getHeader("userId");

        String encryptStr = MD5.encrypt(userId + skuId);

        return Result.ok(encryptStr);
    }

    @RequestMapping("getStatus/{skuId}")
    String getStatus(@PathVariable("skuId") String skuId){
        return CacheHelper.get(skuId);
    }

    @RequestMapping("getItem/{skuId}")
    SeckillGoods getItem(@PathVariable("skuId") String skuId){
        return secKillService.getItem(skuId);
    }

    @RequestMapping("findAll")
    List<SeckillGoods> findAll(){

        List<SeckillGoods> seckillGoods = secKillService.findAll();

        return seckillGoods;
    }

    @RequestMapping("testStatus/{skuId}")
    public Result testStatus(@PathVariable("skuId") String skuId){

        return Result.ok(CacheHelper.get(skuId));
    }

    @RequestMapping("seckillGoodsPublish/{skuId}")
    public Result seckillGoodsPublish(@PathVariable("skuId") String skuId){

        secKillService.seckillGoodsPublish(skuId);

        return Result.ok();
    }
}
