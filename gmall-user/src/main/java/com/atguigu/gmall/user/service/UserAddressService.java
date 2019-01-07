package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface UserAddressService {
    //根据id 和userid修改
    void updateAddressById(UserAddress userAddress);

    //根据userid id添加
    void addByMemberId(UserAddress userAddress);

    //根据id删除
    void deleteAddressById(Integer id);

    //根据userid查询所有
    List<UserAddress> selectByMemberId(String userId);


}
