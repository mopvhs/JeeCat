package com.jeesite.modules.cat.service.cg;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheDataokeProductDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionGoodPriceDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.entity.MaocheDataokeProductDO;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.CatActivityEnum;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.ProductDataSource;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.helper.ProductValueHelper;
import com.jeesite.modules.cat.helper.dataoke.DaTaoKeResponseHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.model.dataoke.DaTaoKeResponse;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionGoodPriceService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaocheSenderTaskService;
import com.jeesite.modules.cat.service.message.QwService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@EnableScheduling
@Component
public class CgUnionProductStatisticsService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheSenderTaskService maocheSenderTaskService;

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;
    
    @Resource
    private MaocheDataokeProductDao maocheDataokeProductDao;

    @Resource
    private DaTaoKeApiService daTaoKeApiService;

    @Resource
    private CacheService cacheService;

    @Resource
    private MaocheCategoryMappingService maocheCategoryMappingService;

    @Resource
    private MaocheAlimamaUnionGoodPriceService maocheAlimamaUnionGoodPriceService;

    private static final String UNIQUE_ID = "3dd41c9e0df2aab04cb8f1c1afc359b8.py";

    @Resource
    private QwService qwService;

    // todo 任务间隔30分钟
//    @Scheduled(cron = "0 50 7,10,18 * * ?")
    public String runJob() {
        // 判断是否执行，以及更新的内容
        // 获取当前时间
        int hour = LocalTime.now().getHour();

        // TODO
//        Map<Integer, Long> idMap = new HashMap<>();
//        idMap.put(7, 1L);
//        idMap.put(10, 1L);
//        idMap.put(18, 1L);
//        if (!idMap.containsKey(hour)) {
//            return;
//        }
//        Long id = idMap.get(hour);
        statistics2();

        return "success";
    }

    // 每日统计
    public String statistics() {
        // 时间，选品库，
        // 时间，选品库，有好价 + top2 catDsr

        /**
         【早安】，送上今日猫车精选播报：

         今日在库商品：32564件，上架商品：1543件

         今日历史低价商品精选：

         （凑单可227元）比乐猫粮全价无谷猫粮10kg，券后 259， (1QPqdrTpwjg)/ HU89
         （凑单可227元）比乐猫粮全价无谷猫粮10kg，券后 259， (1QPqdrTpwjg)/ HU89
         （凑单可227元）比乐猫粮全价无谷猫粮10kg，券后 259， (1QPqdrTpwjg)/ HU89

         今日新上架金选商品：

         （凑单可227元）比乐猫粮全价无谷猫粮10kg，券后 259， (1QPqdrTpwjg)/ HU89
         （凑单可227元）比乐猫粮全价无谷猫粮10kg，券后 259， (1QPqdrTpwjg)/ HU89
         （凑单可227元）比乐猫粮全价无谷猫粮10kg，券后 259， (1QPqdrTpwjg)/ HU89


         更多好物，欢迎群内@猫车选品官，带上关键词即可。
         找券/答疑@猫车小助手
         */
        int passStatus = AuditStatusEnum.PASS.getStatus();
        long saleStatus = SaleStatusEnum.ON_SHELF.getStatus();

        // 在库商品数
        long passProductNum = 0;
        long saleProductNum = 0;

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setAuditStatus(passStatus);
        // 获取总数量
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition,
                null,
                0,
                0);

        // 上架商品数
        if (searchData == null) {
            return null;
        }

        passProductNum = searchData.getTotal();
        condition.setSaleStatus(saleStatus);
        // 获取总数量
        searchData = cgUnionProductService.searchProduct(condition,
                null,
                0,
                0);

        if (searchData != null) {
            saleProductNum = searchData.getTotal();
        }

        StringBuilder builder = new StringBuilder();
        String hi = sayHi();


        builder.append("【").append(hi).append("】，送上今日猫车精选播报：\n");
        builder.append("今日在库商品：").append(passProductNum).append("，上架商品：").append(saleProductNum).append("\n");

        // 金选
        List<String> historyLowPriceList = getHistoryLowPriceList();
        if (CollectionUtils.isNotEmpty(historyLowPriceList)) {
            builder.append("今日历史低价商品精选：\n");
            for (String msg : historyLowPriceList) {
                builder.append(msg).append("\n");
            }
        }

        // 今日上架精选
        List<String> todayNew = todayNew();
        if (CollectionUtils.isNotEmpty(todayNew)) {
            builder.append("今日新上架金选商品：\n");
            for (String msg : todayNew) {
                builder.append(msg).append("\n");
            }
        }

        return builder.toString();
    }

    // 每日统计
    public void statistics2() {

        List<MaocheCategoryMappingDO> roots = maocheCategoryMappingService.getCategoryFromCache(0L);

        int passStatus = AuditStatusEnum.PASS.getStatus();
        long saleStatus = SaleStatusEnum.ON_SHELF.getStatus();
        List<String> sorts = Collections.singletonList("catDsr desc");
        List<String> activity = Collections.singletonList(CatActivityEnum.GOOD_PRICE.getActivity());

        String titleFormat = "今日%s好价：";
        // 每段文案
        List<String> stepList = new ArrayList<>();

        for (MaocheCategoryMappingDO mappingDO : roots) {
            List<MaocheCategoryMappingDO> subCategories = maocheCategoryMappingService.getCategoryFromCache(mappingDO.getIid());
            if (CollectionUtils.isEmpty(subCategories)) {
                continue;
            }

            List<String> categoryNames = subCategories.stream().map(MaocheCategoryMappingDO::getName).collect(Collectors.toList());
            CatUnionProductCondition categoryCondition = new CatUnionProductCondition();
            categoryCondition.setCategoryNames(categoryNames);
            categoryCondition.setActivity(activity);
            categoryCondition.setGteShopDsr(48000);
//            categoryCondition.setSaleStatus(saleStatus);
            String key = "daily_push_filter_ids_" + mappingDO.getId();
            String filterIdsStr = cacheService.get(key);
            List<Long> filterIds = new ArrayList<>();
            if (StringUtils.isNotBlank(filterIdsStr)) {
                filterIds = Optional.ofNullable(JsonUtils.toReferenceType(filterIdsStr, new TypeReference<List<Long>>() {
                })).orElse(new ArrayList<>());
                categoryCondition.setFilterIds(filterIds);
            }
            categoryCondition.setSorts(sorts);

            SearchSourceBuilder source = cgUnionProductService.searchSource(categoryCondition, null, cgUnionProductService::commonSort, null, 0, 3);
            ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(source);

            if (searchData == null) {
                // 重置redis
                categoryCondition.setFilterIds(null);
                source = cgUnionProductService.searchSource(categoryCondition, null, cgUnionProductService::commonSort, null, 0, 3);
                searchData = cgUnionProductService.search(source);
                if (searchData != null && CollectionUtils.isNotEmpty(searchData.getDocuments())) {
                    // 重置filterIds
                    filterIds = searchData.getDocuments().stream().map(CarAlimamaUnionProductIndex::getId).collect(Collectors.toList());
                }
            }
            List<String> products = new ArrayList<>();
            /**
             * 雷米高猫咪维生素b防掉毛营养补充剂（取短标题）
             * 券后 6.9，该商品当前低于去年促销价
             * (1QPqdrTpwjg)/ HU89
             */
            String formatProduct = "%s\n券后 %s，%s\n%s\n";
            if (searchData != null && CollectionUtils.isNotEmpty(searchData.getDocuments())) {
                List<Long> ids = searchData.getDocuments().stream().map(CarAlimamaUnionProductIndex::getId).toList();

                products = processProduct(searchData.getDocuments());
                filterIds.addAll(ids);
                filterIds = filterIds.stream().distinct().collect(Collectors.toList());
                cacheService.set(key, JsonUtils.toJSONString(filterIds));
                cacheService.expire(key, 3600);
            }
            if (CollectionUtils.isEmpty(products)) {
                continue;
            }
            String title = String.format(titleFormat, mappingDO.getName());
            String builder = title + "\n\n" +
                    StringUtils.join(products, "\n") + "\n\n";

            stepList.add(builder);
        }

        // 在库商品数
        List<Long> totalNum = getTotalNum();
        long passProductNum = totalNum.get(0);
        long saleProductNum = totalNum.get(1);

        StringBuilder header = new StringBuilder();
        // 【早安】，送上今日猫车精选播报：
        //今日在库商品：32564件，精选上架商品：1543件
        header.append("【").append(sayHi()).append("】").append("，送上今日猫车精选播报：\n");
        header.append("今日在库商品：").append(passProductNum).append("件，精选上架商品：").append(saleProductNum).append("件\n\n");

        StringBuilder tail = new StringBuilder();
        tail.append("更多好物，欢迎群内@猫车选品官，带上关键词即可，\n找券/答疑@猫车小助手");

        send(header.toString(), tail.toString(), stepList);

        return;
    }

    private List<String> processProduct(List<CarAlimamaUnionProductIndex> indices) {

        List<Long> ids = indices.stream().map(CarAlimamaUnionProductIndex::getId).toList();

        MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
        query.setIid_in(ids.toArray(Long[]::new));
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductDao.findList(query);
        Map<Long, MaocheAlimamaUnionProductDO> productDOMap = productDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductDO::getIid, Function.identity(), (o1, o2) -> o1));

        List<String> itemIdSuffixs = productDOs.stream().map(MaocheAlimamaUnionProductDO::getItemIdSuffix).collect(Collectors.toList());
        List<MaocheAlimamaUnionGoodPriceDO> priceDOs = maocheAlimamaUnionGoodPriceService.listByItemIdSuffixs(itemIdSuffixs);
        Map<String, MaocheAlimamaUnionGoodPriceDO> goodProductMap = priceDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionGoodPriceDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));


        List<String> products = new ArrayList<>();

        String formatProduct = "%s\n券后 %s%s\n%s\n";
        for (CarAlimamaUnionProductIndex index : indices) {
            MaocheAlimamaUnionProductDO unionProductDO = productDOMap.get(index.getId());
            String shortTitle = ProductValueHelper.getShortTitle(JSONObject.parseObject(unionProductDO.getOrigContent()));
            String title = index.getTitle();
            if (StringUtils.isNotBlank(shortTitle)) {
                title = shortTitle;
            }
            String goodPriceType = "";
            MaocheAlimamaUnionGoodPriceDO goodPrice = goodProductMap.get(unionProductDO.getItemIdSuffix());
            try {
                if (goodPrice != null) {
                    goodPriceType = JSONObject.parseObject(goodPrice.getContent()).getString("goodPriceType");
                    goodPriceType = "，该商品当前" + goodPriceType;
                }
            } catch (Exception e) {
                log.error("每日推荐，解析有好价失败 goodPrice:{}", JsonUtils.toJSONString(goodPrice), e);
            }

            // 获取口令
            String apiUrl = cgUnionProductService.getEApiUrl("V73687541H40026415", unionProductDO.getItemId(), "mm_30153430_909250463_109464700418");
            products.add(String.format(formatProduct, title, formatPrice(index.getPromotionPrice()), goodPriceType, apiUrl));
        }

        return products;
    }

    private void send(String header, String tail, List<String> products) {
        if (CollectionUtils.isEmpty(products)) {
            return;
        }
        int size = products.size();

        for (int i = 0; i < size; i++) {
            String content = "";
            if (i == 0) {
                content = header;
            }
            content += products.get(i);
            if (i == size - 1) {
                content += tail;
            }
            qwService.send(UNIQUE_ID, textMap(content));
            try {
                Thread.sleep(3000);
            } catch (Exception e) {

            }
        }
    }

    private Map<String, Object> textMap(String content) {
        Map<String, Object> msg = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("conversation_id", "R:10798108093991504");
        data.put("content", content);
        data.put("request_key", "AAAAAA");
//        data.put("thumburl", "thumburl");
//        data.put("title", "tttt title");
//        data.put("url", "www.baidu.com");
        msg.put("data", data);
        msg.put("type", 5000);

        return msg;
    }

    private List<Long> getTotalNum() {

        List<Long> numResult = new ArrayList<>();
        // 在库商品数
        long passProductNum = 0;
        long saleProductNum = 0;

        int passStatus = AuditStatusEnum.PASS.getStatus();
        long saleStatus = SaleStatusEnum.ON_SHELF.getStatus();

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setAuditStatus(passStatus);
        // 获取总数量
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition,
                null,
                0,
                0);

        // 上架商品数
        if (searchData != null) {
            passProductNum = searchData.getTotal();
        }

        condition.setSaleStatus(saleStatus);
        // 获取总数量
        searchData = cgUnionProductService.searchProduct(condition,
                null,
                0,
                0);

        if (searchData != null) {
            saleProductNum = searchData.getTotal();
        }

        numResult.add(passProductNum);
        numResult.add(saleProductNum);

        return numResult;
    }

    /**
     * 今日新上架金选商品
     *
     */
    private List<String> todayNew() {

        // 今日上架精选
        long starTime = DateTimeUtils.earliestTimeToday(System.currentTimeMillis());
        int passStatus = AuditStatusEnum.PASS.getStatus();
        long saleStatus = SaleStatusEnum.ON_SHELF.getStatus();

        List<String> sorts = new ArrayList<>();
        sorts.add("catDsr desc");

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setAuditStatus(passStatus);
        condition.setSaleStatus(saleStatus);
        // todo 放开
        condition.setGteCreateTime(starTime);
        condition.setSorts(sorts);

        // 获取总数量
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition,
                null,
                0,
                3);

        if (searchData == null || searchData.getTotal() <= 0) {
            // 查询今日更新的
            condition.setGteUpdateTime(null);
            // todo 放开
            condition.setGteCreateTime(starTime);
            searchData = cgUnionProductService.searchProduct(condition,
                    null,
                    0,
                    20);
        }

        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return null;
        }
        MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
        query.setIid_in(searchData.getDocuments().stream().map(CarAlimamaUnionProductIndex::getId).toArray(Long[]::new));
        List<MaocheAlimamaUnionProductDO> products = maocheAlimamaUnionProductDao.findList(query);
        Map<Long, MaocheAlimamaUnionProductDO> productDOMap = products.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductDO::getIid, Function.identity(), (o1, o2) -> o1));

        List<String> msg = new ArrayList<>();
        String format = "%s，券后 %s，%s";
        List<CarAlimamaUnionProductIndex> items = searchData.getDocuments().subList(0, Math.min(searchData.getDocuments().size(), 3));
        for (CarAlimamaUnionProductIndex index : items) {

            MaocheAlimamaUnionProductDO unionProductDO = productDOMap.get(index.getId());

            // 获取口令
            String apiUrl = cgUnionProductService.getEApiUrl("V73687541H40026415", unionProductDO.getItemId(), "mm_30153430_909250463_109464700418");

             msg.add(String.format(format, index.getTitle(), formatPrice(index.getPromotionPrice()), apiUrl));
        }

        return msg;
    }

    private static String formatPrice(Long price) {
        if (price == null || price <= 0) {
            return null;
        }

        return new BigDecimal(price).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).toString();
    }

    private static String sayHi() {
        // 获取当前时间
        int hour = LocalTime.now().getHour();

        if (hour <= 10) {
            return "早安";
        } else if (hour <= 13) {
            return "午安";
        }

        return "晚安";
    }

    public static void main(String[] args) {
        int pageId = 1;
        int pageSize = 20;
        long total = 40;
        System.out.println(total / pageSize);

    }

    // 获取所有的历史低价商品

    public List<String> getHistoryLowPriceList() {
        int pageId = 1;
        int pageSize = 20;
        String cids = "4";

        DaTaoKeResponse<Object> historyLowPriceList = daTaoKeApiService.getHistoryLowPriceList("647802ed3a2b4", "v1.0.0", pageSize, pageId, cids, "0");

        long total = DaTaoKeResponseHelper.getHistoryLowPriceTotal(JsonUtils.toJSONString(historyLowPriceList.getData()));
        // 获取需要翻页的总数
        if (total <= 0) {
            return new ArrayList<>();
        }
        List<Long> historyLowPriceIds = DaTaoKeResponseHelper.getHistoryLowPriceIds(JsonUtils.toJSONString(historyLowPriceList.getData()));
        if (CollectionUtils.isEmpty(historyLowPriceIds)) {
            log.error("解析大淘客的历史低价商品接口异常，无法获取大淘客id, historyLowPriceList:{}", JsonUtils.toJSONString(historyLowPriceList));
            return new ArrayList<>();
        }


        long totalSize = total / pageSize + 1;
        for (int i = 2; i <= totalSize; i++) {
            historyLowPriceList = daTaoKeApiService.getHistoryLowPriceList("647802ed3a2b4", "v1.0.0", pageSize, i, cids, "0");

            List<Long> ids = DaTaoKeResponseHelper.getHistoryLowPriceIds(JsonUtils.toJSONString(historyLowPriceList.getData()));
            if (CollectionUtils.isEmpty(ids)) {
                continue;
            }
            historyLowPriceIds.addAll(ids);
        }

        List<MaocheDataokeProductDO> daTaoKeProducts = new ArrayList<>();

        List<List<Long>> partition = Lists.partition(historyLowPriceIds, 20);
        for (List<Long> p : partition) {
            // 分批查询
            MaocheDataokeProductDO query = new MaocheDataokeProductDO();
            query.setDtkId_in(p.toArray(new Long[0]));
            query.setStatus("NORMAL");
            List<MaocheDataokeProductDO> list = maocheDataokeProductDao.findList(query);
            if (CollectionUtils.isNotEmpty(list)) {
                daTaoKeProducts.addAll(list);
            }
        }

        List<String> innerIds = daTaoKeProducts.stream().map(MaocheDataokeProductDO::getId).collect(Collectors.toList());
        // 查询在union_product中也存在的商品
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductService.listByMaocheInnerIds(innerIds,
                ProductDataSource.DATAOKE,
                "宠物/宠物食品及用品",
                "NORML");
        List<MaocheAlimamaUnionProductDO> searchProducts = new ArrayList<>();
        List<Long> productIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(productDOs)) {
            // 过滤掉无效的商品
            for (MaocheAlimamaUnionProductDO product : productDOs) {
                if (!"NORMAL".equals(product.getStatus())) {
                    continue;
                }
                if (product.getAuditStatus() == null || product.getSaleStatus() == null) {
                    continue;
                }
                if (!SaleStatusEnum.ON_SHELF.getStatus().equals(product.getSaleStatus())) {
                    continue;
                }
                if (AuditStatusEnum.PASS.getStatus() != product.getAuditStatus().intValue()) {
                    continue;
                }

                productIds.add(product.getIid());
                searchProducts.add(product);
            }
        }

        if (CollectionUtils.isEmpty(searchProducts)) {
            return new ArrayList<>();
        }

        Map<Long, MaocheAlimamaUnionProductDO> productDOMap = searchProducts.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductDO::getIid, Function.identity(), (o1, o2) -> o1));

        List<CarAlimamaUnionProductIndex> indices = new ArrayList<>();
        // 获取到所有的id，然后查询es，每100个id查询一次
        List<List<Long>> pids = Lists.partition(productIds, 100);
        for (List<Long> p : pids) {
            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            queryBuilder.must(QueryBuilders.termsQuery("id", productIds));
            queryBuilder.must(QueryBuilders.termQuery("saleStatus", SaleStatusEnum.ON_SHELF.getStatus()));
            queryBuilder.must(QueryBuilders.termQuery("auditStatus", AuditStatusEnum.PASS.getStatus()));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(0)
                    .size(p.size())
                    .query(queryBuilder);

            ElasticSearchData<CarAlimamaUnionProductIndex, Object> searchData = elasticSearch7Service.search(searchSourceBuilder,
                    ElasticSearchIndexEnum.CAT_PRODUCT_INDEX,
                    null,
                    CatRobotHelper::convertUnionProduct,
                    null);

            if (searchData == null || searchData.getTotal() <= 0) {
                continue;
            }
            indices.addAll(searchData.getDocuments());
        }

        // 按照字段排序
        indices.sort(new Comparator<CarAlimamaUnionProductIndex>() {
            @Override
            public int compare(CarAlimamaUnionProductIndex o1, CarAlimamaUnionProductIndex o2) {
                return (int) (o2.getCatDsr() - o1.getCatDsr());
            }
        });

        List<String> msg = new ArrayList<>();

