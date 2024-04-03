package com.jeesite.modules.cat.service.cg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.common.MtxHttpClientUtils;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionTitleKeywordDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionGoodPriceDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.entity.MaocheDataokeProductDO;
import com.jeesite.modules.cat.entity.MaocheProductV2DO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.ProductDataSource;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatEsHelper;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.helper.UnionProductHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionGoodPriceService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductBihaohuoService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductDetailService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionTitleKeywordService;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaocheCategoryProductRelService;
import com.jeesite.modules.cat.service.MaocheCategoryService;
import com.jeesite.modules.cat.service.MaocheDataokeProductService;
import com.jeesite.modules.cat.service.MaocheProductV2Service;
import com.jeesite.modules.cat.service.es.common.SearchService;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.jeesite.modules.cat.service.stage.cg.ProductEsFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CgUnionProductService {

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaocheAlimamaUnionTitleKeywordDao maocheAlimamaUnionTitleKeywordDao;

    @Resource
    private MaocheAlimamaUnionTitleKeywordService maocheAlimamaUnionTitleKeywordService;

    @Resource
    private MaocheAlimamaUnionGoodPriceService maocheAlimamaUnionGoodPriceService;

    @Resource
    private MaocheCategoryProductRelService maocheCategoryProductRelService;

    @Resource
    private MaocheCategoryService maocheCategoryService;

    @Resource
    private MaocheAlimamaUnionProductDetailService maocheAlimamaUnionProductDetailService;

    @Resource
    private ProductEsFactory productEsFactory;

    @Resource
    private MaocheDataokeProductService maocheDataokeProductService;

    @Resource
    private MaocheCategoryMappingService maocheCategoryMappingService;

    @Resource
    private MaocheAlimamaUnionProductBihaohuoService maocheAlimamaUnionProductBihaohuoService;

    @Resource
    private MaocheProductV2Service maocheProductV2Service;

    @Resource
    private DingDingService dingDingService;

    @Resource
    private SearchService searchService;

    /**
     * 查询商品索引数据
     * @param condition
     * @param from
     * @param size
     */
    public ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchProduct(CatUnionProductCondition condition,
                                                                                            Function<CatUnionProductCondition, List<AggregationBuilder>> aggregation,
                                                                                            BiConsumer<CatUnionProductCondition, BoolQueryBuilder> customBoolQueryBuilder,
                                                                                            int from,
                                                                                            int size) {

        SearchSourceBuilder source = searchService.searchSource(condition,
                aggregation,
                this::sort,
                customBoolQueryBuilder,
                CatUnionProductCondition.class,
                from,
                size);

        return elasticSearch7Service.search(source, ElasticSearchIndexEnum.CAT_PRODUCT_INDEX, null, CatRobotHelper::convertUnionProduct, CatRobotHelper::convertUnionProductAggregationMap);
    }

    public void sort(CatUnionProductCondition condition, SearchSourceBuilder source) {
        List<String> sorts = condition.getSorts();
        if (CollectionUtils.isNotEmpty(sorts)) {
            for (String sort : sorts) {
                if (StringUtils.isBlank(sort)) {
                    continue;
                }
                String[] sortArr = sort.split(" ");
                String name = sortArr[0];
                SortOrder sortOrder = SortOrder.DESC;
                if (sortArr.length > 1) {
                    String order = sortArr[1];
                    if (order.equals("asc")) {
                        sortOrder = SortOrder.ASC;
                    }
                }
                if ("commission".equals(name)) {
                    // 自定义排序脚本
                    String code = "return (doc['commissionRate'].value / 100) * (doc['reservePrice'].value / 100)";

                    Script script = new Script(
                            Script.DEFAULT_SCRIPT_TYPE,
                            Script.DEFAULT_SCRIPT_LANG,
                            code,
                            new HashMap<>(),
                            new HashMap<>()
                    );

                    ScriptSortBuilder scriptSortBuilder = SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.NUMBER);
                    scriptSortBuilder.order(sortOrder);
                    source.sort(scriptSortBuilder);
                } else {
                    source.sort(name, sortOrder);
                }
            }
        }
    }

    /**
     * 查询商品索引数据
     * @param condition
     * @param from
     * @param size
     */
    public ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchProduct(CatUnionProductCondition condition,
                                                                                            Function<CatUnionProductCondition, AggregationBuilder> aggregation,
                                                                                            int from,
                                                                                            int size) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, CatUnionProductCondition.class);

        List<String> sorts = condition.getSorts();
        if (CollectionUtils.isNotEmpty(sorts)) {
            for (String sort : sorts) {
                if (StringUtils.isBlank(sort)) {
                    continue;
                }
                String[] sortArr = sort.split(" ");
                String name = sortArr[0];
                SortOrder sortOrder = SortOrder.DESC;
                if (sortArr.length > 1) {
                    String order = sortArr[1];
                    if (order.equals("asc")) {
                        sortOrder = SortOrder.ASC;
                    }
                }
                if ("commission".equals(name)) {
                    // 自定义排序脚本
                    String code = "return (doc['commissionRate'].value / 100) * (doc['reservePrice'].value / 100)";

                    Script script = new Script(
                            Script.DEFAULT_SCRIPT_TYPE,
                            Script.DEFAULT_SCRIPT_LANG,
                            code,
                            new HashMap<>(),
                            new HashMap<>()
                    );

                    ScriptSortBuilder scriptSortBuilder = SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.NUMBER);
                    scriptSortBuilder.order(sortOrder);
                    searchSourceBuilder.sort(scriptSortBuilder);
                } else {
                    searchSourceBuilder.sort(name, sortOrder);
                }
            }
        }
        // ES搜索条件
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.query(boolBuilder);

        AggregationBuilder aggregationBuilder = null;
        if (aggregation != null) {
            aggregationBuilder = aggregation.apply(condition);
        }

        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.CAT_PRODUCT_INDEX,
                aggregationBuilder,
                CatRobotHelper::convertUnionProduct,
                CatRobotHelper::convertUnionProductAggregationMap
                );

        return searchData;
    }

    /**
     *
     * @param condition 条件
     * @param aggregations 聚合
     * @param sort 排序
     * @param customBoolQueryBuilder 自定义build
     * @param from from
     * @param size size
     * @return
     */
    public SearchSourceBuilder searchSource(CatUnionProductCondition condition,
                                            Function<CatUnionProductCondition, List<AggregationBuilder>> aggregations,
                                            BiConsumer<CatUnionProductCondition, SearchSourceBuilder> sort,
                                            BiConsumer<CatUnionProductCondition, BoolQueryBuilder> customBoolQueryBuilder,
                                            int from,
                                            int size) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, CatUnionProductCondition.class);

        if (customBoolQueryBuilder != null) {
            customBoolQueryBuilder.accept(condition, boolBuilder);
        }

        // ES搜索条件
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.query(boolBuilder);

        if (aggregations != null) {
            List<AggregationBuilder> aggs = aggregations.apply(condition);
            if (CollectionUtils.isNotEmpty(aggs)) {
                for (AggregationBuilder agg : aggs) {
                    searchSourceBuilder.aggregation(agg);
                }
            }
        }

        if (sort != null) {
            sort.accept(condition, searchSourceBuilder);
        }

        return searchSourceBuilder;
    }

    public ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> search(SearchSourceBuilder searchSourceBuilder) {

        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.CAT_PRODUCT_INDEX,
                CatRobotHelper::convertUnionProduct,
                CatRobotHelper::convertUnionProductAggregationMap
        );

        return searchData;
    }

    public ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> search(SearchSourceBuilder searchSourceBuilder,
                                                                                     Function<Aggregations, Map<String, List<CatProductBucketTO>>> bucketConverter) {

        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.CAT_PRODUCT_INDEX,
                CatRobotHelper::convertUnionProduct,
                bucketConverter
        );

        return searchData;
    }

    public void indexEs(List<MaocheAlimamaUnionProductDO> items, int corePoolSize) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        List<String> itemSuffixIds = UnionProductHelper.getItemIds(items);
        List<String> iids = UnionProductHelper.getIids(items);

        List<Long> ids = items.stream().map(MaocheAlimamaUnionProductDO::getUiid).toList();

        // 获取标签信息
        List<MaocheAlimamaUnionTitleKeywordDO> keywordDOs = maocheAlimamaUnionTitleKeywordService.listByItemIdSuffixs(itemSuffixIds);
        Map<String, MaocheAlimamaUnionTitleKeywordDO> keywordMap = keywordDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionTitleKeywordDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));

        // 获取有好价信息
