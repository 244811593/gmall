package com.atguigu.gmall.user.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    UserAddressMapper userAddressMapper;


    @Override
    public void updateAddressById(UserAddress userAddress) {

        userAddressMapper.updateByPrimaryKey(userAddress);
    }




    @Override
    public void addByMemberId(UserAddress userAddress) {
        userAddressMapper.insert(userAddress);
    }

    @Override
    public void deleteAddressById(Integer id) {
        userAddressMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<UserAddress> selectByMemberId(String userId) {
        Example example = new Example(UserAddress.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId);
        List<UserAddress> userAddresses = userAddressMapper.selectByExample(example);
        return userAddresses;
        
    }

}
