package com.jeesite.modules.cat.service.cg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.codec.EncodeUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheDataokeProductDao;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.entity.QwChatroomInfoDO;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.CatActivityEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.PriceHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionGoodPriceService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaocheSenderTaskService;
import com.jeesite.modules.cat.service.QwChatroomInfoService;
import com.jeesite.modules.cat.service.helper.ProductSearchHelper;
import com.jeesite.modules.cat.service.message.QwService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Resource
    private QwChatroomInfoService qwChatroomInfoService;

    private static final String UNIQUE_ID = "3dd41c9e0df2aab04cb8f1c1afc359b8.py";

    @Resource
    private QwService qwService;

    // 每日统计
    public void statistics() {

        List<MaocheCategoryMappingDO> roots = maocheCategoryMappingService.getCategoryFromCache(0L);

        List<String> sorts = new ArrayList<>(Collections.singletonList("catDsr desc"));
        sorts.add("updateTime desc");
        List<String> activity = Collections.singletonList(CatActivityEnum.GOOD_PRICE.getActivity());

        // 每段文案
        long starTime = DateTimeUtils.earliestTimeToday(System.currentTimeMillis());
        List<Content> contents = new ArrayList<>();

        Long nDay = 86400L * 3L * 1000L;

        for (MaocheCategoryMappingDO mappingDO : roots) {
            List<MaocheCategoryMappingDO> subCategories = maocheCategoryMappingService.getCategoryFromCache(mappingDO.getIid());
            if (CollectionUtils.isEmpty(subCategories)) {
                continue;
            }

            List<String> categoryNames = subCategories.stream().map(MaocheCategoryMappingDO::getName).collect(Collectors.toList());
            CatUnionProductCondition categoryCondition = new CatUnionProductCondition();
            categoryCondition.setCategoryNames(categoryNames);
            categoryCondition.setActivity(activity);
            categoryCondition.setAuditStatus(AuditStatusEnum.PASS.getStatus());
            categoryCondition.setHadRates(true);
            categoryCondition.setGteShopDsr(48000);
            // 5%
            categoryCondition.setGteCommissionRate(500L);
            categoryCondition.setGteCouponRemainCount(1L);
            // todo
            categoryCondition.setGteUpdateTime(System.currentTimeMillis() - nDay);
            String key = "daily_good_product_filter_ids_" + mappingDO.getId();
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
                categoryCondition.setGteUpdateTime(null);
                source = cgUnionProductService.searchSource(categoryCondition, null, cgUnionProductService::commonSort, null, 0, 3);
                searchData = cgUnionProductService.search(source);
                if (searchData != null && CollectionUtils.isNotEmpty(searchData.getDocuments())) {
                    // 重置filterIds
                    filterIds = searchData.getDocuments().stream().map(CarAlimamaUnionProductIndex::getId).collect(Collectors.toList());
                }
            }

            if (searchData != null && CollectionUtils.isNotEmpty(searchData.getDocuments())) {
                List<Long> ids = searchData.getDocuments().stream().map(CarAlimamaUnionProductIndex::getId).toList();

                Content content = processProductProps(searchData, mappingDO);
                filterIds.addAll(ids);
                filterIds = filterIds.stream().distinct().collect(Collectors.toList());
                cacheService.set(key, JsonUtils.toJSONString(filterIds));
                // 半天
                cacheService.expire(key, 43200);

                if (StringUtils.isBlank(content.getText())) {
                    continue;
                }

                contents.add(content);

            }
        }

        if (CollectionUtils.isEmpty(contents)) {
            return;
        }

        // 在库商品数
        List<Long> totalNum = getTotalNum();
        long passProductNum = totalNum.get(0);
        long saleProductNum = totalNum.get(1);
        long todayGoodProductNum = totalNum.get(2);
        long totalGoodProductNum = totalNum.get(3);


        // 【早安】，送上今日猫车精选播报：
        //今日在库商品：32564件，精选上架商品：1543件
        String header = sayHi() + "，送上“今日有好价”播报\n" +
                "功能说明：\n" +
                "推送今日获取到的所有近期低价，低于同款商品低价或低于去年促销价的商品，店铺均分≥4.8\n" +
                "有好价商品：共计" + totalGoodProductNum + "件，今日新增：" + todayGoodProductNum + "件\n";

        simpleSend(header, "更多好物，欢迎群内@猫车选品官，带上关键词即可\n找券/答疑@猫车小助手", contents);
    }

    private Content processProductProps(ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData, MaocheCategoryMappingDO mappingDO) {

        Content content = new Content();

        List<CarAlimamaUnionProductIndex> indices = searchData.getDocuments();

//        List<Long> ids = indices.stream().map(CarAlimamaUnionProductIndex::getId).toList();

//        MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
//        query.setIid_in(ids.toArray(Long[]::new));
//        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductDao.findList(query);
//        Map<Long, MaocheAlimamaUnionProductDO> productDOMap = productDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductDO::getIid, Function.identity(), (o1, o2) -> o1));
//
//        List<String> itemIdSuffixs = productDOs.stream().map(MaocheAlimamaUnionProductDO::getItemIdSuffix).collect(Collectors.toList());
//        List<MaocheAlimamaUnionGoodPriceDO> priceDOs = maocheAlimamaUnionGoodPriceService.listByItemIdSuffixs(itemIdSuffixs);
//        Map<String, MaocheAlimamaUnionGoodPriceDO> goodProductMap = priceDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionGoodPriceDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));


        // 有好价猫粮158件，包含品牌：高爷家，渴望，网易严选，花麻麻...等，包含产品：身份牌，猫粮，冻干，罐头...等
        String formatProduct = "有好价%s%d件，包含品牌：%s...等，包含产品：%s...等\n";
        String formatProduct2 = "有好价%s%d件，包含品牌：%s...等\n";
        List<String> brands = new ArrayList<>();
        List<String> productNames = new ArrayList<>();
        for (CarAlimamaUnionProductIndex index : indices) {
            if (StringUtils.isNotBlank(index.getPropsBrand())) {
                brands.add(index.getPropsBrand());
            }
            if (StringUtils.isNotBlank(index.getPropsProductName())) {
                productNames.add(index.getPropsProductName());
            }
        }
        String categoryName = mappingDO.getName();

        String text = null;
        brands = brands.stream().distinct().collect(Collectors.toList());
        productNames = productNames.stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(productNames)) {
            text = String.format(formatProduct2, categoryName, searchData.getTotal(), StringUtils.join(brands, "，"));
        } else {
            text = String.format(formatProduct, categoryName, searchData.getTotal(), StringUtils.join(brands, "，"), StringUtils.join(productNames, "，"));
        }

        String cardTitle = "猫车® 今日" + categoryName + "有好价" + searchData.getTotal() + "件";

        String title = "今日" + categoryName + "有好价";

        content.setText(text);
        content.setCardTitle(cardTitle);
        content.setCardContent("每日更新当天低价优质品，放心买，便宜买。");
        content.setCardThumburl("https://mmbiz.qpic.cn/sz_mmbiz_png/y7ibJn5iaZcWBicu2ewoJaiazq2q7ot0szXMAw3JaQlBFH3QPk2oicR5SdlVNbwlkGbZ6ooatibEuOWgjQzSGWvTFusA/640?wx_fmt=png");
        content.setCardUrl("https://cat.zhizher.com/cat-sass-mobile/#/pages/sys/good_price/index?cidOne=" + mappingDO.getId() +"&title="+ EncodeUtils.encodeUrl(title));

        return content;
    }

    private void simpleSend(String header, String tail, List<Content> contents) {
        if (CollectionUtils.isEmpty(contents)) {
            return;
        }

        // 获取所有的群
        List<QwChatroomInfoDO> qwChatroomInfoDOS = qwChatroomInfoService.listByOwnerId("1688856684429282");

        try {
            for (QwChatroomInfoDO qwChatroomInfoDO : qwChatroomInfoDOS) {
//                if (!qwChatroomInfoDO.getId().equals("68")) {
//                    continue;
//                }
                String roomChatId = qwChatroomInfoDO.getRoomChatId();
                qwService.send(UNIQUE_ID, textMap(header, roomChatId));
                Thread.sleep(3500);

                for (Content content : contents) {

                    qwService.send(UNIQUE_ID, textMap(content.getText(), roomChatId));
                    Thread.sleep(5000);

                    // 发送卡片
                    qwService.send(UNIQUE_ID, cardMap(content, roomChatId));
                    Thread.sleep(3000);
                }

                qwService.send(UNIQUE_ID, textMap(tail, roomChatId));
            }


        } catch (Exception e) {

        }
    }

    @Data
    private static class Content implements Serializable {

        @Serial
        private static final long serialVersionUID = -4596032074436111834L;

        // 今日上午有好价猫砂158件，包含品牌：高爷家，渴望，网易严选，花麻麻...等，包含产品：身份牌，猫粮，冻干，罐头...等
        private String text;

        private String cardTitle;

        private String cardContent;

        private String cardThumburl;

        private String cardUrl;


    }

    private Map<String, Object> textMap(String content, String rid) {
        Map<String, Object> msg = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("conversation_id", rid);
        data.put("content", content);
        data.put("request_key", "AAAAAA");

        msg.put("data", data);
        msg.put("type", 5000);

        return msg;
    }

    private Map<String, Object> cardMap(Content content, String rid) {
        Map<String, Object> msg = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("conversation_id", rid);
        data.put("desc", content.getCardContent());
        data.put("request_key", "AAAAAA");
        data.put("thumburl", content.getCardThumburl());
        data.put("title", content.getCardTitle());
        data.put("url", content.getCardUrl());
        msg.put("data", data);
        msg.put("type", 5007);

        return msg;
    }

     private Map<String, Object> buildCardMap(Map<String, Object> data, String rid) {
        Map<String, Object> msg = new HashMap<>();

        msg.put("data", data);
        msg.put("type", 5007);

        return msg;
    }

    private static Map<String, Object> buildCardDataMap(String desc, String thumburl, String title, String url, String rid) {
        Map<String, Object> data = new HashMap<>();

        data.put("conversation_id", rid);
        data.put("desc", desc);
        data.put("request_key", "AAAAAA");
        data.put("thumburl", thumburl);
        data.put("title", title);
        data.put("url", url);

        return data;
    }

    private List<Long> getTotalNum() {

        List<Long> numResult = new ArrayList<>();
        // 在库商品数
        long passProductNum = 0;
        long saleProductNum = 0;
        long totalGoodProductNum = 0;
        long todayGoodProductNum = 0;

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
        condition.setHadRates(true);
        // 获取总数量
        searchData = cgUnionProductService.searchProduct(condition,
                null,
                0,
                0);

        if (searchData != null) {
            saleProductNum = searchData.getTotal();
        }

        long starTime = DateTimeUtils.earliestTimeToday(System.currentTimeMillis());
        CatUnionProductCondition goodCondition = new CatUnionProductCondition();

        Long nDay = 86400L * 3L * 1000L;
        // 全部有好价
        goodCondition.setAuditStatus(passStatus);
        goodCondition.setGteShopDsr(48000);
        // 5%
        goodCondition.setGteCommissionRate(500L);
        goodCondition.setGteUpdateTime(System.currentTimeMillis() - nDay);
        goodCondition.setGteCouponRemainCount(1L);
        goodCondition.setHadRates(true);

        goodCondition.setActivity(Collections.singletonList(CatActivityEnum.GOOD_PRICE.getActivity()));
        // 获取总数量
        searchData = cgUnionProductService.searchProduct(goodCondition,
                null,
                0,
                0);

        if (searchData != null) {
            totalGoodProductNum = searchData.getTotal();
        }

        // 今日有好价
        goodCondition.setGteShopDsr(null);
        goodCondition.setGteCreateTime(starTime);
        goodCondition.setGteCommissionRate(null);
        goodCondition.setGteCouponRemainCount(null);
        goodCondition.setHadRates(null);
        // 获取总数量
        searchData = cgUnionProductService.searchProduct(goodCondition,
                null,
                0,
                0);

        if (searchData != null) {
            todayGoodProductNum = searchData.getTotal();
        }

        numResult.add(passProductNum);
        numResult.add(saleProductNum);
        numResult.add(todayGoodProductNum);
        numResult.add(totalGoodProductNum);

        return numResult;
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

        long starTime = DateTimeUtils.earliestTimeToday(System.currentTimeMillis());

        System.out.println(starTime);

    }

    public void nineRcmd() {
        List<Long> nineNumMap = getNineNum();
        long totalNum = nineNumMap.get(0);
        long todayNum = nineNumMap.get(1);

        Map<Long, Integer> categoryNumMap = new HashMap<>();
        /**
         * 1,0,驱虫保健,1
         * 23,0,猫砂,1
         * 25,0,猫咪用品,1
         * 50,0,猫粮,1
         */
        categoryNumMap.put(1L, 2);
        categoryNumMap.put(25L, 2);
        categoryNumMap.put(23L, 4);
        categoryNumMap.put(50L, 4);

        CatUnionProductCondition condition = ProductSearchHelper.buildNineSearchCondition();
        List<String> sorts = new ArrayList<>(Collections.singletonList("shopDsr desc"));
        condition.setSorts(sorts);

        List<MaocheCategoryMappingDO> roots = maocheCategoryMappingService.getCategoryFromCache(0L);

        List<String> items = new ArrayList<>();
        for (MaocheCategoryMappingDO mappingDO : roots) {
            List<MaocheCategoryMappingDO> subCategories = maocheCategoryMappingService.getCategoryFromCache(mappingDO.getIid());
            if (CollectionUtils.isEmpty(subCategories)) {
                continue;
            }

            Integer size = categoryNumMap.get(mappingDO.getIid());
            List<String> categoryNames = subCategories.stream().map(MaocheCategoryMappingDO::getName).toList();
            condition.setCategoryNames(categoryNames);

            SearchSourceBuilder source = cgUnionProductService.searchSource(condition, null, cgUnionProductService::commonSort, null, 0, 20);
            ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(source);

            if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
                continue;
            }

            int i = 0;
            Collections.shuffle(searchData.getDocuments());
            for (CarAlimamaUnionProductIndex product : searchData.getDocuments()) {
                if (i >= size) {
                    break;
                }
                String name = product.getPropsBrand();
                if (StringUtils.isBlank(name) || name.equalsIgnoreCase("other")) {
                    continue;
                }
                if (StringUtils.isNotBlank(product.getPropsProductName())) {
                    name += product.getPropsProductName();
                }
                String priceTitle = "现价";
                if (product.getCoupon() != null && product.getCoupon() > 0 && product.getCouponRemainCount() != null && product.getCouponRemainCount() > 0) {
                    priceTitle = "券后";
                }
                name += priceTitle + PriceHelper.formatPrice(product.getPromotionPrice(), ".00", "") + "元";
                items.add(name);
                i++;
            }
        }
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        /**
         * 送上今日“9.9精选“”播报
         * 功能说明：
         * 推送获取到价格≤9.9元，店铺均分≥4.8的精选商品
         * 共计267件，今日新增：58件
         */
        StringBuilder header = new StringBuilder();
        header.append("送上今日“9.9精选“”播报\n");
        header.append("功能说明：\n");
        header.append("推送获取到价格≤9.9元，店铺均分≥4.8的精选商品\n");
        header.append("共计").append(totalNum).append("件，今日新增：").append(todayNum).append("件\n");

        StringBuilder content = new StringBuilder();
        content.append("包含以下商品：\n\n");
        content.append(StringUtils.join(items, "\n"));
        content.append("\n.... \n\n").append("更多今日低价欢迎点击以下专题页获取\n");

        String title = "猫车® 今日9.9精选" + totalNum + "件，新增" + todayNum + "件";
        Map<String, Object> cardMap = nineCardMap(title);

        String tail = "更多好物好价，欢迎群内@猫车选品官，带上关键词即可找券/答疑@猫车小助手\n";

        sendNine(header.toString(), tail, content.toString(), cardMap);
    }

    public static Map<String, Object> nineCardMap(String title) {
        Map<String, Object> cardMap = buildCardDataMap("精选店铺评分4.8分以上猫咪优质好物，帮您省心买",
                "https://mmbiz.qpic.cn/sz_mmbiz_png/y7ibJn5iaZcWBicu2ewoJaiazq2q7ot0szXMAw3JaQlBFH3QPk2oicR5SdlVNbwlkGbZ6ooatibEuOWgjQzSGWvTFusA/640?wx_fmt=png",
                title,
                "https://cat.zhizher.com/cat-sass-mobile/#/pages/sys/goods/index",
                null);

        return cardMap;
    }

    public List<Long> getNineNum() {

        CatUnionProductCondition baseCondition = ProductSearchHelper.buildNineSearchCondition();

        long totalNum = 0;
        long todayNum = 0;

        SearchSourceBuilder source = cgUnionProductService.searchSource(baseCondition, null, cgUnionProductService::commonSort, null, 0, 1);
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(source);

        if (searchData != null) {
            totalNum = searchData.getTotal();
        }

        // 今日新增(昨天14点开始)
        long starTime = DateTimeUtils.earliestTimeToday(System.currentTimeMillis()) - (10 * 3600 * 1000L);
        baseCondition.setGteSaleStatusTime(starTime);

        source = cgUnionProductService.searchSource(baseCondition, null, cgUnionProductService::commonSort, null, 0, 1);
        searchData = cgUnionProductService.search(source);

        if (searchData != null) {
            todayNum = searchData.getTotal();
        }

        // 清空
        baseCondition.setSaleStatus(null);

        List<Long> num = new ArrayList<>();
        num.add(totalNum);
        num.add(todayNum);

        return num;
    }

    private void sendNine(String header, String tail, String content, Map<String, Object> baseCardMap) {
        if (StringUtils.isBlank(content)) {
            return;
        }

        // 获取所有的群
        List<QwChatroomInfoDO> qwChatroomInfoDOS = qwChatroomInfoService.listByOwnerId("1688856684429282");

        try {
            for (QwChatroomInfoDO qwChatroomInfoDO : qwChatroomInfoDOS) {
//                if (!qwChatroomInfoDO.getId().equals("68")) {
//                    continue;
//                }
                String roomChatId = qwChatroomInfoDO.getRoomChatId();
                qwService.send(UNIQUE_ID, textMap(header, roomChatId));
                Thread.sleep(3500);

                qwService.send(UNIQUE_ID, textMap(content, roomChatId));
                Thread.sleep(3000);

                baseCardMap.put("conversation_id", roomChatId);
                // 发送卡片
                qwService.send(UNIQUE_ID, buildCardMap(baseCardMap, roomChatId));
                Thread.sleep(3000);

                qwService.send(UNIQUE_ID, textMap(tail, roomChatId));
                Thread.sleep(3000);
            }

        } catch (Exception e) {

        }
    }
}
