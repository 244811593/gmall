package com.atguigu.gmall.manage.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.manage.util.ManageUploadUtil;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SpuController {
    @Reference
    SpuInfoService spuInfoService;

//文件上传

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file")MultipartFile multipartFile) {
        String imgUrl = ManageUploadUtil.impUpload(multipartFile);
        return imgUrl;
    }

    //销售属性下拉选项
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = spuInfoService.getBaseSaleAttrList();
        return baseSaleAttrList;
    }
    //保存销售属性

    @RequestMapping("saveSpu")
    @ResponseBody
    public String saveSpu(SpuInfo spuInfo) {

        spuInfoService.saveSpu(spuInfo);
        return "success";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> getSpuList(String catalog3Id) {
        List<SpuInfo> spuInfos = spuInfoService.getSpuList(catalog3Id);
        return spuInfos;
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        List<SpuSaleAttr> SpuSaleAttrs= spuInfoService.spuSaleAttrList(spuId);
        return SpuSaleAttrs;
    }


    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId) {
        List<SpuImage> spuImageList= spuInfoService.spuImageList(spuId);
        return spuImageList;
    }

}
