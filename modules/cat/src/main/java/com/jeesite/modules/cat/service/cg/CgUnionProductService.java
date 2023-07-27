package com.jeesite.modules.cat.service.cg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dtk.fetch.client.DtkFetchClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.common.MtxHttpClientUtils;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionTitleKeywordDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionGoodPriceDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductBihaohuoDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductPriceChartDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.entity.MaocheCategoryDO;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.entity.MaocheCategoryProductRelDO;
import com.jeesite.modules.cat.entity.MaocheDataokeProductDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.ProductDataSource;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatEsHelper;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.helper.CategoryHelper;
import com.jeesite.modules.cat.helper.UnionProductHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.ProductCategoryModel;
import com.jeesite.modules.cat.model.UnionProductModel;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionGoodPriceService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductBihaohuoService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductDetailService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductPriceChartService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionTitleKeywordService;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaocheCategoryProductRelService;
import com.jeesite.modules.cat.service.MaocheCategoryService;
import com.jeesite.modules.cat.service.MaocheDataokeProductService;
import com.jeesite.modules.cat.service.stage.cg.ProductEsContext;
import com.jeesite.modules.cat.service.stage.cg.ProductEsFactory;
import com.jeesite.modules.cat.service.stage.cg.ProductEsStage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.checkerframework.checker.units.qual.C;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
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
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.CAT_PRODUCT_INDEX,
                aggregationBuilder,
                CatRobotHelper::convertUnionProduct,
                CatRobotHelper::convertUnionProductAggregationMap
                );

//        stopWatch.stop();
//        log.info("maocheSearch searchSourceBuilder {}, time:{}", JSON.toJSONString(searchSourceBuilder.toString()), stopWatch.toString());

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


        // 获取标签信息
        List<MaocheAlimamaUnionTitleKeywordDO> keywordDOs = maocheAlimamaUnionTitleKeywordService.listByItemIdSuffixs(itemSuffixIds);
        Map<String, MaocheAlimamaUnionTitleKeywordDO> keywordMap = keywordDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionTitleKeywordDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));

        // 获取有好价信息
        List<MaocheAlimamaUnionGoodPriceDO> unionGoodPriceDOs = maocheAlimamaUnionGoodPriceService.listByItemIdSuffixs(itemSuffixIds, 3);
        Map<String, MaocheAlimamaUnionGoodPriceDO> unionGoodPriceMap = unionGoodPriceDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionGoodPriceDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));

        // 商品sku detail
        List<MaocheAlimamaUnionProductDetailDO> productDetailDOs = maocheAlimamaUnionProductDetailService.listByItemIdSuffixs(itemSuffixIds);
        Map<String, MaocheAlimamaUnionProductDetailDO> productDetailMap = productDetailDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductDetailDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));

//        List<MaocheAlimamaUnionProductPriceChartDO> priceChartDOs = maocheAlimamaUnionProductPriceChartService.listLatestChartPrices(iids);
//        Map<String, MaocheAlimamaUnionProductPriceChartDO> priceChartDOMap = priceChartDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductPriceChartDO::getIid, Function.identity(), (o1, o2) -> o1));

        List<MaocheAlimamaUnionProductBihaohuoDO> priceChartDOs = maocheAlimamaUnionProductBihaohuoService.listLatestChartPrices(iids);
        Map<String, MaocheAlimamaUnionProductBihaohuoDO> priceChartDOMap = priceChartDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductBihaohuoDO::getIid, Function.identity(), (o1, o2) -> o1));

        // 获取大淘客的数据
        Map<String, MaocheDataokeProductDO> daTaoKeProductMap = getDaTaoKeProductMap(items);

//         获取全部类目
//        List<CategoryTree> categoryTrees = maocheCategoryService.listAllCategoryFromCache();
//        List<CategoryTree> categoryTrees = new ArrayList<>();
        // 获取类目
