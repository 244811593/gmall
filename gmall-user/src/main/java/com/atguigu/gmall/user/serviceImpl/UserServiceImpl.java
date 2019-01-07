package com.atguigu.gmall.user.serviceImpl;

import com.atguigu.gmall.user.bean.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImpl implements UserInfoService {
@Autowired
    UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> getUserAll() {
        return userInfoMapper.selectAll();
    }
}
