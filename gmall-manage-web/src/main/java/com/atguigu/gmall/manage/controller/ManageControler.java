package com.atguigu.gmall.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class ManageControler {
    @RequestMapping("index")
    public  String test(){

        return "index";
    }

    @RequestMapping("attrListPage")
    public  String toAttrListPage(){

        return "attrListPage";
    }
    @RequestMapping("spuListPage")
    public  String toSpuListPage(){

        return "spuListPage";
    }



}
