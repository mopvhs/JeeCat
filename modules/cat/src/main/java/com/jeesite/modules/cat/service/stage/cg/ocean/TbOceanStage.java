package com.jeesite.modules.cat.service.stage.cg.ocean;

import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.model.ProductPriceTO;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.inner.InnerApiService;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponse;
import com.jeesite.modules.cat.service.stage.cg.ocean.exception.QueryThirdApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public String getAffType() {
        return "tb";
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
        objectMap.put("detail", 1);
        objectMap.put("deepcoupon", 1);
        objectMap.put("couponId", 1);
        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Result<CommandResponse> response = tbApiService.getCommonCommand(content, objectMap);

        if (!Result.isOK(response)) {
            Map<String, Object> remarks = new HashMap<>();
            remarks.put("api_error", response);
            context.setFailRemarks(JsonUtils.toJSONString(remarks));

            throw new QueryThirdApiException(QueryThirdApiException.QUERY_FAIL, "查询淘宝api失败");
        }

        CommandResponse commandResponse = response.getResult();
        context.setTbProduct(commandResponse);
    }

    @Override
    public void buildBaseMessageProducts(OceanContext context) {

        CommandResponse tbProduct = context.getTbProduct();
        if (tbProduct == null) {
            throw new IllegalArgumentException("tbProduct is null");
        }

        // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
        String[] idArr = StringUtils.split(tbProduct.getNumIid(), "-");
        String itemIdSuffix = idArr[1];

        MaocheRobotCrawlerMessageProductDO productDO = new MaocheRobotCrawlerMessageProductDO();
        productDO.setResourceId(itemIdSuffix);
        productDO.setInnerId("0");
        productDO.setApiContent(JsonUtils.toJSONString(tbProduct));
        productDO.setCategory(tbProduct.getCatLeafName());
        productDO.setTitle(tbProduct.getTitle());
        productDO.setShortTitle(tbProduct.getShortTitle());
        productDO.setShopDsr(tbProduct.getShopDsr());
        productDO.setShopName(tbProduct.getShopTitle());
        productDO.setSellerId(tbProduct.getSellerId());
        productDO.setPictUrl(tbProduct.getPictUrl());
        productDO.setCommissionRate(new BigDecimal(tbProduct.getCommissionRate()).multiply(new BigDecimal(100)).longValue());
        productDO.setPrice(new BigDecimal(tbProduct.getZkFinalPrice()).multiply(new BigDecimal(100)).longValue());
        productDO.setVolume(NumberUtils.toLong(tbProduct.getVolume()));
        productDO.setStatus("NORMAL");
        productDO.setCreateBy("admin");
        productDO.setUpdateBy("admin");
        productDO.setRemarks("");

        context.setMessageProducts(Collections.singletonList(productDO));

    }

    @Override
    public void saveMessageAndProduct(OceanContext context) {

        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        CommandResponse data = context.getTbProduct();
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
        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setItemIdSuffix(itemIdSuffix);
        // 在库商品，获取在库商品数据
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition, null, 0, 1);
        if (searchData != null) {
            List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
            if (CollectionUtils.isNotEmpty(productTOs)) {
                UnionProductTO productTO = productTOs.get(0);
                ProductPriceTO displayPrice = productTO.getDisplayPrice();
                price = displayPrice.getPrice();
                uiid = productTO.getId();
            }
        }

        long shopDsr = NumberUtils.toLong(data.getShopDsr());
        // 不存在并且shopdsr >= 4.8
        if (uiid == 0 && shopDsr >= 48000) {
            Result<String> result = innerApiService.syncTbProduct(numIid);
            if (Result.isOK(result)) {
                uiid = NumberUtils.toLong(result.getResult());
            } else {
                // todo 同步接口判断已经存在的话，需要覆盖numiid为库里面的

                String message = result.getMessage();
                Map<String, Object> remarks = new HashMap<>();
                remarks.put("auto_storage_error", message);
                messageSync.addRemarks(remarks);
            }
        }

        messageSync.setProcessed(1L);
        messageSync.setResourceIds(itemIdSuffix);
        messageSync.setStatus("NORMAL");
        boolean res = maocheRobotCrawlerMessageSyncService.addIfAbsent(messageSync);
        if (!res) {
            log.error("messageSync is exist message:{}", JsonUtils.toJSONString(context.getCrawlerMessage()));
            throw new IllegalArgumentException("messageSync is exist message:" + JsonUtils.toJSONString(context.getCrawlerMessage()));
        }

        MaocheRobotCrawlerMessageProductDO productDO = messageProducts.get(0);

        if (price != null && price > 0) {
            productDO.setPrice(price);
        }
        if (uiid != null && uiid > 0) {
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
