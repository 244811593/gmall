package com.atguigu.gmall.payment.paymentListener;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;

@Component
public class delayCheckListener {

    @Autowired
    PaymentInfoService paymentInfoService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeCheckResult(MapMessage mapMessage) {

        try {
            String out_trade_no = mapMessage.getString("out_trade_no");
            int count = mapMessage.getInt("count");

                if (count>0) {
                    //进行支付状态检验
                    System.out.println("正在进行第" + (6 - count) + "次支付状态检验");
                    PaymentInfo paymentInfo=paymentInfoService.checkPaymentResult(out_trade_no);

                    if (paymentInfo.getPaymentStatus()!=null &&( paymentInfo.getPaymentStatus().equals("TRADE_SUCCESS") || paymentInfo.getPaymentStatus().equals("TRADE_FINISHED"))){
                        //支付成功或订单完成
                        //修改支付信息
                        //幂等性检查
                        boolean b= paymentInfoService.checkPaymentStatus(out_trade_no);
                        if (!b){
                            PaymentInfo paymentInfoUpdate = new PaymentInfo();
                            paymentInfo.setCallbackTime(new Date());
                            paymentInfo.setOutTradeNo(out_trade_no);
                            paymentInfo.setCallbackContent(paymentInfo.getCallbackContent());
                            paymentInfo.setAlipayTradeNo(paymentInfo.getAlipayTradeNo());
                            paymentInfoService.updatePayment(paymentInfoUpdate);
                            //发送消息队列    发送系统消息，出发并发商品支付业务服务消息队列
                            paymentInfoService.sendPaymentSuccess(paymentInfo.getOutTradeNo(),paymentInfo.getPaymentStatus(),paymentInfo.getAlipayTradeNo());
                        }

                    }else {
                        //再次检验
                        System.out.println("正在进行第" + (6 - count) + "支付结果次检查，检查用户尚未付款成功，继续检测");
                        paymentInfoService.sendDelayPaymentCheck(out_trade_no,count-1);
                    }

                } else {
                    //检查次数耗尽仍未成功
                    System.out.println("检查次数耗尽仍未成功");
                }

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
