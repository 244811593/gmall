package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface OrderService {
    void genTradeCode(String userId, String tradeCode);

    boolean checkTradeCode(String userId, String tradeCode);

    void saveOrder(OrderInfo orderInfo);

    OrderInfo getOrderInfoByOutTradeNo(String outTradeNo);

   void updateOrder(String out_trade_no,String payment_status,String tracking_no);

    void sendOrderResult(String out_trade_no);
}