//        indices = indices.subList(0, Math.min(indices.size(), 10));
        Map<String, MaocheDataokeProductDO> dataokeProductDOMap = daTaoKeProducts.stream().collect(Collectors.toMap(MaocheDataokeProductDO::getId, Function.identity(), (o1, o2) -> o1));
        int i = 3;
        String format = "%s，券后 %s，%s";

        for (CarAlimamaUnionProductIndex index : indices) {

            MaocheAlimamaUnionProductDO unionProductDO = productDOMap.get(index.getId());
            if (unionProductDO == null) {
                continue;
            }
            MaocheDataokeProductDO maocheDataokeProductDO = dataokeProductDOMap.get(unionProductDO.getMaocheInnerId());
            if (maocheDataokeProductDO == null) {
                continue;
            }
            if (msg.size() >= i) {
                continue;
            }
            // 获取dtitle
            String dTitle = index.getTitle();
            String origContent = maocheDataokeProductDO.getOrigContent();
            if (StringUtils.isNotBlank(origContent)) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(origContent);
                    if (jsonObject != null && jsonObject.get("dtitle") != null) {
                        String tmp = jsonObject.getString("dTitle");
                        if (StringUtils.isNotBlank(tmp)) {
                            dTitle = tmp;
                        }

                    }
                } catch (Exception e) {

                }
            }

            // 获取口令
            String apiUrl = cgUnionProductService.getEApiUrl("V73687541H40026415", unionProductDO.getItemId(), "mm_30153430_909250463_109464700418");

            msg.add(String.format(format, dTitle, formatPrice(index.getPromotionPrice()), apiUrl));
        }

        return msg;
    }

}
