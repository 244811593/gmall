package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;

import java.util.List;

public interface SkuInfoService {
    List<SkuInfo> skuInfoListBySpu(String spuId);
}
