package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.seckill.client.SecKillFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/11 18:25
 * @Description:
 */
@Controller
public class SecKillController {

    @Autowired
    SecKillFeignClient secKillFeignClient;

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    UserFeignClient userFeignClient;


    @RequestMapping("myOrder.html")
    public String myOrder(Model model, HttpServletRequest request) {

        return "order/myOrder";
    }

    @RequestMapping("seckill/trade.html")
    public String trade(Model model, HttpServletRequest request) {

        String userId = request.getHeader("userId");

        List<UserAddress> addresses = userFeignClient.getUserAddressByUserId(userId);

        model.addAttribute("userAddressList",addresses);

        return "seckill/trade";
    }

    /**
     * 立即抢购按钮，跳转静态页面
     * @param skuId
     * @param skuIdStr
     * @param model
     * @param request
     * @return
     */
    @RequestMapping("/seckill/queue.html")
    public String queue(String skuId, String skuIdStr, Model model, HttpServletRequest request) {
        String userId = request.getHeader("userId");
        String encryptStr = MD5.encrypt(userId + skuId);

        String status = secKillFeignClient.getStatus(skuId);
        //查询该用户是否已抢购 一个用户只能抢购一次

        //查询用户是否已生成订单，

        if (encryptStr.equals(skuIdStr)) {
            //校验订单是否已售罄
            if(StringUtils.isEmpty(status)||"0".equals(status)){
                model.addAttribute("message", "商品已售罄");
                return "seckill/fail";
            }
            //校验合法
            model.addAttribute("skuId", skuId);
            model.addAttribute("skuIdStr", skuIdStr);
            return "seckill/queue";
        } else {
            model.addAttribute("message", "请求不合法");
            return "seckill/fail";
        }
    }


    /**
     * 秒杀商品详情页
     *
     * @param model
     * @param skuId
     * @return
     */
    @RequestMapping("seckill/{skuId}.html")
    public String getItem(Model model, @PathVariable("skuId") String skuId) {

        SeckillGoods seckillGood = secKillFeignClient.getItem(skuId);

        model.addAttribute("item", seckillGood);

        return "seckill/item";

    }

    /**
     * 秒杀商品列表页
     *
     * @param model
     * @return
     */
    @RequestMapping("seckill.html")
    public String index(Model model) {

        List<SeckillGoods> seckillGoods = secKillFeignClient.findAll();

        model.addAttribute("list", seckillGoods);

        return "seckill/index";
    }
}
