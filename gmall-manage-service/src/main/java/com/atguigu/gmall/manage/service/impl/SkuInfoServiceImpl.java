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
        for (SkuSaleAttrValue skuSaleAttrValue:skuSaleAttrValueList){
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }
        //保存平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue:skuAttrValueList ) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }
        //保存图片属性
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage:skuImageList) {
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
        //从缓存中拿取skuInfo
        String skuInfoStr = jedis.get("sku:" + skuId + "info");
        SkuInfo skuInfo = JSON.parseObject(skuInfoStr,SkuInfo.class);
        //如果缓存中没有数据
        if (skuInfo==null){
            //拿分布式锁
            String lock = jedis.set("sku:" + skuId + "lock", "1", "nx", "px", 10000);
            if (StringUtils.isBlank(lock)){
                //  锁被占用
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return  getItem(skuId);
            }

            else {
                //从数据库查询
                skuInfo = getItemDB(skuId);

            }
            //还锁
            jedis.del("sku:"+skuId+":lock")  ;
            //存入缓存中
            jedis.set("sku:" + skuId + "info",JSON.toJSONString(skuInfo));
        }
        jedis.close();
        return skuInfo;
    }

    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuInfo> skuInfoList= skuSaleAttrValueMapper.getSkuSaleAttrValueListBySpu(spuId);
        return skuInfoList;
    }


}
