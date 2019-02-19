package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class itemController {
    @Reference
    SkuInfoService skuInfoService;
    @Reference
    SpuInfoService spuInfoService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, Model model) {
        SkuInfo skuInfo = skuInfoService.getItem(skuId);
        String spuId = skuInfo.getSpuId();
        //通过skuId 的查询销售属性和销售属性值
       List<SpuSaleAttr> spuSaleAttrList= spuInfoService.spuSaleAttrListCheckBySku(spuId,skuId);
        model.addAttribute("spuSaleAttrListCheckBySku", spuSaleAttrList);
        model.addAttribute("skuInfo", skuInfo);

        List<SkuInfo> skuInfos  = skuInfoService.getSkuSaleAttrValueListBySpu(spuId);
        Map<String,String> skuMap = new HashMap<>();
        for (SkuInfo sku : skuInfos) {
            String v = sku.getId();
            List<SkuSaleAttrValue> skuSaleAttrValues = sku.getSkuSaleAttrValueList();
            String k = "";
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValues) {
                String valueId = skuSaleAttrValue.getSaleAttrValueId();
                k = k + "|"+valueId;
            }
            skuMap.put(k,v);
        }

       model.addAttribute("skuMap", JSON.toJSONString(skuMap));
        return "item";
    }
}
