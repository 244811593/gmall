package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;

import java.util.List;

public interface BaseAttrInfoService {
    List<BaseCatalog1> getBaseCatalog1();


    List<BaseCatalog2> getBaseCatalog2(String catalog1Id);

    List<BaseCatalog3> getBaseCatalog3(String catalog2Id);

    List<BaseAttrInfo> getAttrList(String catalog3Id);
}
