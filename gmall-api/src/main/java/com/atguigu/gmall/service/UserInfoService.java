package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.bean.UserAddress;
import java.util.List;

public interface UserInfoService {

//查询所有
    public List<UserInfo> getUserInfoListAll();
//添加
    public void addUser(UserInfo userInfo);
//更新
    public void updateUser(String id,UserInfo userInfo);
//根据用户id查询addressList
    public List<UserAddress> getUserAddressList(String userId);





}

