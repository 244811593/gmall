package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.BaseAttrInfoService;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class ListController {


    @Reference
    ListService listService;
    @Reference
    BaseAttrInfoService baseAttrInfoService;

    //页面显示商品信息
    @RequestMapping("list.html")
    public String toListPage(SkuLsParam skuLsParam, Model model) {
        //入参查询进入商品列表
        List<SkuLsInfo> skuLsInfoList = listService.getList(skuLsParam);
        //根据valueid查询平台属性
        //获取valueid并用set去重
        Set<String> attrValueId = new HashSet<>();
        List<BaseAttrInfo> attrList=new ArrayList<>();
if (skuLsInfoList!=null&&skuLsInfoList.size()>0) {
    for (SkuLsInfo skuLsInfo : skuLsInfoList) {
        List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
        for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
            String valueId = skuLsAttrValue.getValueId();
            attrValueId.add(valueId);
        }
    }
    //将集合按逗号分隔 便于数据库查询
    String join = StringUtils.join(attrValueId, ",");
    //在数据库中查询baseAttr和baseAttrValue
    attrList = baseAttrInfoService.getAttrValueByJoin(join);
}
        model.addAttribute("attrList", attrList);
        model.addAttribute("skuLsInfoList", skuLsInfoList);



        //从所有属性中去掉请求中已存在的属性
        String[] valueId = skuLsParam.getValueId();
//        List<Cramb> attrValueSelectedList = new ArrayList<>();
        if (valueId != null && valueId.length > 0) {
            Iterator<BaseAttrInfo> iterator = attrList.iterator();
            //遍历所有属性
            while (iterator.hasNext()) {
                List<BaseAttrValue> attrValueList = iterator.next().getAttrValueList();
                //遍历所有属性对应的属性值
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    //属性值id
                    String id = baseAttrValue.getId();
                    //请求中的属性值id
                    for (String sid : valueId) {
                        if (id.equals(sid)) {
//                            // 有一个属性值被去掉，就有一个面包屑
//                            Cramb cramb = new Cramb();
//                            String myUrlParam = getMyUrlParam(skuLsParam, sid);
//                            cramb.setUrlParam(myUrlParam);
//                            cramb.setValueName(baseAttrValue.getValueName());
//                            attrValueSelectedList.add(cramb);
                            iterator.remove();
                        }
                    }
                }
            }
        }
//        model.addAttribute("attrValueSelectedList", attrValueSelectedList);

        //获得参数url
        String urlParam = getMyUrlParam(skuLsParam);
        model.addAttribute("urlParam", urlParam);
        return "list";
    }

    private String getMyUrlParam(SkuLsParam skuLsParam) {
        //通过入参设置url
        String urlParam = "";
        String catalog3Id = skuLsParam.getCatalog3Id();
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        String keyword = skuLsParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        String[] valueId = skuLsParam.getValueId();
        if (valueId != null && valueId.length > 0) {
            for (String sid : valueId) {
                    urlParam = urlParam + "&valueId=" + sid;
            }
        }
        return urlParam;
    }

}
