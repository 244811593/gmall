package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.bean.UserAddress;
import java.util.List;

public interface UserInfoService {

//查询所有
     List<UserInfo> getUserInfoListAll();
//添加
     void addUser(UserInfo userInfo);
//更新
     void updateUser(String id,UserInfo userInfo);
//根据用户id查询addressList
     List<UserAddress> getUserAddressList(String userId);


    UserInfo login(UserInfo userInfo);

    void addUserCache(UserInfo user);

    UserAddress getUserAddressById(String deliveryAddressId);
}

