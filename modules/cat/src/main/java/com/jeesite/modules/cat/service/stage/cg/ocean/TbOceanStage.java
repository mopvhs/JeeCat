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
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponse;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.stage.cg.ocean.exception.QueryThirdApiException;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TbOceanStage extends AbstraOceanStage {

    @Resource
    private TbApiService tbApiService;

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
        return "tb";
    }

    @Override
    public Pattern getPattern() {
        return CommandService.tb;
    }

    @Override
    public void queryProductFromThirdApi(OceanContext context) {
        // 1. 查询淘宝api获取商品数据
        // 2. 保存商品数据
        // 3. 保存商品数据到消息中

        MaocheRobotCrawlerMessageDO crawlerMessage = context.getCrawlerMessage();
        String content = crawlerMessage.getMsg();

        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("detail", 2);
        objectMap.put("deepcoupon", 1);
        objectMap.put("couponId", 1);
        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Result<CommandResponseV2> response = tbApiService.getCommonCommand(content, objectMap);

        if (!Result.isOK(response)) {
            Map<String, Object> remarks = new HashMap<>();
            remarks.put("api_error", response);
            context.setFailRemarks(JsonUtils.toJSONString(remarks));

            throw new QueryThirdApiException(QueryThirdApiException.QUERY_FAIL, "查询淘宝api失败");
        }

        CommandResponseV2 commandResponse = response.getResult();
        context.setTbProduct(commandResponse);
    }

    @Override
    public void buildBaseMessageProducts(OceanContext context) {

        CommandResponseV2 tbProduct = context.getTbProduct();
        if (tbProduct == null) {
            throw new IllegalArgumentException("tbProduct is null");
        }

        CommandResponseV2.ItemBasicInfo itemBasicInfo = tbProduct.getItemBasicInfo();
        CommandResponseV2.PricePromotionInfo pricePromotionInfo = tbProduct.getPricePromotionInfo();

        // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
        String[] idArr = StringUtils.split(tbProduct.getNumIid(), "-");
        String itemIdSuffix = idArr[1];

        MaocheRobotCrawlerMessageProductDO productDO = new MaocheRobotCrawlerMessageProductDO();
        productDO.setResourceId(itemIdSuffix);
        productDO.setInnerId("0");
        productDO.setItemId(tbProduct.getNumIid());
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

        context.setMessageProducts(Collections.singletonList(productDO));

    }

    @Override
    public void saveMessageAndProduct(OceanContext context) {

        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        CommandResponseV2 data = context.getTbProduct();
        List<MaocheRobotCrawlerMessageProductDO> messageProducts = context.getMessageProducts();

        if (messageSync == null || data == null || CollectionUtils.isEmpty(messageProducts)) {
            throw new IllegalArgumentException("messageSync or data or messageProducts is null");
        }

        String numIid = data.getNumIid();
        // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
        String[] idArr = StringUtils.split(numIid, "-");
        String itemIdSuffix = idArr[1];
        Long price = 0L;
        Long uiid = 0L;

        MaocheAlimamaUnionProductDO unionProductDO = maocheAlimamaUnionProductService.getProduct(itemIdSuffix, "NORMAL");
        // 在库商品，获取在库商品数据
        if (unionProductDO != null) {
            // 券后价
            price = ProductValueHelper.calVeApiPromotionPrice(JSONObject.parseObject(unionProductDO.getOrigContent()));
            uiid = unionProductDO.getUiid();
        }
        CommandResponseV2.ItemBasicInfo itemBasicInfo = data.getItemBasicInfo();
        String status = "NORMAL";
        // todo yhq detail = 2后 字段移除，先使用销量替换
//        long shopDsr = NumberUtils.toLong(data.getShopDsr());
        long volume = NumberUtils.toLong(itemBasicInfo.getVolume());
        // 不存在并且shopdsr >= 4.8
        // volume >= 100
        if (uiid == 0 && volume >= 100) {
            Result<String> result = innerApiService.syncTbProduct(numIid);
            if (Result.isOK(result)) {
                uiid = NumberUtils.toLong(result.getResult());
            } else {
                // todo 同步接口判断已经存在的话，需要覆盖numiid为库里面的
                String message = result.getMessage();
                messageSync.addRemarks("auto_storage_error", message);
            }
        }
        long processed = 0;
        String resourceId = itemIdSuffix;
        if (uiid > 0 && unionProductDO == null) {
            MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
            query.setId(String.valueOf(uiid));
            unionProductDO = maocheAlimamaUnionProductService.get(query);
        }

        if (unionProductDO != null && StringUtils.isNotBlank(unionProductDO.getIid())) {
            resourceId = unionProductDO.getIid();
            processed = 1;
        }

//        if (shopDsr < 48000) {
//            status = "LOW_SHOP_DSR";
//        }
//        if (volume < 10) {
//            status = "LOW_SHOP_DSR";
//        }

        // 获取商品额时间
        Date createDate = messageSync.getCreateDate();
        int newProduct = 0;
        // 获取3天前的开始时间
        long startTime = DateUtils.getOfDayFirst(DateUtils.addDays(createDate, -3)).getTime();
        // 获取今天开始时间
        long endTime = DateUtils.getOfDayFirst(createDate).getTime() - 1;
        // 判断3天前内是否存在
        OceanMessageCondition condition = new OceanMessageCondition();
        condition.setResourceIds(Collections.singletonList(resourceId));
        condition.setAffType("tb");
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

        messageSync.setProcessed(processed);
        messageSync.setResourceIds(resourceId);
        messageSync.setStatus(status);
        boolean res = maocheRobotCrawlerMessageSyncService.addIfAbsent(messageSync);
        if (!res) {
            log.error("messageSync is exist message:{}", JsonUtils.toJSONString(context.getCrawlerMessage()));
            throw new IllegalArgumentException("messageSync is exist message:" + JsonUtils.toJSONString(context.getCrawlerMessage()));
        }

        MaocheRobotCrawlerMessageProductDO productDO = messageProducts.get(0);

        if (price > 0) {
            productDO.setPrice(price);
        }
        if (uiid > 0) {
            productDO.setInnerId(String.valueOf(uiid));
        }

        fillMessageInfo2Product(messageSync, productDO);

        maocheRobotCrawlerMessageProductService.save(productDO);
    }

    @Override
    public void fillMessageInfo2Product(MaocheRobotCrawlerMessageSyncDO message, MaocheRobotCrawlerMessageProductDO productDO) {
        productDO.setRobotMsgId(message.getRobotMsgId());
        productDO.setMsgId(message.getUiid());
        productDO.setAffType(message.getAffType());
        productDO.setCreateDate(message.getCreateDate());
        productDO.setUpdateDate(message.getUpdateDate());
    }
}