//        List<MaocheAlimamaUnionGoodPriceDO> unionGoodPriceDOs = maocheAlimamaUnionGoodPriceService.listByItemIdSuffixs(itemSuffixIds, 3);
//        Map<String, MaocheAlimamaUnionGoodPriceDO> unionGoodPriceMap = unionGoodPriceDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionGoodPriceDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));

        // 商品sku detail
        List<MaocheAlimamaUnionProductDetailDO> productDetailDOs = maocheAlimamaUnionProductDetailService.listByIids(iids);
        Map<String, MaocheAlimamaUnionProductDetailDO> productDetailMap = productDetailDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductDetailDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));

        // 获取价格和优惠信息
        List<MaocheProductV2DO> productV2DOs = maocheProductV2Service.listByProductIds(ids, "NORMAL");
        Map<Long, MaocheProductV2DO> productV2DOMap = productV2DOs.stream().collect(Collectors.toMap(MaocheProductV2DO::getProductId, Function.identity(), (o1, o2) -> o1));

        List<Map<String, Object>> list = new ArrayList<>();
        for (MaocheAlimamaUnionProductDO item : items) {
            try {
                if (!"宠物/宠物食品及用品".equals(item.getLevelOneCategoryName())) {
                    elasticSearch7Service.delIndex(Collections.singletonList(item.getUiid()), ElasticSearchIndexEnum.CAT_PRODUCT_INDEX);
                    continue;
                }

                if (!"NORMAL".equalsIgnoreCase(item.getStatus())) {
                    elasticSearch7Service.delIndex(Collections.singletonList(item.getUiid()), ElasticSearchIndexEnum.CAT_PRODUCT_INDEX);
                    continue;
                }
                MaocheAlimamaUnionTitleKeywordDO titleKeywordDO = keywordMap.get(item.getItemIdSuffix());
//                MaocheAlimamaUnionGoodPriceDO goodPriceDO = unionGoodPriceMap.get(item.getItemIdSuffix());
                MaocheAlimamaUnionProductDetailDO productDetailDO = productDetailMap.get(item.getItemIdSuffix());
//                MaocheAlimamaUnionProductBihaohuoDO priceChartDO = priceChartDOMap.get(item.getIid());
                MaocheProductV2DO productV2DO = productV2DOMap.get(item.getUiid());

//                CarAlimamaUnionProductIndex catIndex = CatEsHelper.buildCatAlimamaUnionProductIndex(item,
//                        titleKeywordDO,
//                        null,
//                        productDetailDO,
//                        null,
//                        productV2DO);

                CarAlimamaUnionProductIndex catIndex = CatEsHelper.buildCatProductIndexV2(item,
                        productDetailDO,
                        productV2DO);

                if (catIndex == null) {
                    continue;
                }
                Map<String, Object> data = JsonUtils.toReferenceType(JSON.toJSONString(catIndex), new TypeReference<Map<String, Object>>() {
                });
                list.add(data);
            } catch (Exception e) {
                // todo yhq npe
                log.error("index error1 item:{} ", JSON.toJSONString(item), e);
            }
        }

        elasticSearch7Service.index(list, ElasticSearchIndexEnum.CAT_PRODUCT_INDEX, "id", corePoolSize);
    }

    public Map<String, MaocheDataokeProductDO> getDaTaoKeProductMap(List<MaocheAlimamaUnionProductDO> items) {
        if (CollectionUtils.isEmpty(items)) {
            return new HashMap<>();
        }

        List<String> ids = new ArrayList<>();
        List<MaocheAlimamaUnionProductDO> daTaoKeItems = new ArrayList<>();
        for (MaocheAlimamaUnionProductDO item : items) {
            if (item == null || !ProductDataSource.DATAOKE.getSource().equals(item.getDataSource())) {
                continue;
            }
            if (StringUtils.isBlank(item.getMaocheInnerId())) {
                continue;
            }
            ids.add(item.getMaocheInnerId());
            daTaoKeItems.add(item);
        }

        // 查表
        List<MaocheDataokeProductDO> products = maocheDataokeProductService.listByIds(ids);
        if (CollectionUtils.isEmpty(products)) {
            return new HashMap<>();
        }

        /** <itemIdSuffix, MaocheDataokeProductDO>*/
        Map<String, MaocheDataokeProductDO> map = new HashMap<>();
        Map<String, MaocheDataokeProductDO> productDOMap = products.stream().collect(Collectors.toMap(MaocheDataokeProductDO::getId, Function.identity(), (o1, o2) -> o1));
        for (MaocheAlimamaUnionProductDO item : daTaoKeItems) {
            MaocheDataokeProductDO maocheDataokeProductDO = productDOMap.get(item.getMaocheInnerId());
            if (maocheDataokeProductDO == null) {
                continue;
            }
            map.put(item.getItemIdSuffix(), maocheDataokeProductDO);
        }

        return map;
    }

    public void delIndex(List<Long> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        elasticSearch7Service.delIndex(ids, ElasticSearchIndexEnum.CAT_PRODUCT_INDEX);
    }

    // todo 废弃，迁移 获取转链链接 com.jeesite.modules.cat.service.cg.third.tb.TbApiService.getCommonCommand
