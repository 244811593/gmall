package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartInfoService {
    CartInfo ifExist(CartInfo ifExist);

    void saveCartInfo(CartInfo cartInfo);

    void updateCartInfo(CartInfo resultExist);

    void flushCartCacheByUser(String userId);

    List<CartInfo> cartListFromCache(String userId);

    void mergCart(String userId, String listCartCookie);
}
