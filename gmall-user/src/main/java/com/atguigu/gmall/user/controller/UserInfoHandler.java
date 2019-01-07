package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.user.bean.UserInfo;
import com.atguigu.gmall.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserInfoHandler {
    @Autowired
    UserInfoService userService;

    @RequestMapping("index")
    @ResponseBody
    public List<UserInfo> UserList() {
        List<UserInfo> list=  userService.selectUserAll();


        return list;
    }
}
