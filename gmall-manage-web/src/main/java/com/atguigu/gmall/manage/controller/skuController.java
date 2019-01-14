package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class skuController {
@Reference
SkuInfoService skuInfoService;

    @RequestMapping("skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> skuInfoListBySpu(String spuId){
        List<SkuInfo> skuInfoList = skuInfoService.skuInfoListBySpu(spuId);
        return skuInfoList;
    }
    @RequestMapping("saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){

        return "success";
    }
}
