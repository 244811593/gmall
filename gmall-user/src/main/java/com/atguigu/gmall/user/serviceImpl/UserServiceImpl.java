package com.atguigu.gmall.user.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Service
public class UserServiceImpl implements UserInfoService {

@Autowired
    UserInfoMapper userInfoMapper;
@Autowired
UserAddressMapper userAddressMapper ;

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


        return null;



    }
}



