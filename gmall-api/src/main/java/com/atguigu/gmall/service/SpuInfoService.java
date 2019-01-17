package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface SpuInfoService {
    List<SpuInfo> getSpuList(String catalog3Id);

    List<BaseSaleAttr> getBaseSaleAttrList();

    List<SpuSaleAttr> spuSaleAttrList(String spuId);

    List<SpuImage> spuImageList(String spuId);

    void saveSpu(SpuInfo spuInfo);

    List<SpuSaleAttr> spuSaleAttrListCheckBySku(String spuId,String skuId);
}
