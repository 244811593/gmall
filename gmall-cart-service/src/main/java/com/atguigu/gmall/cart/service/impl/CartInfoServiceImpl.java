package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public CartInfo ifExist(CartInfo ifExist) {
        CartInfo cartInfo = cartInfoMapper.selectOne(ifExist);
        return cartInfo;
    }

    @Override
    public void saveCartInfo(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void updateCartInfo(CartInfo resultExist) {
        cartInfoMapper.updateByPrimaryKeySelective(resultExist);

    }

    @Override
    public void flushCartCacheByUser(String userId) {
        //通过hash同步缓存

        HashMap<String, String> stringStringHashMap = new HashMap<>();
        //通过userId查询购物车信息
        List<CartInfo> cartInfoList = getCartInfoListByUserId(userId);
        if (cartInfoList != null) {
            for (CartInfo cartInfo : cartInfoList) {
                stringStringHashMap.put(cartInfo.getId(), JSON.toJSONString(cartInfo));
            }
        }
        Jedis jedis = redisUtil.getJedis();
        jedis.hmset("cart:" + userId + ":info", stringStringHashMap);
        jedis.close();

    }

    @Override
    public List<CartInfo> cartListFromCache(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("cart:" + userId + ":info");
        for (String hval : hvals) {
            CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
            cartInfoList.add(cartInfo);
        }

        return cartInfoList;
    }

    //合并购物车 将未登录时缓存cookie中的购物车根据userid存入db  然后刷新redis缓存中的购物车
    @Override
    public void mergCart(String userId, String listCartCookie) {
        List<CartInfo> cartInfoListFromCookie = JSON.parseArray(listCartCookie, CartInfo.class);
        List<CartInfo> cartInfoListFromDB = getCartInfoListByUserId(userId);
        for (CartInfo cartInfoCookie : cartInfoListFromCookie) {
            Boolean b = isNewProduct(cartInfoListFromDB, cartInfoCookie);//cookie中的数据是否在DB中存在
            if (b){
                //不存在 将cookie中的数据插入DB
                cartInfoCookie.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCookie);
            }else {
                //更新
                for (CartInfo cartInfoDB : cartInfoListFromDB) {
                    if (cartInfoDB.getUserId().equals(cartInfoCookie.getUserId())){
                        cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCookie.getSkuNum());
                        cartInfoDB.setCartPrice(cartInfoDB.getSkuPrice().multiply(new BigDecimal(cartInfoCookie.getSkuNum())));
                    }
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                }

            }
            //同步缓存
            flushCartCacheByUser(userId);
        }

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

    private List<CartInfo> getCartInfoListByUserId(String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);
        return cartInfos;
    }
}
