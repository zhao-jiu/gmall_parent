package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/7 14:40
 * @Description:
 */
@RestController
@RequestMapping("/api/payment")
public class PayApiController {

    @Autowired
    PayService payService;

    @RequestMapping("/alipay/payCheck/{outTradeNo}")
    public Result payCheck(HttpServletRequest request,@PathVariable("outTradeNo") String outTradeNo){

        Map<String,Object>map = payService.payCheck(outTradeNo);

        return Result.ok(map);
    }


    /**
     * 支付宝回调跳转请求
     * @return http://payment.gmall.com/payment/success.html
     */
    @RequestMapping("/alipay/callback/return")
    public String alipay(HttpServletRequest request){

        //根据回调信息修改支付信息
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_no = request.getParameter("trade_no");
        String callback_content = request.getQueryString();

        PaymentInfo paymentInfo = new PaymentInfo();
        //外部订单号，用于修改的条件
        paymentInfo.setOutTradeNo(out_trade_no);
        //支付宝订单号
        paymentInfo.setTradeNo(trade_no);
        //回调时间
        paymentInfo.setCallbackTime(new Date());
        //回调的其他参数
        paymentInfo.setCallbackContent(callback_content);
        //修改支付状态
        paymentInfo.setPaymentStatus("TRADE_SUCCESS1");

        //幂等性检查
        String payStatus = payService.checkPayStatus(out_trade_no);

        if(!StringUtils.isEmpty(payStatus) && !payStatus.equals("TRADE_SUCCESS")){
            payService.updatePayStatus(paymentInfo);
        }


        return "<form name=\"punchout_form\" method=\"post\" action=\"http://payment.gmall.com/payment/success.html\">\n" +
                "</form>\n" +
                "<script>document.forms[0].submit();</script>";
    }

    /**
     * 异步调用支付宝接口，返回支付页面
     * @param orderId
     * @return
     */
    @RequestMapping("alipay/submit/{orderId}")
    public String tradePagePay(@PathVariable Long orderId){

        String form = payService.tradePagePay(orderId);

        PaymentInfo paymentInfo = new PaymentInfo();
        
        paymentInfo.setOrderId(orderId);
        //保存支付信息
        payService.save(paymentInfo);

        //发送延迟队列，检查支付结果
        Map<String, Object> map = new HashMap<>();
        map.put("orderId",orderId);
        map.put("count",7);

        payService.sendPayCheckQueue(map);

        return form;
    }



}
