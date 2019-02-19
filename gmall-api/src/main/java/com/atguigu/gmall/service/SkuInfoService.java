package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface SkuInfoService {
    List<SkuInfo> skuInfoListBySpu(String spuId);

    void saveSku(SkuInfo skuInfo);

    SkuInfo getItemDB(String skuId);

    SkuInfo getItem(String skuId);

    List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId);

    List<SkuInfo> skuListByCatalog3Id(String catalog3Id);

    SkuInfo getSkuInfoBySkuId(String skuId);

    SkuInfo getSkuById(String skuId);
}
