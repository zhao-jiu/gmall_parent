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
 * @author 赵赳
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
     * 下架在ES中存储的商品信息
     *
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        System.out.println("es下架商品");

        searchElasticsearchRepository.deleteById(skuId);
    }

    /**
     * 查询数据库获取存储数据上架到ES中
     *
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {

        //从mysql获取数据
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryViewByCategory3Id(skuInfo.getCategory3Id());
        BigDecimal price = productFeignClient.getSkuPriceBySkuId(skuId);
        BaseTrademark baseTrademark = productFeignClient.getTrademarkById(skuInfo.getTmId());
        List<SearchAttr> searchAttrs = productFeignClient.getSearchAttrList(skuId);

        //添加数据到ES中
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
        //存储到ES中
        searchElasticsearchRepository.save(goods);
        System.out.println("es上架商品");
    }

    //创建goods索引
    @Override
    public void creatGoods() {
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
    }

    /**
     * @return 查询封装首页分类数据
     */
    @Override
    public List<JSONObject> getBaseCategoryList() {
        //查出的所有数据
        List<BaseCategoryView> baseCategoryList = productFeignClient.getBaseCategoryList();

        List<JSONObject> list1 = new ArrayList<>();
        //获取一级分类信息
        Map<Long, List<BaseCategoryView>> group1 = baseCategoryList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        for (Map.Entry<Long, List<BaseCategoryView>> category1List : group1.entrySet()) {
            JSONObject jsonObject1 = new JSONObject();
            Long category1Id = category1List.getKey();
            String category1Name = category1List.getValue().get(0).getCategory1Name();
            //封装一级分类信息
            jsonObject1.put("categoryId", category1Id);
            jsonObject1.put("categoryName", category1Name);

            List<JSONObject> list2 = new ArrayList<>();
            //从一级分类中获取二级分类信息
            Map<Long, List<BaseCategoryView>> group2 = category1List.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> category2List : group2.entrySet()) {
                JSONObject jsonObject2 = new JSONObject();
                Long category2Id = category2List.getKey();
                String category2Name = category2List.getValue().get(0).getCategory2Name();
                //封装二级分类信息
                jsonObject2.put("categoryId", category2Id);
                jsonObject2.put("categoryName", category2Name);

                List<JSONObject> list3 = new ArrayList<>();
                //从二级分类中获取三级分类信息
                Map<Long, List<BaseCategoryView>> group3 = category2List.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                for (Map.Entry<Long, List<BaseCategoryView>> category3List : group3.entrySet()) {
                    JSONObject jsonObject3 = new JSONObject();
                    Long category3Id = category3List.getKey();
                    String category3Name = category3List.getValue().get(0).getCategory3Name();
                    //封装三级分类信息
                    jsonObject3.put("categoryId", category3Id);
                    jsonObject3.put("categoryName", category3Name);
                    list3.add(jsonObject3);
                }
                jsonObject2.put("categoryChild", list3); //二级分类数据
                list2.add(jsonObject2);
            }
            jsonObject1.put("categoryChild", list2); //一级分类数据
            list1.add(jsonObject1);
        }

        return list1;
    }

    /***
     * 商品检索
     * @param searchParam 检索条件
     * @return 查询数据结果集
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        //构建请求
        SearchRequest searchRequest = getSearchRequest(searchParam);

        SearchResponse searchResponse = new SearchResponse();
        try {
            //查询返回结果
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //返回封装之后的结果
        return parseResponseValues(searchResponse);
    }

    /***
     * 从redis中获取热度值更新到ES库中
     */
    @Override
    public void hotScore(Long skuId) {

        //获取热度值
        Long increment = redisTemplate.opsForValue().increment("goods:" + skuId + ":hotScore", 1L);

        if (increment % 10 == 0) {
            Optional<Goods> goodsOptional = searchElasticsearchRepository.findById(skuId);
            Goods goods = goodsOptional.get();
            //设置最新的热度值
            goods.setHotScore(increment);
            searchElasticsearchRepository.save(goods);
        }


    }

    /***
     *  拼接查询条件
     * @param searchParam 查询参数
     * @return 查询请求
     */
    private SearchRequest getSearchRequest(SearchParam searchParam) {
        //获取搜索参数
        Long category3Id = searchParam.getCategory3Id();
        Long category2Id = searchParam.getCategory2Id();
        Long category1Id = searchParam.getCategory1Id();
        String keyword = searchParam.getKeyword();
        String order = searchParam.getOrder();
        String trademark = searchParam.getTrademark();
        String[] props = searchParam.getProps();


        SearchRequest searchRequest = new SearchRequest();
        //searchSourceBuilder 请求条件构造
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // bool 查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (!StringUtils.isEmpty(keyword)) {
            //查询标题 keyword=电脑
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword));

            //高亮设置
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder;'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        //过滤查询 过滤分类id
        if (category3Id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", category3Id));
        }
        if (category2Id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", category2Id));
        }
        if (category1Id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", category1Id));
        }

        //品牌查询过滤
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            Long tmId = Long.parseLong(split[0]);
            boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", tmId));
        }

        //属性过滤查询
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                //props=23:4G:运行内存
                //平台属性Id 平台属性值名称 平台属性名，
                Long attrId = Long.parseLong(split[0]);
                String attrValue = split[1];
                String attrName = split[2];
                BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();

                boolQueryBuilder1.filter(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryBuilder1.filter(QueryBuilders.termQuery("attrs.attrValue", attrValue));
                boolQueryBuilder1.filter(QueryBuilders.termQuery("attrs.attrName", attrName));
                //nested查询
                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs", boolQueryBuilder1, ScoreMode.None);
                boolQueryBuilder.must(nestedQueryBuilder);
            }
        }

        //进行bool查询
        searchSourceBuilder.query(boolQueryBuilder);

        //聚合品牌查询
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        //构建聚合品牌查询请求
        searchSourceBuilder.aggregation(aggregationBuilder);

        //属性聚合
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("attrsAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")));
        //构建属性聚合查询请求
        searchSourceBuilder.aggregation(nestedAggregationBuilder);


        //排序 // 后台拼接：1:hotScore 2:price
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

        //分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(60);
        //请求构建完成
        searchRequest.source(searchSourceBuilder);
        //System.out.println(searchSourceBuilder.toString());
        return searchRequest;
    }

    /***
     *  解析返回结果封装
     * @param searchResponse 请求ES返回响应
     * @return 封装后的数据
     */
    private SearchResponseVo parseResponseValues(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //解析返回结果
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();

            List<SearchResponseTmVo> trademarkList = new ArrayList<>();

            //解析品牌聚合结果
            ParsedLongTerms tmIdAgg = searchResponse.getAggregations().get("tmIdAgg");
            if (tmIdAgg != null) {
                List<? extends Terms.Bucket> buckets = tmIdAgg.getBuckets();
                if (null != buckets && buckets.size() > 0) {
                    for (Terms.Bucket bucket : buckets) {
                        SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                        //获取品牌名称聚合的bucket
                        ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
                        //获取品牌logoUrl聚合的bucket
                        ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
                        //封装品牌信息
                        searchResponseTmVo.setTmId(bucket.getKeyAsNumber().longValue());
                        searchResponseTmVo.setTmName(tmNameAgg.getBuckets().get(0).getKeyAsString());
                        searchResponseTmVo.setTmLogoUrl(tmLogoUrlAgg.getBuckets().get(0).getKeyAsString());
                        trademarkList.add(searchResponseTmVo);
                    }
                }
                searchResponseVo.setTrademarkList(trademarkList);
            }

            //解析属性聚合的结果
            ParsedNested attrsAgg = searchResponse.getAggregations().get("attrsAgg");
            ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
            if (attrIdAgg != null) {
                //收集
                List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAgg.getBuckets().stream().map(
                        attrIdBucket -> {
                            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                            //设置属性id
                            searchResponseAttrVo.setAttrId(attrIdBucket.getKeyAsNumber().longValue());
                            //获取属性名的bucket
                            ParsedStringTerms attrNameAgg = attrIdBucket.getAggregations().get("attrNameAgg");
                            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                            //设置属性名
                            searchResponseAttrVo.setAttrName(attrName);
                            //获取属性值的bucket
                            ParsedStringTerms attrValueAgg = attrIdBucket.getAggregations().get("attrValueAgg");
                            List<String> attrValueList = attrValueAgg.getBuckets().stream().map(attrValueBucket -> {
                                return attrValueBucket.getKeyAsString();
                            }).collect(Collectors.toList());
                            //设置属性值
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

                        //获取高亮属性替换原有的title值
                        Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
                        if (highlightFields != null && highlightFields.size() > 0) {
                            String highlight = highlightFields.get("title").getFragments()[0].toString();
                            goods.setTitle(highlight);
                        }

                    }

                }
                searchResponseVo.setGoodsList(goodsList);
            }
            //总记录数
            long totalHits = hits.getTotalHits();
            searchResponseVo.setTotalPages(totalHits);
        }
        return searchResponseVo;
    }


    // TODO 递归调用
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
