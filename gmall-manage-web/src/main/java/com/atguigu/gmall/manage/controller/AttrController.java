package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;
import com.atguigu.gmall.service.BaseAttrInfoService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrController {
    @Reference
    BaseAttrInfoService attrInfoService;


    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getBaseCatalog1List() {
        List<BaseCatalog1> baseCatalog1 = attrInfoService.getBaseCatalog1();

        return baseCatalog1;
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getBaseCatalog2List(String catalog1Id) {
        List<BaseCatalog2> baseCatalog2 = attrInfoService.getBaseCatalog2(catalog1Id);
        return baseCatalog2;
    }
    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getBaseCatalog3List(String catalog2Id) {
        List<BaseCatalog3> baseCatalog3 = attrInfoService.getBaseCatalog3(catalog2Id);
        return baseCatalog3;
    }
    @RequestMapping("getAttrList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        List<BaseAttrInfo> baseAttrInfoList = attrInfoService.getAttrList(catalog3Id);
        return baseAttrInfoList;
    }

}
