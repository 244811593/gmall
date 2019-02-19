package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public List<SkuInfo> skuInfoListBySpu(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfo);
        return skuInfos;
    }

    //增加sku的保存
    @Override
    public void saveSku(SkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();
        //保存销售属性
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }
        //保存平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }
        //保存图片属性
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insertSelective(skuImage);
        }


    }

    //从数据库查询数据
    @Override
    public SkuInfo getItemDB(String skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo info = skuInfoMapper.selectOne(skuInfo);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> imgList = skuImageMapper.select(skuImage);
        info.setSkuImageList(imgList);
        return info;
    }

    @Override
    public SkuInfo getItem(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        //从缓存中拿取数据
        String s = jedis.get("sku:" + skuId + ":Info");
        SkuInfo skuInfo = JSON.parseObject(s, SkuInfo.class);
        if (skuInfo == null) {
            //缓存中没有数据
            //拿锁
            String lock = jedis.set("sku:" + skuId + ":lock", "1", "nx", "ex", 10000);
            if (StringUtils.isBlank(lock)) {
                //锁被占用
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //4s后重试
                return getItem(skuId);
            } else {
                //从数据库拿取数据
                skuInfo = skuInfo = getItemDB(skuId);
            }
            //还锁
            jedis.del("sku:" + skuId + ":lock");
            //存入缓存
            jedis.set("sku:" + skuId + ":Info", JSON.toJSONString(skuInfo));
        }
        jedis.close();
        return skuInfo;
    }

    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuInfo> skuInfoList = skuSaleAttrValueMapper.getSkuSaleAttrValueListBySpu(spuId);
        return skuInfoList;
    }
     //通过catalog3Id查询所有的skuInfo
    @Override
    public List<SkuInfo> skuListByCatalog3Id(String catalog3Id) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id(catalog3Id);
        List<SkuInfo> skuInfoList = skuInfoMapper.select(skuInfo);

        for (SkuInfo info : skuInfoList) {
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(info.getId());
            List<SkuAttrValue> select = skuAttrValueMapper.select(skuAttrValue);
            info.setSkuAttrValueList(select);
        }

        return skuInfoList;
    }

    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {
        SkuInfo skuInfo1 = new SkuInfo();
        skuInfo1.setId(skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(skuInfo1);
        return skuInfo;
    }

    @Override
    public SkuInfo getSkuById(String skuId) {
        return null;
    }


}
