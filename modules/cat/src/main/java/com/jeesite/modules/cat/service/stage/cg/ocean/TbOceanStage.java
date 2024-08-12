package com.jeesite.modules.cat.service.stage.cg.ocean;

import cn.hutool.http.HttpUtil;
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
import com.jeesite.modules.cat.service.OkHttpService;
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
import okhttp3.HttpUrl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        MaocheRobotCrawlerMessageSyncDO syncDO = context.getMessageSync();
        String content = crawlerMessage.getMsg();

        // 判断是否含有中间页
        content = middlePageContent(content);
        // 覆盖一下
        crawlerMessage.setMsg(content);
        syncDO.setMsg(content);

        List<String> oriCommands = matchTbCommand(content);
        Map<String, CommandResponseV2> productMap = new HashMap<>();
        Map<String, Object> apiErrorMap = new HashMap<>();
        try {
            for (String command : oriCommands) {

                // https://www.veapi.cn/apidoc/taobaolianmeng/283
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("detail", 2);
                objectMap.put("deepcoupon", 1);
                objectMap.put("couponId", 1);
                // https://www.veapi.cn/apidoc/taobaolianmeng/283
                Result<CommandResponseV2> response = tbApiService.getCommonCommand(command, objectMap);

                if (!Result.isOK(response)) {
                    apiErrorMap.put(command, response);
                } else {
                    productMap.put(command, response.getResult());
                }

            }

        } catch (Exception e) {

        }

        if (MapUtils.isEmpty(productMap)) {
            Map<String, Object> remarks = new HashMap<>();
            remarks.put("api_error", apiErrorMap);
            context.setFailRemarks(JsonUtils.toJSONString(remarks));

            throw new QueryThirdApiException(QueryThirdApiException.QUERY_FAIL, "查询淘宝api失败");
        }

