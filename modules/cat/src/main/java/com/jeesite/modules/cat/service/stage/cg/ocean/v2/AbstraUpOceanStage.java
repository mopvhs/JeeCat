package com.jeesite.modules.cat.service.stage.cg.ocean.v2;

import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.utils.UrlUtils;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageProductCondition;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.SimHashService;
import com.jeesite.modules.cat.service.cg.OceanSyncService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.cg.third.tb.dto.GeneralConvertResp;
import com.jeesite.modules.cat.service.es.OceanEsService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.jeesite.modules.cat.service.stage.cg.ocean.SimilarContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.SimilarDetail;
import com.jeesite.modules.cat.service.stage.cg.ocean.exception.QueryThirdApiException;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanContentHelper;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanMonitorHelper;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstraUpOceanStage implements OceanUpStage {

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

    @Resource
    private OceanEsService oceanEsService;

    @Resource
    private OceanSearchService oceanSearchService;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private CacheService cacheService;

//    @Resource
//    private OceanSyncService oceanSyncService;

//    @Resource
//    private SimHashService simHashService;

    @Override
    public void process(OceanUpContext context) {

        try {
            // 1. 构建基础的消息结构
            buildBaseMessageSync(context);

            // 2. 查询第三方接口获取商品数据
            queryProductFromThirdApi(context);

            // 3. 保存商品数据到消息中
            buildBaseMessageProducts(context);

            // 1.1 对干预处理完成的消息，计算一个simHash
//            calSimHash(context);
//            // 计算相似内容code
//            calSimilar(context);
//            // 判断是否为相似商品，兼容形式
//            checkSimilar(context);

            // 4. 保存商品数据
            saveMessageAndProduct(context);

            // 相似文案判断
//            similarMsgCheck(context);

            // 6. 构建索引
            indexEx(context);

            // 相似消息更新索引
//            similarMsgUpdate(context);

            // 统计
            statLog(context);

        } catch (QueryThirdApiException e) {
            String action = e.getAction();
            // 查询失败后，是否需要保存消息
            if (QueryThirdApiException.QUERY_FAIL.equals(action)) {

                // 判断是否达到失败频控次数，3次
                String key = "sync_fail_retry_" + context.getMessageSync().getId();
                String val = cacheService.get(key);
                long times = NumberUtils.toLong(val);
                if (times <= 3) {
                    // 重试
                    cacheService.incr(key);
                    cacheService.expire(key, (int) TimeUnit.DAYS.toSeconds(1));
                    // 重新调用下接口
                    // 调用接口
//                    oceanSyncService.retryOceanAnalysis(context.getMessageSync().getId());

                    MaocheRobotCrawlerMessageSyncDO syncDO = maocheRobotCrawlerMessageSyncDao.getById(NumberUtils.toLong(context.getMessageSync().getId()));
                    if (syncDO == null) {
                        return;
                    }
                    String affType = syncDO.getAffType();
                    OceanUpContext retryContext = new OceanUpContext(syncDO);
                    process(retryContext);
                    return;
                }

                updateFailQueryProduct(context);

                // 需要写索引
                List<Map<String, Object>> messageSyncIndex = OceanContentHelper.getMessageSyncIndex(Collections.singletonList(context.getMessageSync()), Collections.singletonList(context.getRobotMsg()));
                elasticSearch7Service.index(messageSyncIndex, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);

                return;
            }
            log.error("查询第三方接口获取商品数据失败 message :{}", JsonUtils.toJSONString(context.getMessageSync()), e);
        } catch (Exception e) {
            log.error("公海流程处理异常 message :{}", JsonUtils.toJSONString(context.getMessageSync()), e);
            updateFailQueryProduct(context);
            DingDingService.sendParseDingDingMsg("公海流程处理异常 message :{}, e:{}", JsonUtils.toJSONString(context.getMessageSync()), e.getMessage());
        }
    }


    public void statLog(OceanUpContext context) {
        try {
            if (context == null) {
                return;
            }

            int urlSize = 0;
            CommandContext commandContext = context.getCommandContext();
            if (commandContext == null) {
                log.error("statLog commandContext is null");
                return;
            }
            if (commandContext.listShortDetails() != null) {
                urlSize = commandContext.listShortDetails().size();
            }

            MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
            String affType = messageSync.getAffType();

            String affNumKey = OceanMonitorHelper.getOceanNumKey(affType);
            // 淘宝 & 京东单子次数
            cacheService.incr(affNumKey);
            cacheService.expire(affNumKey, (int) TimeUnit.DAYS.toSeconds(7));

            // 淘宝 & 京东链接次数
            String urlSizeKey = OceanMonitorHelper.getOceanUrlSizeKey(affType);
            cacheService.incrBy(urlSizeKey, (long) urlSize);
            cacheService.expire(urlSizeKey, (int) TimeUnit.DAYS.toSeconds(7));


            long tsTotal = 0;
            // 淘宝 & 京东 耗时区间
            if (CollectionUtils.isNotEmpty(commandContext.listShortDetails())) {
                for (ShortUrlDetail detail : commandContext.listShortDetails()) {
                    if (detail == null || detail.getTs() <= 0) {
                        continue;
                    }
                    long ts = detail.getTs();
                    String singleTsKey = OceanMonitorHelper.getOceanTsKey(affType, ts);

                    tsTotal += ts;

                    cacheService.incr(singleTsKey);
                    cacheService.expire(singleTsKey, (int) TimeUnit.DAYS.toSeconds(7));
                }
            }

            // 淘宝 & 京东 总耗时
            String tsTotalKey = OceanMonitorHelper.getOceanTsTotalKey(affType);
            cacheService.incrBy(tsTotalKey, tsTotal);
            cacheService.expire(tsTotalKey, (int) TimeUnit.DAYS.toSeconds(7));
        } catch (Exception e) {
            log.error("公海统计异常 ", e);
            DingDingService.sendDingDingMsg("公海统计异常" + e.getMessage());
        }
    }

    @Override
    public void buildBaseMessageSync(OceanUpContext context) {

        MaocheRobotCrawlerMessageSyncDO message = context.getMessageSync();
        //
        // 机器人原始内容 - 消息内容干预
        String msg = OceanContentHelper.interposeMsg(message.getMsg());
        message.setMsg(msg);

        // 写入到context中
        customBuildMessage(context);
    }

    @Override
    public void customBuildMessage(OceanUpContext context) {
        return;
    }

//    public void similarMsgUpdate(OceanUpContext context) {
//        List<MaocheMessageSyncIndex> similarMessages = context.getSimilarMessages();
//        if (CollectionUtils.isEmpty(similarMessages) || !context.isIndexResult()) {
//            return;
//        }
//        // 更新msg的状态，并且记录因为谁导致的相似
//        List<Long> ids = similarMessages.stream().map(MaocheMessageSyncIndex::getId).toList();
//        // 获取相似的消息
//        MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
//        query.setUiid_in(ids);
//        query.setStatus("NORMAL");
//        List<MaocheRobotCrawlerMessageSyncDO> similarMsgs = maocheRobotCrawlerMessageSyncService.findList(query);
//
//        // 更新相似消息的状态
//        // 10条一次
//        List<List<MaocheRobotCrawlerMessageSyncDO>> partition = Lists.partition(similarMsgs, 10);
//
//        Long similarIid = context.getCrawlerMessage().getIid();
//        List<Map<String, Object>> data = new ArrayList<>();
//        for (List<MaocheRobotCrawlerMessageSyncDO> p : partition) {
//            Map<String, Object> messageSyncIndex = new HashMap<>();
//
//            try {
//                for (MaocheRobotCrawlerMessageSyncDO item : p) {
//                    item.addRemarks("similar", similarIid);
//                    item.setStatus("SIMILAR");
//
//                    messageSyncIndex.put("id", item.getUiid());
//                    messageSyncIndex.put("status", "SIMILAR");
//                    data.add(messageSyncIndex);
//                }
//
//                // 批量更新
//                maocheRobotCrawlerMessageSyncService.updateBatch(p);
//            } catch (Exception e) {
//                log.error("更新相似消息状态失败", e);
//                try {
//                    maocheRobotCrawlerMessageSyncService.updateBatch(p);
//                } catch (Exception ee) {
//                    log.error("第二次更新相似消息状态失败", ee);
//                }
//            }
//        }
//
//        elasticSearch7Service.update(data, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);
//
//    }

    public void updateFailQueryProduct(OceanUpContext context) {
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        if (messageSync == null) {
            log.error("保存失败的查询商品数据失败, messageSync is null");
            return;
        }
        messageSync.setStatus(OceanStatusEnum.FAIL.name());
        // remarks在流程各个流程上设置
//        messageSync.setRemarks(context.getFailRemarks());
        maocheRobotCrawlerMessageSyncService.updateById(messageSync);
    }

    public void updateExceptionQueryProduct(OceanUpContext context) {
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        if (messageSync == null) {
            log.error("保存失败的查询商品数据失败, messageSync is null");
            return;
        }
        messageSync.setStatus(OceanStatusEnum.EXCEPTION.name());
        // remarks在流程各个流程上设置
//        messageSync.setRemarks(context.getFailRemarks());
        maocheRobotCrawlerMessageSyncService.updateById(messageSync);
    }

    @Override
    public void indexEx(OceanUpContext context) {
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        if (messageSync == null || messageSync.getUiid() == null || messageSync.getUiid() == 0L) {
            log.error("索引构建失败, messageSync is null");
            return;
        }

        oceanEsService.indexEs(Collections.singletonList(messageSync.getUiid()), 10);

        // 默认执行的话就认为是成功
        context.setIndexResult(true);
    }


    @Override
    public void similarMsgCheck(OceanUpContext context) {
//        if (context.isOnlySpecialUri()) {
//            return;
//        }
        // 获取文案的md5
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        String uniqueHash = messageSync.getUniqueHash();
        if (context.getSimilar() != null) {
            // 新逻辑
            return;
        }

        // 判断3天前内是否存在
        OceanMessageCondition condition = new OceanMessageCondition();
        condition.setUniqueHash(uniqueHash);
        condition.setAffType(getAffType());
        // 查询存在一样的uniqueHash的数据，通过es查询
        ElasticSearchData<MaocheMessageSyncIndex, CatProductBucketTO> searchData = oceanSearchService.searchMsg(condition, null, null, null, 0, 1000);
        // 异常的时候，再查询一次
        if (searchData == null) {
            searchData = oceanSearchService.searchMsg(condition, null, null, null, 0, 1000);
        }
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            // 为空的时候 做一次db的查询，es刷磁盘需要时间，短时间内可能会查询不出来
//            MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
//            query.setUniqueHash(uniqueHash);
//            query.setStatus("NORMAL");
//            List<MaocheRobotCrawlerMessageSyncDO> similarMsgs = maocheRobotCrawlerMessageSyncService.findList(query);
            return;
        }
        List<MaocheMessageSyncIndex> documents = searchData.getDocuments();

        List<String> resourceIds = Arrays.asList(StringUtils.split(messageSync.getResourceIds(), ","));

        List<MaocheMessageSyncIndex> similarMessages = new ArrayList<>();
        // 需要二次对比的数据
        List<MaocheMessageSyncIndex> secondChecks = new ArrayList<>();

        // 先对比资源id是否一样，数量，以及集合的差集是否为0
        for (MaocheMessageSyncIndex doc : documents) {
            List<String> tempIds = new ArrayList<>(resourceIds);
            List<String> itemResourceIds = Optional.ofNullable(doc.getResourceIds()).orElse(new ArrayList<>());

            if (tempIds.size() != itemResourceIds.size()) {
                continue;
            }
            // 差集为0，说明资源id一样
            tempIds.removeAll(itemResourceIds);

            if (CollectionUtils.isEmpty(tempIds)) {
                // 说明资源id一样，直接返回
                similarMessages.add(doc);
                continue;
            }
            secondChecks.add(doc);
        }

        if (CollectionUtils.isNotEmpty(secondChecks)) {
            // 通过secondChecks获取msgId的map
            Map<Long, MaocheMessageSyncIndex> messageSyncIndexMap = secondChecks.stream().collect(Collectors.toMap(MaocheMessageSyncIndex::getId, Function.identity(), (k1, k2) -> k1));

            List<Long> msgIds = secondChecks.stream().map(MaocheMessageSyncIndex::getId).distinct().toList();
            OceanMessageProductCondition productSearch = new OceanMessageProductCondition();
            productSearch.setMsgIds(msgIds);

            ElasticSearchData<MaocheMessageProductIndex, CatProductBucketTO> productSearchData = oceanSearchService.searchProduct(productSearch, null, null, 0, 1000);
            // 获取失败的话，再获取一次
            if (productSearchData == null) {
                productSearchData = oceanSearchService.searchProduct(productSearch, null, null, 0, 1000);
            }
            if (productSearchData == null || CollectionUtils.isEmpty(productSearchData.getDocuments())) {
                // 多次失败的话，就认为是一样的
                similarMessages.addAll(secondChecks);
                return;
            }

            List<String> sellerIds = context.getMessageProducts().stream().map(MaocheRobotCrawlerMessageProductDO::getSellerId).distinct().toList();

            List<MaocheMessageProductIndex> productIndices = productSearchData.getDocuments();
            // 按照msgId分组
            Map<Long, List<MaocheMessageProductIndex>> msgIdMap = productIndices.stream().collect(Collectors.groupingBy(MaocheMessageProductIndex::getMsgId));
            // 对比seller是否一样
            for (Map.Entry<Long, List<MaocheMessageProductIndex>> entry : msgIdMap.entrySet()) {
                MaocheMessageSyncIndex messageSyncIndex = messageSyncIndexMap.get(entry.getKey());
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    similarMessages.add(messageSyncIndex);
                    continue;
                }
                List<String> productSellerIds = entry.getValue().stream().map(MaocheMessageProductIndex::getSellerId).distinct().toList();
                List<String> temp = new ArrayList<>(sellerIds);
                temp.removeAll(productSellerIds);
                if (CollectionUtils.isEmpty(temp)) {
                    similarMessages.add(messageSyncIndex);
                }
            }
        }

        context.setSimilarMessages(similarMessages);
    }

    public static MatchContent calMatchContent(Pattern pattern, String content) {
        StringBuilder calContent = new StringBuilder();
        String[] split = content.split("\n");
        String regex = "[\\w\\d*.()/]+";

        List<Matcher> matchers = new ArrayList<>();

        List<String> contents = new ArrayList<>();
        for (String item : split) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                matchers.add(matcher);
                // 去除所有的数字和符号还有英文
                calContent.append(item.replaceAll(regex, ""));
            } else {
                contents.add(item);
                calContent.append(item);
            }
        }

        MatchContent matchContent = new MatchContent();

        matchContent.setCalContent(calContent.toString());
        matchContent.setContents(contents);
        matchContent.setMatchers(matchers);
        matchContent.setCalMd5(Md5Utils.md5(calContent.toString()));
        return matchContent;
    }

    @Data
    public static class MatchContent {

        private List<Matcher> matchers;

        private String calContent;

        private List<String> contents;

        private String calMd5;
    }



    public static String fixAffType(String msg, String affType) {
        if (StringUtils.isBlank(msg)) {
            return affType;
        }
//        boolean contains = msg.contains("y.q5url.cn") || msg.contains("y-03.cn");
        boolean contains = msg.contains("y.q5url.cn");

        return contains ? "tb" : affType;
    }

    @Data
    @AllArgsConstructor
    public static class TextBO {

        private String text;

        private Integer size;
    }

    @Override
    public void buildBaseMessageProducts(OceanUpContext context) {

        List<MaocheRobotCrawlerMessageProductDO> messageProducts = new ArrayList<>();
        // 获取淘宝的
        List<MaocheRobotCrawlerMessageProductDO> tbProducts = buildTbProducts(context);
        // 获取京东的
        List<MaocheRobotCrawlerMessageProductDO> jdProducts = buildJdProducts(context);

        if (CollectionUtils.isNotEmpty(tbProducts)) {
            messageProducts.addAll(tbProducts);
        }

        if (CollectionUtils.isNotEmpty(jdProducts)) {
            messageProducts.addAll(jdProducts);
        }

        context.setMessageProducts(messageProducts);
    }

    public List<MaocheRobotCrawlerMessageProductDO> buildTbProducts(OceanUpContext context) {

        Map<String, GeneralConvertResp> productMap = context.getTbProductMap();
        if (MapUtils.isEmpty(productMap)) {
            return null;
        }

        List<MaocheRobotCrawlerMessageProductDO> messageProducts = new ArrayList<>();
        for (Map.Entry<String, GeneralConvertResp> entry : productMap.entrySet()) {
            GeneralConvertResp tbProduct = entry.getValue();
            GeneralConvertResp.ItemBasicInfo itemBasicInfo = tbProduct.getItemBasicInfo();
            GeneralConvertResp.PricePromotionInfo pricePromotionInfo = tbProduct.getPricePromotionInfo();

//            String itemId = tbProduct.getItemId();
//            if (StringUtils.isBlank(itemId)) {
//                if (itemBasicInfo == null || StringUtils.isBlank(itemBasicInfo.getItemUrl())) {
//                    continue;
//                }
//                String itemUrl = itemBasicInfo.getItemUrl();
//                // https://uland.taobao.com/item/edetail?id=g4kQxqPU3t20YWJwR5iYMOibUr-QA0RODib7nd058YtB
//                Map<String, String> parameters = UrlUtils.getParameters(itemUrl);
//                if (MapUtils.isNotEmpty(parameters)) {
//                    itemId = parameters.get("id");
//                }
//            }
            String itemId = GeneralConvertResp.analyzingItemId(tbProduct);
            if (StringUtils.isBlank(itemId)) {
                continue;
            }

            // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
            String[] idArr = StringUtils.split(itemId, "-");
            String itemIdSuffix = idArr[1];

            MaocheRobotCrawlerMessageProductDO productDO = new MaocheRobotCrawlerMessageProductDO();
            productDO.setResourceId(itemIdSuffix);
            productDO.setInnerId("0");
            productDO.setItemId(tbProduct.getItemId());
            productDO.setApiContent(JsonUtils.toJSONString(tbProduct));
            productDO.setCategory(itemBasicInfo.getCategoryName());
            productDO.setTitle(itemBasicInfo.getTitle());
            productDO.setShortTitle(itemBasicInfo.getShortTitle());
            // detail = 2之后，字段被移除了
            productDO.setShopDsr("0");
            productDO.setShopName(itemBasicInfo.getShopTitle());
            productDO.setSellerId(itemBasicInfo.getSellerId());
            productDO.setPictUrl(itemBasicInfo.getPictUrl());
            productDO.setCommissionRate(new BigDecimal(tbProduct.getCommissionRate()).multiply(new BigDecimal(100)).longValue());
            productDO.setPrice(new BigDecimal(pricePromotionInfo.getZkFinalPrice()).multiply(new BigDecimal(100)).longValue());
            productDO.setVolume(NumberUtils.toLong(itemBasicInfo.getVolume()));
            productDO.setStatus("NORMAL");
            productDO.setCreateBy("admin");
            productDO.setUpdateBy("admin");
            productDO.setRemarks("{}");

            messageProducts.add(productDO);
        }

        return messageProducts;
    }

    public List<MaocheRobotCrawlerMessageProductDO> buildJdProducts(OceanUpContext context) {
        List<JdUnionIdPromotion> promotions = context.getJdProducts();
        if (context.isOnlySpecialUri()) {
            return buildSpecialUriProducts(context);
        }

        if (CollectionUtils.isEmpty(promotions)) {
            return null;
        }

        List<MaocheRobotCrawlerMessageProductDO> productDOs = new ArrayList<>();
        for (JdUnionIdPromotion promotion : promotions) {
            String skuId = promotion.getSkuId();
            boolean isCoupon = false;
            if ((StringUtils.isBlank(skuId)) && StringUtils.isNotBlank(promotion.getShortURL())) {
                // 优惠券
                isCoupon = true;
                skuId = "999999999999";
            }

            MaocheRobotCrawlerMessageProductDO productDO = new MaocheRobotCrawlerMessageProductDO();

            long reservePrice = 0L;
            long originalPrice = 0L;
            if (promotion.getPriceInfo() != null) {
                originalPrice = BigDecimal.valueOf(promotion.getPriceInfo().getPrice()).multiply(new BigDecimal(100)).longValue();
                reservePrice = BigDecimal.valueOf(promotion.getPriceInfo().getLowestPrice()).multiply(new BigDecimal(100)).longValue();
            }

            long commissionRate = 0L;
            long commission = 0L;
            if (promotion.getCommissionInfo() != null) {
                commissionRate = BigDecimal.valueOf(promotion.getCommissionInfo().getCommissionShare()).multiply(new BigDecimal(100)).longValue();
                commission = BigDecimal.valueOf(promotion.getCommissionInfo().getCommission()).multiply(new BigDecimal(100)).longValue();
            }

            String imgUrl = "";
            if (promotion.getImageInfo() != null && CollectionUtils.isNotEmpty(promotion.getImageInfo().getImageList())) {
                imgUrl = promotion.getImageInfo().getImageList().get(0).getUrl();
            }
            // 获取不到的话 取视频的封面图
            if (StringUtils.isBlank(imgUrl) && CollectionUtils.isNotEmpty(promotion.getVideoInfo()) && promotion.getVideoInfo().get(0) != null) {
                JdUnionIdPromotion.VideoInfo videoInfo = promotion.getVideoInfo().get(0);
                List<JdUnionIdPromotion.Video> videoList = videoInfo.getVideoList();
                if (CollectionUtils.isNotEmpty(videoList)) {
                    JdUnionIdPromotion.Video video = videoList.get(0);
                    imgUrl = video.getImageUrl();
                }
            }

            String sellerId = "";
            String shopTitle = "";
            if (promotion.getShopInfo() != null) {
                shopTitle = promotion.getShopInfo().getShopName();
                sellerId = String.valueOf(promotion.getShopInfo().getShopId());
            }

            // 商品标题
            productDO.setItemUrl(promotion.getShortURL());
//            productDO.setRobotMsgId(message.getRobotMsgId());
//            productDO.setMsgId(message.getUiid());


            productDO.setAffType(getAffType());
            productDO.setResourceId(String.valueOf(skuId));
            productDO.setInnerId("0");

            promotion.setImageInfo(null);
            productDO.setApiContent(JsonUtils.toJSONString(promotion));

            String skuName = promotion.getSkuName();
            if (isCoupon) {
                skuName = "优惠券";
            }
            productDO.setCategory("京东");
            productDO.setTitle(skuName);

            productDO.setShortTitle("");
            productDO.setShopDsr("0");
            productDO.setCommissionRate(commissionRate);
            productDO.setShopName(shopTitle);
            productDO.setSellerId(sellerId);
            productDO.setPrice(reservePrice);
            productDO.setPictUrl(imgUrl);
            productDO.setVolume(0L);
            productDO.setStatus("NORMAL");
            productDO.setCreateBy("admin");
            productDO.setUpdateBy("admin");
//            productDO.setCreateDate(message.getCreateDate());
//            productDO.setUpdateDate(message.getUpdateDate());
            productDO.setRemarks("{}");

            productDOs.add(productDO);
        }

        if (CollectionUtils.isEmpty(productDOs)) {
            return null;
        }

        return productDOs;
    }

    public List<MaocheRobotCrawlerMessageProductDO> buildSpecialUriProducts(OceanUpContext context) {
        List<MaocheRobotCrawlerMessageProductDO> productDOs = new ArrayList<>();
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        String resourceId = messageSync.getUniqueHash();

        MaocheRobotCrawlerMessageProductDO productDO = new MaocheRobotCrawlerMessageProductDO();

        long reservePrice = 0L;
        long commissionRate = 0L;
        String imgUrl = "https://cat.zhizher.com/assets/userfiles/fileupload/202404/1784101138316550144.png";
        // 获取不到的话 取视频的封面图
        String sellerId = "";
        String shopTitle = "";

        // 商品标题
        productDO.setAffType(getAffType());
        productDO.setResourceId(resourceId);
        productDO.setInnerId("0");

        productDO.setCategory("京东");
        productDO.setTitle("外部链接");

        productDO.setShortTitle("");
        productDO.setShopDsr("0");
        productDO.setCommissionRate(commissionRate);
        productDO.setShopName(shopTitle);
        productDO.setSellerId(sellerId);
        productDO.setApiContent("");
        productDO.setPrice(reservePrice);
        productDO.setPictUrl(imgUrl);
        productDO.setVolume(0L);
        productDO.setStatus("NORMAL");
        productDO.setCreateBy("admin");
        productDO.setUpdateBy("admin");
        productDO.setRemarks("{}");

        productDOs.add(productDO);


        return productDOs;
    }

    @Override
    public void calSimilar(OceanUpContext context) {
        // 获取转链详情
        CommandContext command = context.getCommandContext();
        if (CollectionUtils.isEmpty(command.listShortDetails())) {
            return;
        }
        SimilarContext similar = new SimilarContext();
        List<SimilarDetail> products = new ArrayList<>();
        // 淘宝为空
        List<String> failUrls = new ArrayList<>();
        List<ShortUrlDetail> shortUrlDetails = command.listShortDetails();
        for (ShortUrlDetail url : shortUrlDetails) {
            String checkUrl = Optional.ofNullable(url.getReplaceUrl()).orElse(url.getContentUrl());
            if (BooleanUtils.isNotTrue(url.getApiRes())) {
                failUrls.add(checkUrl);
                continue;
            }
            // 京东商品
            JdUnionIdPromotion promotion = url.getPromotion();
            GeneralConvertResp tbProduct = url.getTbProduct();
            if (promotion != null) {
                SimilarDetail detail = SimilarDetail.convertProduct(promotion);
                if (detail == null) {
                    continue;
                }
                products.add(detail);
            }
            if (tbProduct != null) {
                SimilarDetail detail = SimilarDetail.convertProduct(tbProduct);
                if (detail == null) {
                    continue;
                }
                products.add(detail);
            }
        }

        similar.setNum(shortUrlDetails.size());
        similar.setProducts(products);
        similar.setFailUrls(failUrls);

        context.setSimilar(similar);
    }

    public void calSimHash(OceanUpContext context) {
        if (context == null || context.getMessageSync() == null || StringUtils.isBlank(context.getMessageSync().getMsg())) {
            return;
        }
        SimilarContext similar = new SimilarContext();

        String msg = context.getMessageSync().getMsg();
        String simHash = doCalSimHash(msg);
        similar.setSimHash(simHash);

        context.getMessageSync().setUniqueHash(simHash);
        context.setSimilar(similar);
    }

    public static String doCalSimHash(String msg) {
        String[] split = msg.split("\n");
        StringBuilder str = new StringBuilder();
        // 判断是否带口令或者是链接，忽略
        for (String line : split) {
            Matcher matcher = CommandService.jd.matcher(line);
            if (matcher.find()) {
                continue;
            }
            matcher = CommandService.tb.matcher(line);
            if (matcher.find()) {
                continue;
            }
            str.append(line);
        }
        String simHash = SimHashService.get(str.toString());
        if (StringUtils.isBlank(simHash)) {
            simHash = Md5Utils.md5(str.toString());
        }
        return simHash;
    }

    public static void main(String[] args) {
        String msg = "✨有好价✨\n" +
                "@\uD83C\uDF5F 朗诺宠物鸡肉冻干300g\n" +
                "+赠猫粮试吃8g*4\n" +
                "\uD83D\uDCB0105，88vip\uD83D\uDCB099.7\n" +
                "(2wr1VXY4Pur)/ CA21,)/ AC01\n" +
                "---------------------\n" +
                "自助查车 dwz.cn/qveM26UV";

        String c = OceanContentHelper.interposeMsg(msg);

        System.out.println(c);
    }

    public void checkSimilar(OceanUpContext context) {
        // 获取转链详情
        SimilarContext similar = context.getSimilar();
        if (similar == null) {
            return;
        }

        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        String simHash = similar.getSimHash();

        // 判断3天前内是否存在
        long daysAgo = DateTimeUtils.nDaysAgo(3);

        OceanMessageCondition condition = new OceanMessageCondition();
        condition.setUniqueHash(simHash);
        condition.setAffType(getAffType());
        condition.setGteCreateDate(daysAgo);
        // 查询存在一样的uniqueHash的数据，通过es查询
        ElasticSearchData<MaocheMessageSyncIndex, CatProductBucketTO> searchData = oceanSearchService.searchMsg(condition, null, null, null, 0, 10);
        // 异常的时候，再查询一次
        if (searchData == null) {
            searchData = oceanSearchService.searchMsg(condition, null, null, null, 0, 10);
        }
        List<Long> similarIds = new ArrayList<>();
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            // 为空的时候 做一次db的查询，es刷磁盘需要时间，短时间内可能会查询不出来
//            MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
//            query.setUniqueHash(simHash);
//            query.setStatus("NORMAL");
//            List<MaocheRobotCrawlerMessageSyncDO> similarMsgs = maocheRobotCrawlerMessageSyncService.findList(query);
            List<MaocheRobotCrawlerMessageSyncDO> similarMsgs = maocheRobotCrawlerMessageSyncDao.findSimHashMessages(simHash, OceanStatusEnum.FAIL.name(), DateTimeUtils.getStringDate(new Date(daysAgo)));
            if (CollectionUtils.isNotEmpty(similarMsgs)) {
                similarIds = similarMsgs.stream().map(MaocheRobotCrawlerMessageSyncDO::getUiid).toList();
            }
        } else {
            List<MaocheMessageSyncIndex> documents = searchData.getDocuments();
            similarIds = documents.stream().map(MaocheMessageSyncIndex::getId).collect(Collectors.toList());
        }
        // 移除自身
        if (CollectionUtils.isNotEmpty(similarIds)) {
            similarIds.remove(messageSync.getUiid());
        }

        // 修改状态为相似内容
        if (CollectionUtils.isNotEmpty(similarIds)) {
            messageSync.setStatus(OceanStatusEnum.SIMILAR.name());
            messageSync.setUniqueHash(simHash);
        }

    }
}