//        List<MaocheCategoryProductRelDO> categoryProductRelDOs = maocheCategoryProductRelService.listByItemIdSuffixs(itemSuffixIds);
//        Map<String, List<MaocheCategoryProductRelDO>> categoryRelMap = new HashMap<>();
//        // 分组
//        for (MaocheCategoryProductRelDO categoryProductRelDO : categoryProductRelDOs) {
//            List<MaocheCategoryProductRelDO> rels = categoryRelMap.get(categoryProductRelDO.getItemIdSuffix());
//            if (CollectionUtils.isEmpty(rels)) {
//                rels = new ArrayList<>();
//            }
//            rels.add(categoryProductRelDO);
//            categoryRelMap.put(categoryProductRelDO.getItemIdSuffix(), rels);
//        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (MaocheAlimamaUnionProductDO item : items) {
            try {
                if (!"宠物/宠物食品及用品".equals(item.getLevelOneCategoryName())) {
                    elasticSearch7Service.delIndex(Collections.singletonList(item.getUiid()), ElasticSearchIndexEnum.CAT_PRODUCT_INDEX);
                    continue;
                }

                if (!"NORMAL".equalsIgnoreCase(item.getStatus())) {
//                    log.info("del product item:{} \n", JsonUtils.toJSONString(item));
                    elasticSearch7Service.delIndex(Collections.singletonList(item.getUiid()), ElasticSearchIndexEnum.CAT_PRODUCT_INDEX);
                    continue;
                }
//                List<MaocheCategoryProductRelDO> rels = categoryRelMap.get(item.getItemIdSuffix());
                MaocheAlimamaUnionTitleKeywordDO titleKeywordDO = keywordMap.get(item.getItemIdSuffix());
                MaocheAlimamaUnionGoodPriceDO goodPriceDO = unionGoodPriceMap.get(item.getItemIdSuffix());
                MaocheAlimamaUnionProductDetailDO productDetailDO = productDetailMap.get(item.getItemIdSuffix());
                MaocheAlimamaUnionProductBihaohuoDO priceChartDO = priceChartDOMap.get(item.getIid());
//                ProductCategoryModel productCategory = CategoryHelper.getRelProductCategory(rels, categoryTrees);

                ProductEsContext context = new ProductEsContext();
                context.setItem(item);
                context.setDaTaoKeProduct(daTaoKeProductMap.get(item.getItemIdSuffix()));
                context.setProductDetailDO(productDetailDO);
                context.setPriceChartDO(priceChartDO);
                ProductEsStage stage = productEsFactory.getStage(item.getDataSource());

                CarAlimamaUnionProductIndex catIndex = null;
                if (stage != null) {
                    Object convert = stage.convert(context);
                    if (convert instanceof UnionProductModel model) {
                        catIndex = CatEsHelper.buildCatUnionProductIndex(model, context);
                    }
                }

                if (catIndex == null) {
                    catIndex = CatEsHelper.buildCatAlimamaUnionProductIndex(item,
                            titleKeywordDO,
                            goodPriceDO,
                            null,
                            productDetailDO,
                            priceChartDO);
                }


                if (catIndex == null) {
                    continue;
                }
                Map<String, Object> data = JsonUtils.toReferenceType(JSON.toJSONString(catIndex), new TypeReference<Map<String, Object>>() {
                });
                list.add(data);
            } catch (Exception e) {
                log.error("index error item:{} ", JSON.toJSONString(item), e);
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

    // 获取转链链接
    public String getEApiUrl(String vekey, String itemId, String pid) {
        CloseableHttpClient httpClient = MtxHttpClientUtils.getHttpsClient();

        String method = "GET";
        // API网址
        String url = "http://api.veapi.cn/tbk/hcapi_v2?vekey=%s&para=%s&pid=%s&";
        url = String.format(url, vekey, itemId, pid);

        RequestBuilder builder = RequestBuilder.create(method);
        RequestConfig configBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        builder.setConfig(configBuilder);
        builder.setUri(url);

        String res = "";
        try {
            CloseableHttpResponse response = httpClient.execute(builder.build());
            HttpEntity entity = response.getEntity();
            String resp = EntityUtils.toString(entity, "UTF-8");

            JSONObject jsonObject = JSONObject.parseObject(resp);
            if (jsonObject != null) {
                JSONObject data = jsonObject.getJSONObject("data");
                if (data != null) {
                    res = data.getString("tbk_pwd");
                }
            }
        } catch (Exception e) {
            log.error("getAuthUrl 获取授权地址失败", e);
        }

        if (StringUtils.isNotBlank(res)) {
            // 替换掉一个￥
            res = "(" + res.substring(1);
            // 替换第二个￥
            res = res.replace("￥", ")");

            res += "/ CA21,)/ AC01";
        }

        return res;
    }

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
        List<Long> ids = documents.stream().map(CarAlimamaUnionProductIndex::getId).toList();

//        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductDao.listByIds(ids);
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductDao.listSimpleByIds(ids);

        // 获取到商品itemId
        List<String> itemIds = UnionProductHelper.getItemIds(productDOs);
        List<String> iids = UnionProductHelper.getIids(productDOs);

        // 获取标签信息
        List<MaocheAlimamaUnionTitleKeywordDO> keywordDOs = maocheAlimamaUnionTitleKeywordService.listByItemIdSuffixs(itemIds);

        // 获取有好价信息
        List<MaocheAlimamaUnionGoodPriceDO> unionGoodPriceDOs = maocheAlimamaUnionGoodPriceService.listByItemIdSuffixs(itemIds, 3);

//        List<MaocheAlimamaUnionProductPriceChartDO> priceChartDOs = maocheAlimamaUnionProductPriceChartService.listByIids(iids);

        // 获取历史价信息

        // 获取sku 详情
//        List<MaocheAlimamaUnionProductDetailDO> productDetailDOs = maocheAlimamaUnionProductDetailService.listByItemIdSuffixs(itemIds);

        // 获取大淘客的数据
//        Map<String, MaocheDataokeProductDO> daTaoKeProductMap = getDaTaoKeProductMap(productDOs);

        /*List<Long> cidOnes = new ArrayList<>();
        // 一级类目
        for (CarAlimamaUnionProductIndex item : documents) {
            if (CollectionUtils.isEmpty(item.getCidOnes())) {
                continue;
            }
            cidOnes.addAll(item.getCidOnes());
        }
        List<MaocheCategoryDO> categoryDOs = maocheCategoryService.listByIds(cidOnes);*/
        // 商品自定义类目
//        List<MaocheCategoryProductRelDO> maocheCategoryProductRelDOs = maocheCategoryProductRelService.listByItemIdSuffixs(itemIds);


        List<UnionProductTO> unionProducts = UnionProductHelper.convertUnionProduct(documents,
                productDOs,
                keywordDOs,
                unionGoodPriceDOs,
                null,
                new ArrayList<>()
//                ,
//                priceChartDOs
        );

        return unionProducts;
    }

    public AggregationBuilder buildWarehouseAgg(CatUnionProductCondition condition) {

        BucketOrder bucketOrder = BucketOrder.count(false);

        return AggregationBuilders
                .terms("by_category")
                .field("categoryName")
                .order(bucketOrder)
                .size(10);
    }

    public AggregationBuilder buildLevelOneCategoryAgg(CatUnionProductCondition condition) {

        BucketOrder bucketOrder = BucketOrder.count(false);

        return AggregationBuilders
                .terms("by_level_one_category")
                .field("levelOneCategoryName")
                .order(bucketOrder)
                .size(10);
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

        List<AggregationBuilder> builders = new ArrayList<>();
        String fieldName = "categoryName";
        String aggNameSuffix = "agg_";
        // 一级类目id
        List<Long> rootCids = maocheCategoryMappingService.getRootCids();
        for (Long cid : rootCids) {
            String name = aggNameSuffix + cid;
            // 获取所有子类目
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

    public static void main(String[] args) {

        String a1 = "该商品当前低于去年促销价";
        String a2 = "该商品低于全网同款均价XX%";
        String a3 = "该商品当前为近XX天最低价";

        String match = "低于";
        int i = StringUtils.indexOf(a2, match);
        System.out.println(i);

        System.out.println(a2.substring(0, i));
        System.out.println(a2.substring(i));
    }
}