//        CommandResponseV2 commandResponse = response.getResult();
//        context.setTbProduct(commandResponse);
        context.setTbProductMap(productMap);
    }

    private List<String> matchTbCommand(String content) {
        String[] split = content.split("\n");
        List<String> commands = new ArrayList<>();
        for (String item : split) {
            Matcher matcher = CommandService.tb.matcher(item);
            if (matcher.find()) {
                commands.add(item);
            }
        }
        return commands;
    }

    public static void main(String[] args) {
        String s = "囤货装❗❗\n" +
                "淘豆玩国混合猫砂2.3kg*4包\n" +
                "\uD83D\uDCB051.6，折\uD83D\uDCB012.9/包\n" +
                "(TkxDWsy56jI)/ AC01";

        String[] split = s.split("\n");
        Map<String, String> urlMap = new HashMap<>();
        for (String item : split) {
            Matcher matcher = CommandService.tb.matcher(item);
            if (matcher.find()) {
                String group = matcher.group();
                urlMap.put(group, "");
            }
        }

        System.out.println(urlMap);
    }

    @Override
    public void buildBaseMessageProducts(OceanContext context) {

        Map<String, CommandResponseV2> productMap = context.getTbProductMap();
        if (MapUtils.isEmpty(productMap)) {
            throw new IllegalArgumentException("tbProduct is null");
        }

        List<MaocheRobotCrawlerMessageProductDO> messageProducts = new ArrayList<>();
        for (Map.Entry<String, CommandResponseV2> entry: productMap.entrySet()) {
            CommandResponseV2 tbProduct = entry.getValue();
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

            messageProducts.add(productDO);
        }

        context.setMessageProducts(messageProducts);

    }

    @Override
    public void saveMessageAndProduct(OceanContext context) {

        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        Map<String, CommandResponseV2> productMap = context.getTbProductMap();
        List<MaocheRobotCrawlerMessageProductDO> messageProducts = context.getMessageProducts();

        if (messageSync == null || MapUtils.isEmpty(productMap) || CollectionUtils.isEmpty(messageProducts)) {
            throw new IllegalArgumentException("messageSync or data or messageProducts is null");
        }

//        CommandResponseV2 tbProduct = context.getTbProduct();
//        String tbkPwd = tbProduct.getTbkPwd();

        Map<String, MaocheRobotCrawlerMessageProductDO> mseeageProductMap = messageProducts.stream().collect(Collectors.toMap(MaocheRobotCrawlerMessageProductDO::getItemId, Function.identity(), (o1, o2) -> o1));

        String msg = messageSync.getMsg();
        String[] split = msg.split("\n");
        StringBuilder msgBuilder = new StringBuilder();
        for (String item : split) {
            Matcher matcher = CommandService.tb.matcher(item);
            if (matcher.find()) {
                String group = matcher.group();
                CommandResponseV2 responseV2 = productMap.get(item);
                String tbkPwd = responseV2.getTbkPwd();
                msgBuilder.append(tbkPwd).append("\n");
            } else {
                msgBuilder.append(item).append("\n");
            }
        }
        msg = msgBuilder.toString();

        // 添加头尾
        msg = "✨有好价✨\n" + msg;
        msg = msg + "---------------------\n" + "自助查车@猫车选品官 +产品名";
        messageSync.setMsg(msg);

        long processed = 0;
        String status = "NORMAL";
        List<String> resourceIds = new ArrayList<>();
        for (Map.Entry<String, CommandResponseV2> entry : productMap.entrySet()) {
            CommandResponseV2 data = entry.getValue();
            String numIid = data.getNumIid();
            MaocheRobotCrawlerMessageProductDO productDO = mseeageProductMap.get(numIid);
            if (productDO == null) {
                continue;
            }

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

            if (uiid == 0) {
                Result<String> result = innerApiService.syncTbProduct(numIid);
                if (Result.isOK(result)) {
                    uiid = NumberUtils.toLong(result.getResult());
                } else {
                    // todo 同步接口判断已经存在的话，需要覆盖numiid为库里面的
                    String message = result.getMessage();
                    messageSync.addRemarks("auto_storage_error", message);
                }
            }

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
            resourceIds.add(resourceId);

            if (price > 0) {
                productDO.setPrice(price);
            }
            if (uiid > 0) {
                productDO.setInnerId(String.valueOf(uiid));
            }
        }

//        String numIid = data.getNumIid();
//        // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
//        String[] idArr = StringUtils.split(numIid, "-");
//        String itemIdSuffix = idArr[1];
//        Long price = 0L;
//        Long uiid = 0L;
//
//        MaocheAlimamaUnionProductDO unionProductDO = maocheAlimamaUnionProductService.getProduct(itemIdSuffix, "NORMAL");
//        // 在库商品，获取在库商品数据
//        if (unionProductDO != null) {
//            // 券后价
//            price = ProductValueHelper.calVeApiPromotionPrice(JSONObject.parseObject(unionProductDO.getOrigContent()));
//            uiid = unionProductDO.getUiid();
//        }
//        String status = "NORMAL";
//        if (uiid == 0) {
//            Result<String> result = innerApiService.syncTbProduct(numIid);
//            if (Result.isOK(result)) {
//                uiid = NumberUtils.toLong(result.getResult());
//            } else {
//                // todo 同步接口判断已经存在的话，需要覆盖numiid为库里面的
//                String message = result.getMessage();
//                messageSync.addRemarks("auto_storage_error", message);
//            }
//        }
//        long processed = 0;
//        String resourceId = itemIdSuffix;
//        if (uiid > 0 && unionProductDO == null) {
//            MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
//            query.setId(String.valueOf(uiid));
//            unionProductDO = maocheAlimamaUnionProductService.get(query);
//        }
//
//        if (unionProductDO != null && StringUtils.isNotBlank(unionProductDO.getIid())) {
//            resourceId = unionProductDO.getIid();
//            processed = 1;
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
//        condition.setResourceIds(Collections.singletonList(resourceId));
        condition.setResourceIds(resourceIds);
        condition.setAffType("tb");
        condition.setGteCreateDate(startTime);
        condition.setLteCreateDate(endTime);
        ElasticSearchData<MaocheMessageSyncIndex, CatProductBucketTO> searchMsg = oceanSearchService.searchMsg(
                condition,
                null,
                null,
                null,
                0, resourceIds.size());
        if (searchMsg != null && CollectionUtils.isEmpty(searchMsg.getDocuments())) {
            newProduct = 1;
        }

        messageSync.addRemarks("newProduct", newProduct);

        messageSync.setProcessed(processed);
        messageSync.setResourceIds(StringUtils.join(resourceIds, ","));
        messageSync.setStatus(status);
        boolean res = maocheRobotCrawlerMessageSyncService.addIfAbsent(messageSync);
        if (!res) {
            log.error("messageSync is exist message:{}", JsonUtils.toJSONString(context.getCrawlerMessage()));
            throw new IllegalArgumentException("messageSync is exist message:" + JsonUtils.toJSONString(context.getCrawlerMessage()));
        }

//        MaocheRobotCrawlerMessageProductDO productDO = messageProducts.get(0);

//        if (price > 0) {
//            productDO.setPrice(price);
//        }
//        if (uiid > 0) {
//            productDO.setInnerId(String.valueOf(uiid));
//        }
        for (MaocheRobotCrawlerMessageProductDO productDO : messageProducts) {
            fillMessageInfo2Product(messageSync, productDO);
            maocheRobotCrawlerMessageProductService.save(productDO);
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

    private String middlePageContent(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        StringBuilder builder = new StringBuilder();
        String[] split = content.split("\n");
        for (String line : split) {

            boolean contains = line.contains("y.q5url.cn");

            if (contains) {
                // 获取原地址:
                HttpUrl httpUrl = OkHttpService.doGetHttpUrl(line);
                if (httpUrl != null) {
                    String realUrl = httpUrl.toString();
                    try {
                        if (StringUtils.isNotBlank(realUrl)) {
                            // http://zzj.cute-cat.cn/dn2.html?taowords=nGEn3dC8n5C&image=https://img.alicdn.com/bao/uploaded/i1/2213875018643/O1CN013PW8262DiY9ILGevq_!!0-item_pic.jpg&url=https://s.tb.cn/h.gkh1uEe
                            URL url = new URL(realUrl);
                            Map<String, String> parameters = extractParameters(url);
                            if (MapUtils.isNotEmpty(parameters) && parameters.get("taowords") != null) {
                                // 7(nGEn3dC8n5C)/ CA1500
                                String tbCommand = "7(" + parameters.get("taowords") + ")/ CA1500";
                                builder.append(tbCommand).append("\n");
                            }
                        }
                    } catch (Exception e) {
                        log.error("middlePageContent 解析链接url失败 {}", realUrl, e);
                    }
                }

            } else {
                builder.append(line).append("\n");
            }
        }

        return builder.toString();
    }

    public static Map<String, String> extractParameters(URL url) {
        Map<String, String> parameters = new LinkedHashMap<>();
        String query = url.getQuery();

        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = idx > 0 ? pair.substring(0, idx) : pair;
                String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
                parameters.put(key, value);
            }
        }

        return parameters;
    }
}
