package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.conf.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentInfoService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Controller
public class paymentController {
    @Autowired
    AlipayClient alipayClient;
    @Reference
    OrderService orderService;
    @Autowired
    PaymentInfoService paymentInfoService;

    @RequestMapping("/alipay/callback/return")
    public String callbackReturn(HttpServletRequest request, Map<String, String> paramsMap) {
        //阿里回调检验
//        String sign = request.getParameter("sign");
//        try {
//            boolean a = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);// 对支付宝回调签名的校验
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
        //---------------------------------------------
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_no = request.getParameter("trade_no");
        //修改支付信息
        //幂等性检查
       boolean b= paymentInfoService.checkPaymentStatus(out_trade_no);
        if (!b){
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setOutTradeNo(out_trade_no);
            paymentInfo.setCallbackContent(request.getQueryString());
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfoService.updatePayment(paymentInfo);
            //发送消息队列    发送系统消息，出发并发商品支付业务服务消息队列
            paymentInfoService.sendPaymentSuccess(paymentInfo.getOutTradeNo(),paymentInfo.getPaymentStatus(),trade_no);
        }

        return "finish";
    }

    @LoginRequired(isNeedLogin = true)
    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String getToPay(HttpServletRequest request, String outTradeNo, BigDecimal totalAmount, Model model) {
        OrderInfo orderInfo = orderService.getOrderInfoByOutTradeNo(outTradeNo);
        String skuName = orderInfo.getOrderDetailList().get(0).getSkuName();

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("out_trade_no", outTradeNo);
        requestMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        requestMap.put("total_amount", "0.01");
        requestMap.put("subject", skuName);

        alipayRequest.setBizContent(JSON.toJSONString(requestMap));//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //保存支付信息到db
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setSubject(skuName);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfoService.save(paymentInfo);

        //发送检查支付结果的消息延迟队列
        paymentInfoService.sendDelayPaymentCheck(outTradeNo,5);

        return form;
    }


    @LoginRequired(isNeedLogin = true)
    @RequestMapping("paymentIndex")
    public String paymentIndex(HttpServletRequest request, String outTradeNo, BigDecimal totalAmount, Model model) {
        String userId = request.getParameter("userId");
        model.addAttribute("outTradeNo", outTradeNo);
        model.addAttribute("totalAmount", totalAmount);

        return "paymentindex";
    }


}
