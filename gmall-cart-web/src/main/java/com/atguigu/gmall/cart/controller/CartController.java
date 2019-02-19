package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuInfoService skuInfoService;
    @Reference
    CartInfoService cartInfoService;



    //购物车列表勾选框
    @LoginRequired(isNeedLogin = false)
    @RequestMapping("checkCart")
    public String checkCart(CartInfo cartInfo, HttpServletRequest request, HttpServletResponse response, Model model) {
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {
            cartList = cartInfoService.cartListFromCache(userId);
        } else {
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartList = JSON.parseArray(listCartCookie, CartInfo.class);
        }
        // 修改购物车状态
        for (CartInfo info : cartList) {
            if (info.getSkuId().equals(cartInfo.getSkuId())) {
                info.setIsChecked(cartInfo.getIsChecked());
                if (StringUtils.isNotBlank(userId)) {
                    cartInfoService.updateCartInfo(info);
                    // 刷新缓存
                    cartInfoService.flushCartCacheByUser(userId);
                } else {
                    // 覆盖cookie
                    CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(cartList), 1000 * 60 * 60 * 24, true);
                }
            }
        }
        model.addAttribute("cartList", cartList);
        model.addAttribute("totalPrice", getMySum(cartList));

        return "cartListInner";

    }

    //购物车列表
    @LoginRequired(isNeedLogin = false)
    @RequestMapping("cartList")
    public String cartList(Model model, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {
            cartList = cartInfoService.cartListFromCache(userId);
        } else {
            //从cookie拿购物车
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            if (StringUtils.isNotBlank(listCartCookie)) {
                cartList = JSON.parseArray(listCartCookie, CartInfo.class);
            }
        }
        model.addAttribute("cartList", cartList);
        BigDecimal mySum = getMySum(cartList);
        model.addAttribute("totalPrice",mySum );

        return "cartList";
    }

    //计算购物车总价格
    private BigDecimal getMySum(List<CartInfo> cartList) {
        BigDecimal b = new BigDecimal("0");
        for (CartInfo cartInfo : cartList) {
            String isChecked = cartInfo.getIsChecked();
            if (isChecked.equals("1")) {
                b= b.add(cartInfo.getCartPrice());
            }
        }
        return b;
    }

    //添加到购物车
    @LoginRequired(isNeedLogin = false)
    @RequestMapping("addToCart")
    public String addToCart(String skuId, int num, HttpServletRequest request, HttpServletResponse response) {
        String userId = (String)request.getAttribute("userId");
        List<CartInfo> cartInfos = new ArrayList<>();

        //通过skuId查询商品
        SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);
        //将商品信息封装为购物车属性
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setSkuNum(num);
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setIsChecked("1");
        if (StringUtils.isNotBlank(userId)) {
            cartInfo.setUserId(userId);
        }
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(num)));

        if (StringUtils.isBlank(userId)) {
            //未登陆 操作cookie
            String listCartCookieStr = CookieUtil.getCookieValue(request, "listCartCookie", true);
            //转化
            cartInfos = JSON.parseArray(listCartCookieStr, CartInfo.class);

            if (StringUtils.isBlank(listCartCookieStr)) {
                //cookie为空
                cartInfos = new ArrayList<>();
                cartInfos.add(cartInfo);
            } else {
                //判断是否新添加
                Boolean newProduct = isNewProduct(cartInfos, cartInfo);

                if (newProduct) {
                    //是新添加商品
                    cartInfos.add(cartInfo);
                } else {
                    //不是新添加商品 修改商品价格和数量
                    for (CartInfo info : cartInfos) {
                        if (info.getSkuId().equals(skuId)) {
                            info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
                }
            }
            CookieUtil.setCookie(request, response, "listCartCookie", JSON.toJSONString(cartInfos), 1000 * 60 * 60 * 24, true);

        } else {
            //已经登陆 更具skuID和userID查询cartInfo是否存在
            CartInfo ifExist = new CartInfo();
            ifExist.setUserId(userId);
            ifExist.setSkuId(skuId);
            CartInfo resultExist = cartInfoService.ifExist(ifExist);

            if (resultExist == null) {
                cartInfoService.saveCartInfo(cartInfo);
            } else {
                //db已存在
                resultExist.setSkuNum(num + resultExist.getSkuNum());
                resultExist.setCartPrice(resultExist.getSkuPrice().multiply(new BigDecimal(resultExist.getSkuNum())));
                cartInfoService.updateCartInfo(resultExist);
            }
            //同步缓存
            cartInfoService.flushCartCacheByUser(userId);
        }
        return "redirect:http://localhost:8084/success.html";
    }


    //判断是否为新购物车

    private Boolean isNewProduct(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = true;
        for (CartInfo cartInfoFromCache : cartInfos) {
            String skuIdFromCache = cartInfoFromCache.getSkuId();
            if (skuIdFromCache.equals(cartInfo.getSkuId())) {
                //添加过的商品
                return false;
            }
        }
        return b;
    }
}
