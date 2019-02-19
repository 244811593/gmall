package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.bean.enums.PaymentWay;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class OrderController {
    @Reference
    CartInfoService cartInfoService;
    @Reference
    UserInfoService userInfoService;
    @Reference
    OrderService orderService;
    @Reference
    SkuInfoService skuInfoService;

    //提交订单
    //                                地址id               交易码 防止重复提交订单
    @LoginRequired(isNeedLogin = true)
    @RequestMapping("submitOrder")
    public String submitOrder(String deliveryAddressId, String tradeCode, HttpServletRequest request, HttpServletResponse response, ModelMap map) {
        String userId = (String) request.getAttribute("userId");
        //根据地址id查询地址信息
        UserAddress userAddress = userInfoService.getUserAddressById(deliveryAddressId);
        boolean result = orderService.checkTradeCode(userId, tradeCode);
        if (result) {
            //对订单封装
            List<CartInfo> cartInfoListFromCache = cartInfoService.cartListFromCache(userId);
            OrderInfo orderInfo = new OrderInfo();

            orderInfo.setProcessStatus("订单已提交");
            orderInfo.setOrderStatus("订单已提交");
            String outTradeNo = "atguigu" + userId;
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(date);
            outTradeNo = outTradeNo + format + System.currentTimeMillis();
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setPaymentWay(PaymentWay.ONLINE);
            orderInfo.setTotalAmount(getMySum(cartInfoListFromCache));
            orderInfo.setOrderComment("尚硅谷商城");
            orderInfo.setDeliveryAddress(userAddress.getUser_address());
            orderInfo.setCreateTime(new Date());
            orderInfo.setConsignee(userAddress.getConsignee());
            orderInfo.setConsigneeTel(userAddress.getPhoneNum());

            //过期时间  当前日期加一天
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            orderInfo.setExpireTime(c.getTime());

            //对订单详情封装
            List<OrderDetail> orderDetailList = new ArrayList<>();
            for (CartInfo cartInfoFromCache : cartInfoListFromCache) {

                if (cartInfoFromCache.getIsChecked().equals("1")) {
                    //一致性检验 价格 库存

                    //通过缓存中skuId查询DB中的skuInfo信息 对比价格 库存
                    SkuInfo skuInfoBySkuId = skuInfoService.getSkuInfoBySkuId(cartInfoFromCache.getSkuId());
                    if (skuInfoBySkuId.getPrice().compareTo(cartInfoFromCache.getSkuPrice()) == 0) {
                        OrderDetail orderDetail = new OrderDetail();
                        //价格一致
                        orderDetail.setSkuNum(cartInfoFromCache.getSkuNum());
                        orderDetail.setImgUrl(cartInfoFromCache.getImgUrl());
                        orderDetail.setOrderPrice(cartInfoFromCache.getCartPrice());
                        orderDetail.setSkuId(cartInfoFromCache.getSkuId());
                        orderDetail.setSkuName(cartInfoFromCache.getSkuName());

                        orderDetailList.add(orderDetail);
                    } else {
                        return "tradeFail";
                    }
                }
            }
            orderInfo.setOrderDetailList(orderDetailList);
            //保存订单
            orderService.saveOrder(orderInfo);
            //删除购物车中已经提交了的商品
            return "redirect:http://payment.gmall.com:8090/paymentIndex?outTradeNo="+outTradeNo+"&totalAmount="+getMySum(cartInfoListFromCache);
        } else {
            return "tradeFail";
        }
    }

    //去结算
    @LoginRequired(isNeedLogin = true)
    @RequestMapping("toTrade")
    public String toTride(HttpServletRequest request, HttpServletResponse response, Model model) {
        String userId = request.getParameter("userId");
        //从缓存中拿cartList
        List<CartInfo> cartInfoList = cartInfoService.cartListFromCache(userId);
        //将cartList转换成order   orderDetailList
        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (CartInfo cartInfo : cartInfoList) {
            if (cartInfo.getIsChecked().equals("1")) {
                //购物车被选中商品
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setHasStock("1");
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetailList.add(orderDetail);
            }
        }
        //用户地址信息 userAddressList
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);

        model.addAttribute("orderDetailList", orderDetailList);
        model.addAttribute("userAddressList", userAddressList);
        model.addAttribute("totalAmount", getMySum(cartInfoList));


        //生成交易码 存入缓存
        String tradeCode = UUID.randomUUID().toString();
        model.addAttribute("tradeCode", tradeCode);
        orderService.genTradeCode(userId, tradeCode);
        return "trade";
    }

    //计算购物车总价格
    private BigDecimal getMySum(List<CartInfo> cartList) {
        BigDecimal b = new BigDecimal("0");
        for (CartInfo cartInfo : cartList) {
            String isChecked = cartInfo.getIsChecked();
            if (isChecked.equals("1")) {
                b.add(cartInfo.getCartPrice());
            }
        }
        return b;
    }

}
