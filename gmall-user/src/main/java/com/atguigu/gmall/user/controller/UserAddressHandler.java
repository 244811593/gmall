package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.user.bean.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import org.apache.ibatis.annotations.ResultMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserAddressHandler {
    @Autowired
    UserAddressService userAddressService;

    @RequestMapping("getAddressByUserId")
    @ResponseBody
    public List<UserAddress> getAddressByUserId(@RequestParam("userId") String userId) {

        List<UserAddress> list = userAddressService.selectByMemberId(userId);
        return list;
    }
}
