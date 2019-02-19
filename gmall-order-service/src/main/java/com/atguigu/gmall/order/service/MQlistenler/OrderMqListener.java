package com.atguigu.gmall.order.service.MQlistenler;

import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderMqListener {
    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE", containerFactory = "jmsQueueListener")
    public void comsumePaymentResult(MapMessage mapMessage) {
        String out_trade_no=null;
        String payment_status=null;
        String tracking_no=null;
        try {
            out_trade_no=mapMessage.getString("out_trade_no");
            payment_status=mapMessage.getString("payment_status");
            tracking_no=mapMessage.getString("tracking_no");

        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println(out_trade_no+"已完成，进行下一步");

        //消费消息队列  更新order表中未完成列
        orderService.updateOrder(out_trade_no,payment_status,tracking_no);

        //订单完成 提供消息队列供库存消费
        orderService.sendOrderResult(out_trade_no);
    }


}
