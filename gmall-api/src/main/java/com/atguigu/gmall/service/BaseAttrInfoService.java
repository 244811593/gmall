package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface BaseAttrInfoService {
    List<BaseCatalog1> getBaseCatalog1();


    List<BaseCatalog2> getBaseCatalog2(String catalog1Id);

    List<BaseCatalog3> getBaseCatalog3(String catalog2Id);

    List<BaseAttrInfo> getAttrList(String catalog3Id);

    void saveAttr(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(String attrId);

    void saveEditAttr(BaseAttrInfo baseAttrInfo);
}

