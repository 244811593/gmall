package com.atguigu.gmall.user.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;


@Service
public class UserServiceImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UserInfo> getUserInfoListAll() {
        return userInfoMapper.selectAll();

    }

    @Override
    public void addUser(UserInfo userInfo) {

    }

    @Override
    public void updateUser(String id, UserInfo userInfo) {
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(Integer.parseInt(userId));
        List<UserAddress> addresses = userAddressMapper.select(userAddress);
        return addresses;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        UserInfo userInfo1 = new UserInfo();
        userInfo1.setLoginName(userInfo.getLoginName());
        userInfo1.setPasswd(userInfo.getPasswd());
        UserInfo userInfo2 = userInfoMapper.selectOne(userInfo1);
        return userInfo2;
    }

    @Override
    public void addUserCache(UserInfo user) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:" + user.getId() + ":info", 60 * 60 * 24, JSON.toJSONString(user));
        jedis.close();
    }

    @Override
    public UserAddress getUserAddressById(String deliveryAddressId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(Integer.parseInt(deliveryAddressId));
        UserAddress userAddress1 = userAddressMapper.selectOne(userAddress);
        return userAddress1;
    }
}



