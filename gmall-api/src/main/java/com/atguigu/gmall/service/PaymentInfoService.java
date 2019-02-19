package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentInfoService {
    void save(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendPaymentSuccess(String outTradeNo, String paymentStatus,String trackingNo);

    void sendDelayPaymentCheck(String outTradeNo, int i);

    PaymentInfo checkPaymentResult(String out_trade_no);

    boolean checkPaymentStatus(String out_trade_no);
}
