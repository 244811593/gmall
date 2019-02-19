package com.atguigu.gmall.payment.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.conf.ActiveMQUtil;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentInfoService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void save(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", paymentInfo.getOutTradeNo());
        paymentInfoMapper.updateByExample(paymentInfo, example);
    }

    @Override
    public void sendPaymentSuccess(String outTradeNo, String paymentStatus, String trackingNo) {

        try {
            ConnectionFactory connect = activeMQUtil.getConnectionFactory();
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no", outTradeNo);
            mapMessage.setString("payment_status", paymentStatus);
            mapMessage.setString("tracking_no", trackingNo);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();// 事务型消息，必须提交后才生效
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    //发送延迟队列
    @Override
    public void sendDelayPaymentCheck(String outTradeNo, int count) {
        try {
            // 连接消息服务器
            ConnectionFactory connect = activeMQUtil.getConnectionFactory();
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            // 发送消息
            Queue testqueue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            //设置延迟时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 30);

            mapMessage.setString("out_trade_no", outTradeNo);
            mapMessage.setInt("count", count);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();// 事务型消息，必须提交后才生效
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    //检测支付结果
    @Override
    public PaymentInfo checkPaymentResult(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();


        // 调用alipayClient接口，根据out_trade_no查询支付状态
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        Map<String, Object> mapString = new HashMap<String, Object>();
        mapString.put("out_trade_no", out_trade_no);
        String s = JSON.toJSONString(mapString);
        request.setBizContent(s);

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentInfo.setCallbackContent(response.getMsg());

        if (response.isSuccess()) {
            System.out.println("交易已经创建");
            // 等待付款、付款成功、付款失败、已经结束
            paymentInfo.setPaymentStatus(response.getTradeStatus());

            paymentInfo.setAlipayTradeNo(response.getTradeNo());
        } else {
            System.out.println("交易未创建");
        }


        return paymentInfo;
    }

    //幂等性检查
    @Override
    public boolean checkPaymentStatus(String out_trade_no) {
        boolean b = false;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);

        if (paymentInfo1.getPaymentStatus().equals("已支付")) {
            b = true;
        }
        return b;
    }
}
