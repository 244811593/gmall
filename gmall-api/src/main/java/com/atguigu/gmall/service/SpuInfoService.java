package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SpuInfo;

import java.util.List;

public interface SpuInfoService {
    List<SpuInfo> getSpuList(String catalog3Id);
}