//    public Result<String> getEApiUrl(String vekey, String itemId, String pid) {
//        CloseableHttpClient httpClient = MtxHttpClientUtils.getHttpsClient();
//
//        String method = "GET";
//        // API网址
//        String url = "http://api.veapi.cn/tbk/hcapi_v2?vekey=%s&para=%s&pid=%s&deepcoupon=1";
//        url = String.format(url, vekey, itemId, pid);
//
//        RequestBuilder builder = RequestBuilder.create(method);
//        RequestConfig configBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
//        builder.setConfig(configBuilder);
//        builder.setUri(url);
//
//        String res = "";
//        String errorMsg = "查询失败";
//        try {
//            CloseableHttpResponse response = httpClient.execute(builder.build());
//            HttpEntity entity = response.getEntity();
//            String resp = EntityUtils.toString(entity, "UTF-8");
//
//            JSONObject jsonObject = JSONObject.parseObject(resp);
//            if (jsonObject != null) {
//                JSONObject data = jsonObject.getJSONObject("data");
//                if (data != null) {
//                    res = data.getString("tbk_pwd");
//                }
//                Object o = jsonObject.get("msg");
//                if (o instanceof String) {
//                    errorMsg = (String) o;
//                }
//            }
//            // 说明错误了
//            if (StringUtils.isBlank(res)) {
//                String dingDingMsg = "口令获取异常 itemId：{} \n 获取错误信息：{} \n 原文: {}";
//                dingDingService.sendParseDingDingMsg(dingDingMsg, 1, itemId, errorMsg, resp);
//                return Result.ERROR(500, errorMsg);
//            }
//        } catch (Exception e) {
//            log.error("getAuthUrl 获取授权地址失败", e);
//        }
//
//        if (StringUtils.isNotBlank(res)) {
//            // 替换掉一个￥
//            res = "(" + res.substring(1);
//            // 替换第二个￥
//            res = res.replace("￥", ")");
//
//            res += "/ CA21,)/ AC01";
//        }
//
//        return Result.OK(res);
//    }

    /**
     * https://www.veapi.cn/apidoc/tongyongjiekou/70
     * 从用户提供的电商平台的标题中，提取精准产品词，和相关的品牌词、规格、材质等属性。从而准确理解用户搜索意图。
     * @param title
     * @param vekey
     */
    public String titleKeyWord(String title, String vekey) {
        if (StringUtils.isBlank(title)) {
            return null;
        }

        CloseableHttpClient httpClient = MtxHttpClientUtils.getHttpsClient();

        String method = "GET";
        // API网址
        String url = "http://api.veapi.cn/tbk/titlekeyword?vekey=%s&title=%s";
        url = String.format(url, vekey, title);

        RequestBuilder builder = RequestBuilder.create(method);
        RequestConfig configBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        builder.setConfig(configBuilder);
        builder.setUri(url);

        String res = "";
        try {
//            CloseableHttpResponse response = httpClient.execute(builder.build());
//            HttpEntity entity = response.getEntity();
//            String resp = EntityUtils.toString(entity, "UTF-8");
//
//            JSONObject jsonObject = JSONObject.parseObject(resp);
//            if (jsonObject != null) {
//                JSONObject data = jsonObject.getJSONObject("data");
//                if (data != null) {
//                    res = data.getString("tbk_pwd");
//                }
//            }
        } catch (Exception e) {
            log.error("titleKeyWord 获取授权地址失败", e);
        }

        return res;
    }


    public List<UnionProductTO> listProductInfo(ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData) {
        if (searchData == null) {
            return new ArrayList<>();
        }

        List<CarAlimamaUnionProductIndex> documents = searchData.getDocuments();
        if (CollectionUtils.isEmpty(documents)) {
            return new ArrayList<>();
        }

        return UnionProductHelper.convertUnionProduct(documents);
    }

    public AggregationBuilder buildCatMessagePushAgg(CatUnionProductCondition condition) {

        return AggregationBuilders
                .min("min_coupon_price")
                .field("promotionPrice");
    }

    public List<AggregationBuilder> buildCatRobotPushAgg(CatUnionProductCondition condition) {
        List<AggregationBuilder> builders = new ArrayList<>();
        MinAggregationBuilder aggOne = AggregationBuilders
                .min("min_coupon_price")
                .field("promotionPrice");

        MaxAggregationBuilder aggTwo = AggregationBuilders.max("max_coupon").field("coupon");

        builders.add(aggOne);
        builders.add(aggTwo);
        return builders;
    }

    /**
     * 大类目下的小类目数据
     * @param condition
     * @return
     */
    public List<AggregationBuilder> buildRootCategoryAgg(CatUnionProductCondition condition) {
        long startTime = System.currentTimeMillis();
        List<AggregationBuilder> builders = new ArrayList<>();
        String fieldName = "categoryName";
        String aggNameSuffix = "agg_";
        // 一级类目id
        List<Long> rootCids = maocheCategoryMappingService.getRootCids();
        for (Long cid : rootCids) {
            String name = aggNameSuffix + cid;
            // 获取所有子类目
//            log.info("buildRootCategoryAgg 类目：{} 开始", cid);
            List<MaocheCategoryMappingDO> categories = maocheCategoryMappingService.getCategoryFromCache(cid);
            if (CollectionUtils.isEmpty(categories)) {
                continue;
            }
            List<String> cNames = categories.stream().map(MaocheCategoryMappingDO::getName).collect(Collectors.toList());
            FilterAggregationBuilder builder = AggregationBuilders
                    .filter(name, QueryBuilders.termsQuery(fieldName, cNames))
                    .subAggregation(AggregationBuilders.count(name).field("categoryName"));

            builders.add(builder);
        }
//        log.info("buildRootCategoryAgg 耗时:{}", System.currentTimeMillis() - startTime);


        return builders;
    }

    public void commonSort(CatUnionProductCondition condition, SearchSourceBuilder searchSourceBuilder) {
        if (condition == null || CollectionUtils.isEmpty(condition.getSorts())) {
            return;
        }
        List<String> sorts = condition.getSorts();
        for (String sort : sorts) {
            if (StringUtils.isBlank(sort)) {
                continue;
            }
            String[] sortArr = sort.split(" ");
            String name = sortArr[0];
            SortOrder sortOrder = SortOrder.DESC;
            if (sortArr.length > 1) {
                String order = sortArr[1];
                if (order.equals("asc")) {
                    sortOrder = SortOrder.ASC;
                }
            }
            if ("commission".equals(name)) {
                // 自定义排序脚本
                String code = "return (doc['commissionRate'].value / 100) * (doc['reservePrice'].value / 100)";

                Script script = new Script(
                        Script.DEFAULT_SCRIPT_TYPE,
                        Script.DEFAULT_SCRIPT_LANG,
                        code,
                        new HashMap<>(),
                        new HashMap<>()
                );

                ScriptSortBuilder scriptSortBuilder = SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.NUMBER);
                scriptSortBuilder.order(sortOrder);
                searchSourceBuilder.sort(scriptSortBuilder);
            } else {
                searchSourceBuilder.sort(name, sortOrder);
            }
        }
    }


    public List<UnionProductTO> listByIdsFromEs(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        CatUnionProductCondition unionProductCondition = new CatUnionProductCondition();
        unionProductCondition.setIds(ids);
        SearchSourceBuilder source = searchSource(unionProductCondition, null, null, null, 0, ids.size());
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = search(source);

        if (searchData == null) {
            return new ArrayList<>();
        }

        List<UnionProductTO> productTOs = listProductInfo(searchData);
        return productTOs;
    }
}
