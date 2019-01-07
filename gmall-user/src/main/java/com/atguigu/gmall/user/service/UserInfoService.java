package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.UserInfo;

import java.util.List;

public interface UserInfoService {

    //查询所有用户
    List<UserInfo> selectUserAll();
    //通过id查询用户
    UserInfo selectById(String userid);
    //添加用户
    void addUserInfo(UserInfo userInfo);
    //修改用户
    Integer updateUserInfo(UserInfo userInfo);
    //通过id删除用户信息
    void deleteUserInfoById(String userid);

}

