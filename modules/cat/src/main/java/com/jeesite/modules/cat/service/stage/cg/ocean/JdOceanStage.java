package com.jeesite.modules.cat.service.stage.cg.ocean;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.ProductValueHelper;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.inner.InnerApiService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponse;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.stage.cg.ocean.exception.QueryThirdApiException;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JdOceanStage extends AbstraOceanStage {

    @Resource
    private DingDanXiaApiService dingDanXiaApiService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductService;

    @Resource
    private InnerApiService innerApiService;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private OceanSearchService oceanSearchService;

    @Override
    public String getAffType() {
        return "jd";
    }

    @Override
    public Pattern getPattern() {
        return CommandService.jd;
    }

    @Override
    public void queryProductFromThirdApi(OceanContext context) {
        // 1. 查询淘宝api获取商品数据
        // 2. 保存商品数据
        // 3. 保存商品数据到消息中

        MaocheRobotCrawlerMessageDO crawlerMessage = context.getCrawlerMessage();
        String content = crawlerMessage.getMsg();

        boolean isSpecialUri = false;
        Map<String, String> urlMap = new HashMap<>();
        List<String> urls = new ArrayList<>();
        Map<String, JdUnionIdPromotion> jdUrlProductMap = new HashMap<>();
        Map<String, String> successUrlMap = new HashMap<>();
        String[] split = StringUtils.split(content, "\n");
        for (String item : split) {
            Matcher matcher = CommandService.jd.matcher(item);
            if (matcher.find()) {
                String group = matcher.group();
                urlMap.put(group, "");
                urls.add(group);
                if (isSpecialUri(group) && !isSpecialUri) {
                    isSpecialUri = true;
                }
            }
        }

        if (MapUtils.isEmpty(urlMap)) {
            Map<String, Object> remarks = new HashMap<>();
            remarks.put("api_error", "正则匹配链接未找到");
            context.setFailRemarks(JsonUtils.toJSONString(remarks));
            throw new QueryThirdApiException(QueryThirdApiException.QUERY_FAIL, "正则匹配链接未找到");
        }

        List<String> otherUrls = new ArrayList<>();
        List<JdUnionIdPromotion> promotions = new ArrayList<>();
        for (String url : urls) {
            Result<JdUnionIdPromotion> result = dingDanXiaApiService.jdByUnionidPromotion("FHPOsYO7zki7tcrxp0amyGMP7wxVkbU3", url, 1002248572L, 3100684498L);
            if (Result.isOK(result)) {
                JdUnionIdPromotion promotion = result.getResult();
                if (promotion.getSkuId() == null || promotion.getSkuId() <= 0) {
                    continue;
                }
                jdUrlProductMap.put(url, promotion);
                successUrlMap.put(url, promotion.getShortURL());
                promotions.add(promotion);
            } else {
                otherUrls.add(url);
            }
        }

        context.setJdOtherUrls(otherUrls);
        context.setJdUrlProductMap(jdUrlProductMap);
        context.setSuccessJdUrlMap(successUrlMap);

        // 如果不存在商品，并且只存在特殊uri
        if (CollectionUtils.isEmpty(promotions) && isSpecialUri) {
            context.setOnlySpecialUri(true);
            context.setJdProducts(promotions);
            return;
        }
        if (CollectionUtils.isEmpty(promotions)) {
            Map<String, Object> remarks = new HashMap<>();
            remarks.put("api_error", "订单侠jd接口信息查询未找到数据");
            context.setFailRemarks(JsonUtils.toJSONString(remarks));
            throw new QueryThirdApiException(QueryThirdApiException.QUERY_FAIL, "订单侠jd接口信息查询未找到数据");
        }

        context.setJdProducts(promotions);
    }

    @Override
    public void buildBaseMessageProducts(OceanContext context) {

        List<JdUnionIdPromotion> promotions = context.getJdProducts();
        if (context.isOnlySpecialUri()) {
            buildUriMessageProducts(context);
            return;
        }
        if (CollectionUtils.isEmpty(promotions)) {
            throw new IllegalArgumentException("jdProduct is null");
        }

        List<MaocheRobotCrawlerMessageProductDO> productDOs = new ArrayList<>();
        for (JdUnionIdPromotion promotion : promotions) {
            Long skuId = promotion.getSkuId();
            if (skuId == null || skuId <= 0) {
                continue;
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
            if (StringUtils.isBlank(imgUrl) && promotion.getVideoInfo() != null && promotion.getVideoInfo().get(0) != null) {
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
            productDO.setResourceId(String.valueOf(promotion.getSkuId()));
            productDO.setInnerId("0");

            promotion.setImageInfo(null);
            productDO.setApiContent(JsonUtils.toJSONString(promotion));

            productDO.setCategory("京东");
            productDO.setTitle(promotion.getSkuName());

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
            throw new IllegalArgumentException("jdProduct is null");
        }

        context.setMessageProducts(productDOs);

    }

    @Override
    public void saveMessageAndProduct(OceanContext context) {

        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        List<JdUnionIdPromotion> jdProducts = context.getJdProducts();
        List<MaocheRobotCrawlerMessageProductDO> messageProducts = context.getMessageProducts();
        if (messageSync == null || CollectionUtils.isEmpty(messageProducts)) {
            throw new IllegalArgumentException("messageSync or data or messageProducts is null");
        }
        List<String> resourceIds = null;
        if (CollectionUtils.isNotEmpty(jdProducts)) {
            resourceIds = jdProducts.stream().map(i -> String.valueOf(i.getSkuId())).distinct().toList();
        } else if (context.isOnlySpecialUri()) {
            resourceIds = Collections.singletonList(messageSync.getUniqueHash());
        } else {
            throw new IllegalArgumentException("messageSync or data or jdProducts is null");
        }

        // 获取商品额时间
        Date createDate = messageSync.getCreateDate();
        int newProduct = 0;
        // 获取3天前的开始时间
        long startTime = DateUtils.getOfDayFirst(DateUtils.addDays(createDate, -3)).getTime();
        // 获取今天开始时间
        long endTime = DateUtils.getOfDayFirst(createDate).getTime() - 1;
        // 判断3天前内是否存在
        OceanMessageCondition condition = new OceanMessageCondition();
        condition.setResourceIds(resourceIds);
        condition.setAffType(getAffType());
        condition.setGteCreateDate(startTime);
        condition.setLteCreateDate(endTime);
        ElasticSearchData<MaocheMessageSyncIndex, CatProductBucketTO> searchMsg = oceanSearchService.searchMsg(
                condition,
                null,
                null,
                null,
                0, 1);
        if (searchMsg != null && CollectionUtils.isEmpty(searchMsg.getDocuments())) {
            newProduct = 1;
        }

        messageSync.addRemarks("newProduct", newProduct);
        messageSync.addRemarks("jdOtherUrls", context.getJdOtherUrls());
        messageSync.addRemarks("successJdUrlMap", context.getSuccessJdUrlMap());

        messageSync.setProcessed(1L);
        messageSync.setResourceIds(StringUtils.join(resourceIds, ","));
        messageSync.setStatus("NORMAL");
        // 替换已经转链完成的链接
        if (MapUtils.isNotEmpty(context.getJdUrlProductMap())) {
            String msg = messageSync.getMsg();
            // 替换
            for (Map.Entry<String, JdUnionIdPromotion> url : context.getJdUrlProductMap().entrySet()) {
                msg = msg.replace(url.getKey(), url.getValue().getShortURL());
            }
            messageSync.setMsg(msg);
        }

        boolean res = maocheRobotCrawlerMessageSyncService.addIfAbsent(messageSync);
        if (!res) {
            log.error("messageSync is exist message:{}", JsonUtils.toJSONString(context.getCrawlerMessage()));
            throw new IllegalArgumentException("messageSync is exist message:" + JsonUtils.toJSONString(context.getCrawlerMessage()));
        }
        for (MaocheRobotCrawlerMessageProductDO productDO : messageProducts) {
            try {
                fillMessageInfo2Product(messageSync, productDO);
                maocheRobotCrawlerMessageProductService.save(productDO);
            } catch (Exception e) {
                log.error("save messageProduct error message:{}", JsonUtils.toJSONString(productDO), e);
            }
        }

    }

    @Override
    public void fillMessageInfo2Product(MaocheRobotCrawlerMessageSyncDO message, MaocheRobotCrawlerMessageProductDO productDO) {
        productDO.setRobotMsgId(message.getRobotMsgId());
        productDO.setMsgId(message.getUiid());
        productDO.setAffType(message.getAffType());
        productDO.setCreateDate(message.getCreateDate());
        productDO.setUpdateDate(message.getUpdateDate());
    }

    public void buildUriMessageProducts(OceanContext context) {
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
        context.setMessageProducts(productDOs);
    }

    private boolean isSpecialUri(String uri) {
        if (StringUtils.isBlank(uri)) {
            return false;
        }
        return uri.contains("y-03.cn") ||
                uri.contains("3.cn") ||
                uri.contains("jd.cn") ||
                uri.contains("t.cn") ||
                uri.contains("q5url.cn") ||
                uri.contains("kurl06.cn");
    }
}
