package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.search.repository.SearchElasticsearchRepository;
import com.atguigu.gmall.search.service.SearchService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ??????
 * @CreateTime: 2021/4/24 13:58
 * @Description:
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    SearchElasticsearchRepository searchElasticsearchRepository;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RedisTemplate redisTemplate;


    /**
     * ?????????ES????????????????????????
     *
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        System.out.println("es????????????");

        searchElasticsearchRepository.deleteById(skuId);
    }

    /**
     * ??????????????????????????????????????????ES???
     *
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {

        //???mysql????????????
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryViewByCategory3Id(skuInfo.getCategory3Id());
        BigDecimal price = productFeignClient.getSkuPriceBySkuId(skuId);
        BaseTrademark baseTrademark = productFeignClient.getTrademarkById(skuInfo.getTmId());
        List<SearchAttr> searchAttrs = productFeignClient.getSearchAttrList(skuId);

        //???????????????ES???
        Goods goods = new Goods();
        goods.setId(skuId);
        goods.setPrice(price.doubleValue());
        goods.setCreateTime(new Date());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setCategory3Id(baseCategoryView.getCategory3Id());
        goods.setCategory3Name(baseCategoryView.getCategory3Name());
        goods.setCategory2Id(baseCategoryView.getCategory2Id());
        goods.setCategory2Name(baseCategoryView.getCategory2Name());
        goods.setCategory1Id(baseCategoryView.getCategory1Id());
        goods.setCategory1Name(baseCategoryView.getCategory1Name());
        goods.setHotScore(0L);
        goods.setTmId(baseTrademark.getId());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        goods.setAttrs(searchAttrs);
        goods.setTitle(skuInfo.getSkuName());
        //?????????ES???
        searchElasticsearchRepository.save(goods);
        System.out.println("es????????????");
    }

    //??????goods??????
    @Override
    public void creatGoods() {
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
    }

    /**
     * @return ??????????????????????????????
     */
    @Override
    public List<JSONObject> getBaseCategoryList() {
        //?????????????????????
        List<BaseCategoryView> baseCategoryList = productFeignClient.getBaseCategoryList();

        List<JSONObject> list1 = new ArrayList<>();
        //????????????????????????
        Map<Long, List<BaseCategoryView>> group1 = baseCategoryList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        for (Map.Entry<Long, List<BaseCategoryView>> category1List : group1.entrySet()) {
            JSONObject jsonObject1 = new JSONObject();
            Long category1Id = category1List.getKey();
            String category1Name = category1List.getValue().get(0).getCategory1Name();
            //????????????????????????
            jsonObject1.put("categoryId", category1Id);
            jsonObject1.put("categoryName", category1Name);

            List<JSONObject> list2 = new ArrayList<>();
            //??????????????????????????????????????????
            Map<Long, List<BaseCategoryView>> group2 = category1List.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> category2List : group2.entrySet()) {
                JSONObject jsonObject2 = new JSONObject();
                Long category2Id = category2List.getKey();
                String category2Name = category2List.getValue().get(0).getCategory2Name();
                //????????????????????????
                jsonObject2.put("categoryId", category2Id);
                jsonObject2.put("categoryName", category2Name);

                List<JSONObject> list3 = new ArrayList<>();
                //??????????????????????????????????????????
                Map<Long, List<BaseCategoryView>> group3 = category2List.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                for (Map.Entry<Long, List<BaseCategoryView>> category3List : group3.entrySet()) {
                    JSONObject jsonObject3 = new JSONObject();
                    Long category3Id = category3List.getKey();
                    String category3Name = category3List.getValue().get(0).getCategory3Name();
                    //????????????????????????
                    jsonObject3.put("categoryId", category3Id);
                    jsonObject3.put("categoryName", category3Name);
                    list3.add(jsonObject3);
                }
                jsonObject2.put("categoryChild", list3); //??????????????????
                list2.add(jsonObject2);
            }
            jsonObject1.put("categoryChild", list2); //??????????????????
            list1.add(jsonObject1);
        }

        return list1;
    }

    /***
     * ????????????
     * @param searchParam ????????????
     * @return ?????????????????????
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        //????????????
        SearchRequest searchRequest = getSearchRequest(searchParam);

        SearchResponse searchResponse = new SearchResponse();
        try {
            //??????????????????
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //???????????????????????????
        return parseResponseValues(searchResponse);
    }

    /***
     * ???redis???????????????????????????ES??????
     */
    @Override
    public void hotScore(Long skuId) {

        //???????????????
        Long increment = redisTemplate.opsForValue().increment("goods:" + skuId + ":hotScore", 1L);

        if (increment % 10 == 0) {
            Optional<Goods> goodsOptional = searchElasticsearchRepository.findById(skuId);
            Goods goods = goodsOptional.get();
            //????????????????????????
            goods.setHotScore(increment);
            searchElasticsearchRepository.save(goods);
        }


    }

    /***
     *  ??????????????????
     * @param searchParam ????????????
     * @return ????????????
     */
    private SearchRequest getSearchRequest(SearchParam searchParam) {
        //??????????????????
        Long category3Id = searchParam.getCategory3Id();
        Long category2Id = searchParam.getCategory2Id();
        Long category1Id = searchParam.getCategory1Id();
        String keyword = searchParam.getKeyword();
        String order = searchParam.getOrder();
        String trademark = searchParam.getTrademark();
        String[] props = searchParam.getProps();


        SearchRequest searchRequest = new SearchRequest();
        //searchSourceBuilder ??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // bool ??????
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (!StringUtils.isEmpty(keyword)) {
            //???????????? keyword=??????
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword));

            //????????????
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder;'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        //???????????? ????????????id
        if (category3Id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", category3Id));
        }
        if (category2Id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", category2Id));
        }
        if (category1Id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", category1Id));
        }

        //??????????????????
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            Long tmId = Long.parseLong(split[0]);
            boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", tmId));
        }

        //??????????????????
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                //props=23:4G:????????????
                //????????????Id ????????????????????? ??????????????????
                Long attrId = Long.parseLong(split[0]);
                String attrValue = split[1];
                String attrName = split[2];
                BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();

                boolQueryBuilder1.filter(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryBuilder1.filter(QueryBuilders.termQuery("attrs.attrValue", attrValue));
                boolQueryBuilder1.filter(QueryBuilders.termQuery("attrs.attrName", attrName));
                //nested??????
                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs", boolQueryBuilder1, ScoreMode.None);
                boolQueryBuilder.must(nestedQueryBuilder);
            }
        }

        //??????bool??????
        searchSourceBuilder.query(boolQueryBuilder);

        //??????????????????
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        //??????????????????????????????
        searchSourceBuilder.aggregation(aggregationBuilder);

        //????????????
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("attrsAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")));
        //??????????????????????????????
        searchSourceBuilder.aggregation(nestedAggregationBuilder);


        //?????? // ???????????????1:hotScore 2:price
        if (!StringUtils.isEmpty(order)) {
            String[] split = order.split(":");
            String sortName = "";
            if (split[0].equals("1")) {
                sortName = "hotScore";
            } else if (split[0].equals("2")) {
                sortName = "price";
            }
            searchSourceBuilder.sort(sortName, split[1].equals("asc") ? SortOrder.ASC : SortOrder.DESC);
        }

        //??????
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(60);
        //??????????????????
        searchRequest.source(searchSourceBuilder);
        //System.out.println(searchSourceBuilder.toString());
        return searchRequest;
    }

    /***
     *  ????????????????????????
     * @param searchResponse ??????ES????????????
     * @return ??????????????????
     */
    private SearchResponseVo parseResponseValues(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //??????????????????
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();

            List<SearchResponseTmVo> trademarkList = new ArrayList<>();

            //????????????????????????
            ParsedLongTerms tmIdAgg = searchResponse.getAggregations().get("tmIdAgg");
            if (tmIdAgg != null) {
                List<? extends Terms.Bucket> buckets = tmIdAgg.getBuckets();
                if (null != buckets && buckets.size() > 0) {
                    for (Terms.Bucket bucket : buckets) {
                        SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                        //???????????????????????????bucket
                        ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
                        //????????????logoUrl?????????bucket
                        ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
                        //??????????????????
                        searchResponseTmVo.setTmId(bucket.getKeyAsNumber().longValue());
                        searchResponseTmVo.setTmName(tmNameAgg.getBuckets().get(0).getKeyAsString());
                        searchResponseTmVo.setTmLogoUrl(tmLogoUrlAgg.getBuckets().get(0).getKeyAsString());
                        trademarkList.add(searchResponseTmVo);
                    }
                }
                searchResponseVo.setTrademarkList(trademarkList);
            }

            //???????????????????????????
            ParsedNested attrsAgg = searchResponse.getAggregations().get("attrsAgg");
            ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
            if (attrIdAgg != null) {
                //??????
                List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAgg.getBuckets().stream().map(
                        attrIdBucket -> {
                            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                            //????????????id
                            searchResponseAttrVo.setAttrId(attrIdBucket.getKeyAsNumber().longValue());
                            //??????????????????bucket
                            ParsedStringTerms attrNameAgg = attrIdBucket.getAggregations().get("attrNameAgg");
                            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                            //???????????????
                            searchResponseAttrVo.setAttrName(attrName);
                            //??????????????????bucket
                            ParsedStringTerms attrValueAgg = attrIdBucket.getAggregations().get("attrValueAgg");
                            List<String> attrValueList = attrValueAgg.getBuckets().stream().map(attrValueBucket -> {
                                return attrValueBucket.getKeyAsString();
                            }).collect(Collectors.toList());
                            //???????????????
                            searchResponseAttrVo.setAttrValueList(attrValueList);
                            return searchResponseAttrVo;
                        }
                ).collect(Collectors.toList());
                searchResponseVo.setAttrsList(searchResponseAttrVos);
            }

            if (hits != null) {
                SearchHit[] hitsResult = hits.getHits();
                List<Goods> goodsList = new ArrayList<>();
                for (SearchHit documentFields : hitsResult) {
                    String index = documentFields.getIndex();
                    if (index.equals("goods")) {
                        String sourceAsString = documentFields.getSourceAsString();
                        Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                        goodsList.add(goods);

                        //?????????????????????????????????title???
                        Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
                        if (highlightFields != null && highlightFields.size() > 0) {
                            String highlight = highlightFields.get("title").getFragments()[0].toString();
                            goods.setTitle(highlight);
                        }

                    }

                }
                searchResponseVo.setGoodsList(goodsList);
            }
            //????????????
            long totalHits = hits.getTotalHits();
            searchResponseVo.setTotalPages(totalHits);
        }
        return searchResponseVo;
    }


    // TODO ????????????
    public List<JSONObject> getBaseCategoryListOrder(List<BaseCategoryView> baseCategoryList, List<JSONObject> categoryChild) {
        Map<Long, List<BaseCategoryView>> group = baseCategoryList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        List<JSONObject> list1 = new ArrayList<>();

        for (Map.Entry<Long, List<BaseCategoryView>> list : group.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            Long categoryId = list.getKey();
            String categoryName = list.getValue().get(0).getCategory1Name();
            jsonObject.put("categoryId", categoryId);
            jsonObject.put("categoryName", categoryName);

            jsonObject.put("categoryChild", categoryChild);
            list1.add(jsonObject);
        }

        return list1;
    }


}
