package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;


    //根据搜索框或者导航显示商品列表
    @Override
    public List<SkuLsInfo> getList(SkuLsParam skuLsParam) {
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        //获取查询dsl语句
        String dsl = getMydsl(skuLsParam);
        System.out.print(dsl);
        //查询
        Search Search = new Search.Builder(dsl).addIndex("gmall0906").addType("SkuLsInfo").build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(Search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (execute.getTotal() > 0) {
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo source = hit.source;
                skuLsInfoList.add(source);
            }
        }
        return skuLsInfoList;
    }



    public String getMydsl(SkuLsParam skuLsParam) {
        //查叙语句封装
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //三级分类id
        String catalog3Id = skuLsParam.getCatalog3Id();
        if (StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder queryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(queryBuilder);
        }
        //关键字
        String keyword = skuLsParam.getKeyword();
        if (StringUtils.isNotBlank(keyword) ) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //分类筛选
        String[] valueId = skuLsParam.getValueId();
        if (valueId != null&&valueId.length > 0  ) {
            for (String id : valueId) {
                TermQueryBuilder queryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", id);
                boolQueryBuilder.filter(queryBuilder);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("skuName");
        searchSourceBuilder.highlight(highlightBuilder);

        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);




        return searchSourceBuilder.toString();
    }
}
