package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.PaymentWay;
import com.atguigu.gmall.conf.ActiveMQUtil;
import com.atguigu.gmall.order.service.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.service.mapper.OrderMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Override
    public void genTradeCode(String userId, String tradeCode) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:"+userId+":tradeCode",60*24,tradeCode);
        jedis.close();
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {
        boolean result=false;
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeFromCache = jedis.get("user:" + userId + ":tradeCode");
        if (tradeCodeFromCache!=null&&tradeCodeFromCache.equals(tradeCode)){
            result=true;
            jedis.del("user:" + userId + ":tradeCode");
        }
        return result;

    }

    @Override
    public void saveOrder(OrderInfo orderInfo) {

        orderMapper.insertSelective(orderInfo);
        String orderInfoId = orderInfo.getId();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfoId);
            orderDetailMapper.insertSelective(orderDetail);

        }
    }

    @Override
    public OrderInfo getOrderInfoByOutTradeNo(String outTradeNo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo = orderMapper.selectOne(orderInfo);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo.getId());
        List<OrderDetail> select = orderDetailMapper.select(orderDetail);

        orderInfo.setOrderDetailList(select);

        return orderInfo;
    }

    @Override
    public void updateOrder(String out_trade_no, String payment_status, String tracking_no) {
        //修改支付状态 支付方式 交易状态 订单状态 支付宝号
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus("订单已支付");
        orderInfo.setProcessStatus("订单已支付");
        orderInfo.setPaymentWay(PaymentWay.ONLINE);
        orderInfo.setTrackingNo(tracking_no);
        orderMapper.updateByExample(orderInfo,example);

    }

    @Override
    public void sendOrderResult(String out_trade_no) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(out_trade_no);
        OrderInfo order = orderMapper.selectOne(orderInfo);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(order.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        order.setOrderDetailList(orderDetailList);

        //订单完成 提供消息队列供库存消费
        try {
            // 连接消息服务器
            ConnectionFactory connect = activeMQUtil.getConnectionFactory();
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            // 发送消息
            Queue testqueue = session.createQueue("ORDER_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();

            textMessage.setText(JSON.toJSONString(order));

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();// 事务型消息，必须提交后才生效
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
