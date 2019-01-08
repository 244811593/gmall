package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;


import com.atguigu.gmall.service.UserInfoService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.List;

@Controller
public class UserInfoHandler {
    @Reference
    UserInfoService userService;

    @RequestMapping("index")
    @ResponseBody
    public List<UserInfo> UserList() {
        List<UserInfo> list = userService.getUserInfoListAll();


        return list;
    }
}
