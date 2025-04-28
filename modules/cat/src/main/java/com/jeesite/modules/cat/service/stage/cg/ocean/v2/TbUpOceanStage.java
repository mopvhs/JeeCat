package com.jeesite.modules.cat.service.stage.cg.ocean.v2;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.codec.EncodeUtils;
import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.utils.UrlUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.ProductValueHelper;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.OkHttpService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.inner.InnerApiService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.cg.third.KdlApiService;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.cg.third.tb.dto.GeneralConvertResp;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.exception.QueryThirdApiException;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandContext;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TbUpOceanStage extends AbstraUpOceanStage {

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

    @Resource
    private KdlApiService kdlApiService;

    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

    @Override
    public String getAffType() {
        return "tb";
    }

    @Override
    public Pattern getPattern() {
        return CommandService.tb;
    }

    @Override
    public void queryProductFromThirdApi(OceanUpContext context) {
        // 1. 查询淘宝api获取商品数据
        // 2. 保存商品数据
        // 3. 保存商品数据到消息中
        MaocheRobotCrawlerMessageSyncDO syncDO = context.getMessageSync();
        String content = syncDO.getMsg();

        CommandContext commandContext = new CommandContext(content);
        context.setCommandContext(commandContext);
        commandContext.setResContent(content);

        // 判断是否含有中间页
        middlePageContent(commandContext);
        syncDO.setMsg(commandContext.getResContent());

        // 填充淘宝口令上下文
        fillMatchTbCommandContext(commandContext);

        List<ShortUrlDetail> shortUrlDetails = commandContext.listShortDetails();

        Map<String, GeneralConvertResp> productMap = new HashMap<>();
        Map<String, Object> apiErrorMap = new HashMap<>();
        boolean hasFailed = false;
        try {
            for (ShortUrlDetail urlDetail : shortUrlDetails) {
                long startTime = System.currentTimeMillis();
                boolean apiRes = false;
                String searchUrl = urlDetail.getSearchUrl();
                // https://www.veapi.cn/apidoc/taobaolianmeng/283
                Map<String, Object> objectMap = new HashMap<>();
//                objectMap.put("detail", 2);
//                objectMap.put("deepcoupon", 1);
//                objectMap.put("couponId", 1);
                // https://www.veapi.cn/apidoc/taobaolianmeng/283
                Result<GeneralConvertResp> response = tbApiService.generalConvert(searchUrl, objectMap);
                long left = System.currentTimeMillis() - startTime;
                if (!Result.isOK(response)) {
                    hasFailed = true;
                    apiErrorMap.put(searchUrl, response);
                    urlDetail.addExchangeLog("查询淘宝口令失败:" + JsonUtils.toJSONString(response));
                } else {
                    GeneralConvertResp commandResponseV2 = response.getResult();
                    productMap.put(searchUrl, commandResponseV2);
                    urlDetail.setTbProduct(commandResponseV2);
                    apiRes = true;
                }

                urlDetail.setApiRes(apiRes);
                urlDetail.setTs(left);
            }

        } catch (Exception e) {

        }
        syncDO.addCommandContext(commandContext);
        syncDO.addApiError(apiErrorMap);

        if (MapUtils.isEmpty(productMap) || hasFailed) {
            throw new QueryThirdApiException(QueryThirdApiException.QUERY_FAIL, "查询淘宝api失败");
        }

        context.setTbProductMap(productMap);
    }

    private void fillMatchTbCommandContext(CommandContext context) {

        String content = context.getContent();
        Map<String, ShortUrlDetail> shortUrlDetailMap = context.getShortUrlDetailMap();

        String[] split = content.split("\n");
        for (String item : split) {
            Matcher matcher = CommandService.tb.matcher(item);
            if (matcher.find()) {
                ShortUrlDetail detail = new ShortUrlDetail(item);
                detail.addExchangeLog(item);
                shortUrlDetailMap.put(StringUtils.trim2(item), detail);
            }
        }
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
    public void saveMessageAndProduct(OceanUpContext context) {

        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        Map<String, GeneralConvertResp> productMap = context.getTbProductMap();
        List<MaocheRobotCrawlerMessageProductDO> messageProducts = Optional.ofNullable(context.getMessageProducts()).orElse(new ArrayList<>());

        if (messageSync == null || (MapUtils.isEmpty(productMap) && CollectionUtils.isEmpty(messageProducts))) {
            throw new IllegalArgumentException("messageSync or (data and messageProducts) is null");
        }
        // 如果是券，是没有淘宝接续出来的商品的messageProducts
        Map<String, MaocheRobotCrawlerMessageProductDO> mseeageProductMap = messageProducts.stream().collect(Collectors.toMap(MaocheRobotCrawlerMessageProductDO::getItemId, Function.identity(), (o1, o2) -> o1));

        String msg = messageSync.getMsg();
        CommandContext commandContext = context.getCommandContext();
        if (commandContext != null && StringUtils.isNotBlank(commandContext.getResContent())) {
            msg = commandContext.getResContent();
        }
        String[] split = msg.split("\n");
        StringBuilder msgBuilder = new StringBuilder();
        for (String item : split) {
            Matcher matcher = CommandService.tb.matcher(item);
            if (matcher.find()) {
                item = StringUtils.trim2(item);
                String group = matcher.group();
                GeneralConvertResp responseV2 = productMap.get(item);
                if (responseV2 != null) {
                    String tbkPwd = responseV2.getTbkPwd();
                    msgBuilder.append(tbkPwd).append("\n");
                }
            } else {
                msgBuilder.append(item).append("\n");
            }
        }
        msg = msgBuilder.toString();

        // 添加头尾
        msg = "✨有好价✨\n" + msg;
        msg = msg + "---------------------\n" + "自助查车 dwz.cn/qveM26UV";
        messageSync.setMsg(msg);

        long processed = 0;
        List<String> resourceIds = new ArrayList<>();
        List<String> hashProducts = new ArrayList<>();
        for (Map.Entry<String, GeneralConvertResp> entry : productMap.entrySet()) {
            GeneralConvertResp data = entry.getValue();
            String numIid = GeneralConvertResp.analyzingItemId(data);
            if (StringUtils.isBlank(numIid)) {
                continue;
            }
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
            if (unionProductDO != null && StringUtils.isNotBlank(unionProductDO.getOrigContent())) {
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

            GeneralConvertResp.ItemBasicInfo itemBasicInfo = data.getItemBasicInfo();
            if (itemBasicInfo != null) {
                String sellerId = itemBasicInfo.getSellerId();
                String title = itemBasicInfo.getTitle();
                hashProducts.add(sellerId + "_" + title);
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

        String hash = null;
        if (!context.isIgnoreSimHash() && CollectionUtils.isNotEmpty(hashProducts)) {
            // 排序
            hashProducts = hashProducts.stream().sorted().collect(Collectors.toList());
            // toJsonString -> md5
            hash = Md5Utils.md5(JsonUtils.toJSONString(hashProducts));
        }

        String status = OceanStatusEnum.NORMAL.name();
        // todo 有部分是忽略相似度判断的，需要忽略
        if (StringUtils.isNotBlank(hash)) {
            // 获取今天凌晨4点的时间戳
            Date startTime = DateUtils.getOfDayFirst(new Date(), 2);
            // 如果当前时间小于4点，则获取前一天的数据
            if (startTime.getTime() > System.currentTimeMillis()) {
                startTime = DateUtils.getOfDayFirst(DateUtils.addDays(new Date(), -1), 4);
            }

            String date = DateTimeUtils.getStringDate(startTime);
            List<MaocheRobotCrawlerMessageSyncDO> simProductSyncMsgList = maocheRobotCrawlerMessageSyncDao.listByProductUniqueHash(hash, "NORMAL", date);
            if (CollectionUtils.isNotEmpty(simProductSyncMsgList)) {
                simProductSyncMsgList = simProductSyncMsgList.stream().filter(i -> !i.getId().equals(messageSync.getId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(simProductSyncMsgList)) {
                    status = OceanStatusEnum.SIMILAR.name();
                }
            }
        }

        // 获取商品额时间
        Date createDate = messageSync.getCreateDate();
        int newProduct = 0;
        // 获取3天前的开始时间
        long startTime = DateUtils.getOfDayFirst(DateUtils.addDays(createDate, -3)).getTime();
        // 获取今天开始时间
        long endTime = DateUtils.getOfDayFirst(createDate).getTime() - 1;
        // 判断3天前内是否存在
        if (CollectionUtils.isNotEmpty(resourceIds)) {
            // resourceIds 排序
            Collections.sort(resourceIds);

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
        } else {
            newProduct = 1;
        }

        messageSync.addRemarks("newProduct", newProduct);
        messageSync.setStatus(status);
        messageSync.setProcessed(processed);
        messageSync.setResourceIds(StringUtils.join(resourceIds, ","));
        messageSync.setProductHash(hash);
        boolean res = maocheRobotCrawlerMessageSyncService.updateById(messageSync);
        if (!res) {
            log.error("messageSync is exist message:{}", JsonUtils.toJSONString(messageSync));
            throw new IllegalArgumentException("messageSync update fail  message:" + JsonUtils.toJSONString(messageSync));
        }

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

    private void middlePageContent(CommandContext commandContext) {
        if (commandContext == null || StringUtils.isBlank(commandContext.getResContent())) {
            return;
        }

        String content = commandContext.getResContent();
        StringBuilder builder = new StringBuilder();
        String[] split = content.split("\n");
        for (String line : split) {
            String errorMsg = null;
            boolean contains = line.contains("y.q5url.cn");
            if (contains) {
                // 获取原地址:
                HttpUrl httpUrl = OkHttpService.doGetHttpUrlWithProxy(line);
                if (httpUrl != null) {
                    String realUrl = httpUrl.toString();
                    try {
                        if (StringUtils.isNotBlank(realUrl)) {
                            // http://zzj.cute-cat.cn/dn2.html?taowords=nGEn3dC8n5C&image=https://img.alicdn.com/bao/uploaded/i1/2213875018643/O1CN013PW8262DiY9ILGevq_!!0-item_pic.jpg&url=https://s.tb.cn/h.gkh1uEe
                            URL url = new URL(realUrl);
                            Map<String, String> parameters = extractParameters(url);
                            if (MapUtils.isNotEmpty(parameters)) {
                                String key = Optional.ofNullable(parameters.get("taowords")).orElse(parameters.get("word"));
                                if (StringUtils.isNotBlank(key)) {
                                    // 7(nGEn3dC8n5C)/ CA1500
                                    String tbCommand = "7(" + key + ")/ CA1500";
                                    builder.append(tbCommand).append("\n");
                                } else {
                                    DingDingService.sendParseDingDingMsg("zzj.cute-cat.cn获取key失败 url:{}", url);
                                    errorMsg = "代理获取 " + line + " 的taobao转链key为空" + " 地址为：" + url;
                                }
                            } else {
                                errorMsg = "代理获取 " + line + " 的原始parameters为空";
                            }
                        } else {
                            errorMsg = "代理获取 " + line + " 的原始url为空";
                        }
                    } catch (Exception e) {
                        log.error("middlePageContent 解析链接url失败 {}", realUrl, e);
                    }
                } else {
                    errorMsg = "原数据：" + line + "包含y.q5url.cn域名，但是获取httpUrl失败";
                }
            } else if (line.contains("y-03.cn")) {
                HttpUrl httpUrl = OkHttpService.doGetHttpUrlWithProxy(line);
                if (httpUrl != null) {
                    String realUrl = httpUrl.toString();
                    if (realUrl.contains("sup331.kuaizhan.com")) {
                        Map<String, String> parameterMap = UrlUtils.getParametersWithSpilt(realUrl);
                        String value = parameterMap.get("k");
                        if (StringUtils.isNotBlank(value)) {
                            String apiUrl = "https://api.cmsv5.iyunzk.com/apis/SuperPage/get?cms_request=1&device_type=web&typ=&key=%s&redirect_url=%s";
                            apiUrl = String.format(apiUrl, value, EncodeUtils.encodeUrl(realUrl));
                            String s = FlameHttpService.doGet(apiUrl);

                            if (StringUtils.isNotBlank(s)) {
                                // 获取淘客口令
                                JSONObject jsonObject = JSONObject.parseObject(s);
                                if (jsonObject != null && jsonObject.getInteger("code") != null && jsonObject.getInteger("code") == 200) {
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    if (data != null && data.getJSONArray("list") != null) {
                                        JSONArray list = data.getJSONArray("list");
                                        String formKey = "form";
                                        for (int i = 0; i < list.size(); i++) {
                                            JSONObject item = list.getJSONObject(i);
                                            if (item == null || item.getJSONArray(formKey) == null) {
                                                continue;
                                            }
                                            JSONArray from = item.getJSONArray(formKey);
                                            for (int j = 0; j < from.size(); j++) {
                                                JSONObject resource = from.getJSONObject(j);
                                                if (resource == null || !"tkl".equals(resource.getString("field"))) {
                                                    continue;
                                                }
                                                // <p>公种号\"可可车\"<\/p><p>$6UQ23eCbwJ5$<br\/><\/p>" 可能情况 1
                                                String tklRichText = resource.getString("value");
                                                // 整个内容直接请求维易接口，做替换
                                                // https://www.veapi.cn/apidoc/taobaolianmeng/283
                                                Map<String, Object> objectMap = new HashMap<>();
                                                objectMap.put("detail", 2);
                                                objectMap.put("deepcoupon", 1);
                                                objectMap.put("couponId", 1);
                                                // https://www.veapi.cn/apidoc/taobaolianmeng/283
                                                Result<CommandResponseV2> response = tbApiService.getCommonCommand(tklRichText, objectMap);

                                                String tbkPwd = null;
                                                if (Result.isOK(response)) {
                                                    CommandResponseV2 tbProduct = response.getResult();
                                                    tbkPwd = tbProduct.getTbkPwd();
                                                    if (tbkPwd != null) {
                                                        builder.append(tbkPwd).append("\n");
                                                    } else {
                                                        builder.append(realUrl);
                                                    }
                                                }

                                                DingDingService.sendParseDingDingMsg("tb redirectUrl {}, \ntklRichText {}, \n转链后 tbProduct:{}", realUrl, tklRichText, tbkPwd);
                                                break;
                                            }

                                        }
                                    }
                                }
                            } else {
                                errorMsg = "代理获取 " + line + " 的tb快站地址解析不成功，错误内容：" + s;

                                DingDingService.sendParseDingDingMsg("tb 快站{},解析获取不到淘客地址不成功", line);
                            }

                        } else {
                            DingDingService.sendParseDingDingMsg("tb 快站{},解析获取不到k的参数", line);
                            errorMsg = "代理获取 " + line + " 的tb 快站url解析获取不到k的参数，快站地址:" + realUrl;
                        }
                    }
                } else {
                    errorMsg = "代理获取 " + line + " 的原始url为空";
                }


            } else {
                builder.append(line).append("\n");
            }

            if (StringUtils.isNotBlank(errorMsg)) {
                commandContext.addErrors(errorMsg);
            }
        }

        commandContext.setResContent(builder.toString());
        commandContext.setContent(commandContext.getResContent());
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
