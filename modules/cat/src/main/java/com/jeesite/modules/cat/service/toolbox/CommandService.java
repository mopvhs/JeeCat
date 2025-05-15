package com.jeesite.modules.cat.service.toolbox;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.codec.EncodeUtils;
import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.utils.UrlUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheProductV2DO;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.helper.ProductValueHelper;
import com.jeesite.modules.cat.service.CsOpLogService;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheProductV2Service;
import com.jeesite.modules.cat.service.OkHttpService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
import com.jeesite.modules.cat.service.cg.third.DwzApiService;
import com.jeesite.modules.cat.service.cg.third.dto.DwzShortUrlDetail;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.cg.third.tb.dto.GeneralConvertResp;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandContext;
import com.jeesite.modules.cat.service.toolbox.dto.CommandDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 口令接口
 */
@Slf4j
@Component
public class CommandService {

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private DingDanXiaApiService dingDanXiaApiService;

    @Resource
    private TbApiService tbApiService;

    @Resource
    private CsOpLogService csOpLogService;

    @Resource
    private CacheService cacheService;

    @Resource
    private MaocheProductV2Service maocheProductV2Service;

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private DwzApiService dwzApiService;

    // 淘宝
//    public static Pattern tb = Pattern.compile("\\((.*?)\\)\\/|\\/(.*?)\\/\\/");
    public static Pattern tb = Pattern.compile("\\((.*?)\\)\\/|\\/(.*?)\\/\\/|\\$(.*?)\\$://|\\$(.*?)\\)|\\¥(.*?)\\¥");

    // 京东
    public static Pattern jd = Pattern.compile("(http|https):\\/\\/[a-zA-Z0-9-\\.]+\\.[a-z]{2,}(\\/\\S*)");

    public static Pattern url = Pattern.compile("https?:\\/\\/[^\\s]+|[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}[\\/?]*[^\\s]*");

    public Result<CommandDTO> exchangeCommand(String content, String type) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(type)) {
            return Result.ERROR(500, "参数不能为空");
        }

        CommandContext context = new CommandContext();
        context.setContent(content);
        // 淘宝
        if ("tb".equals(type)) {
            return doExchangeTb(context);
        }

        if ("jd".equals(type) || "dwz".equals(type)) {
            return doDwz(context);
        }

        return Result.OK(null);
    }

    public Result<CommandDTO> doDwz(CommandContext context) {

        Result<CommandDTO> exchangeJd = doExchangeJd(context);
        if (!Result.isOK(exchangeJd)) {
            return exchangeJd;
        }
        CommandDTO data = exchangeJd.getResult();
        data.setShortUrlDetailMap(context.getShortUrlDetailMap());

        String jdResultContent = data.getContent();
        if (StringUtils.isBlank(jdResultContent)) {
            return Result.ERROR(500, "参数不能为空");
        }

        // 判断是否支持 dwz
        matchDwzUrl(context);

        for (ShortUrlDetail detail : context.listShortDetails()) {
            if (!detail.isSupportDwz() || StringUtils.isBlank(detail.getSupportDwzUrl())) {
                continue;
            }

            long startTime = System.currentTimeMillis();
            String oriUrl = detail.getSupportDwzUrl();
            // 获取短地址md5
            String shortUrl = getDwzByUrl(oriUrl);
            if (StringUtils.isBlank(shortUrl)) {
                continue;
            }

            // 短网址时间
            detail.setDwzTs(System.currentTimeMillis() - startTime);
            detail.addExchangeLog(shortUrl);
            detail.setReplaceUrl(shortUrl);
            detail.setApiRes(true);
            jdResultContent = jdResultContent.replace(oriUrl, shortUrl);
        }

        data.setContent(jdResultContent);
        context.setResContent(jdResultContent);
        if (data.getProducts() == null) {
            data.setProducts(new ArrayList<>());
        }
        exchangeJd.setCode(200);
        exchangeJd.setSuccess(true);
        return exchangeJd;
    }

    /**
     * 获取短地址
     * @param url
     * @return
     */
    public String getDwzByUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        // 获取短地址md5
        String key = "dwz_" + Md5Utils.md5(url);
        String shortUrl = cacheService.get(key);
        if (StringUtils.isBlank(shortUrl)) {
            Result<DwzShortUrlDetail> rpcRes = dwzApiService.shortUrl(url, false);
            if (rpcRes.isSuccess() && rpcRes.getResult() != null && StringUtils.isNotBlank(rpcRes.getResult().getShortUrl())) {
                shortUrl = rpcRes.getResult().getShortUrl();
                cacheService.setWithExpireTime(key, shortUrl, (int) TimeUnit.DAYS.toSeconds(7));
            }
        }

        return shortUrl;
    }

    /**
     * 匹配是否支持短网址转换
     * @param context
     */
    private void matchDwzUrl(CommandContext context) {
        if (context == null || MapUtils.isEmpty(context.getShortUrlDetailMap())) {
            return;
        }

        List<String> containUrls = new ArrayList<>();
        containUrls.add("coupon.m.jd.com");
        containUrls.add("pro.m.jd.com");
        containUrls.add("h5static.m.jd.com");
        containUrls.add("shopmember.m.jd.com");
        containUrls.add("coupon.jd.com");
        containUrls.add("activities.m.jd.com");

        List<ShortUrlDetail> shortDetails = context.listShortDetails();
        for (ShortUrlDetail detail : shortDetails) {
            String checkUrl = detail.getContentUrl();
            if (StringUtils.isNotBlank(detail.getReplaceUrl())) {
                checkUrl = detail.getReplaceUrl();
            }
            if (!checkUrl.contains("http")) {
                continue;
            }
            try {
                URL url = new URL(checkUrl);
                String host = url.getHost();

                if (containUrls.contains(host)) {
                    detail.setSupportDwz(true);
                    detail.setSupportDwzUrl(checkUrl);
                }
            } catch (Exception e) {
                log.error("短地址转换失败", e);
            }
        }
    }

    private Result<CommandDTO> doExchangeTb(CommandContext context) {
        if (StringUtils.isBlank(context.getContent())) {
            return Result.ERROR(500, "参数不能为空");
        }

        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Map<String, Object> objectMap = new HashMap<>();
//        objectMap.put("detail", 2);
//        objectMap.put("deepcoupon", 2);
        String content = context.getContent();
        String redisKey = Md5Utils.md5(content) + "_v2";
        String redisValue = cacheService.get(redisKey);
        if (StringUtils.isNotBlank(redisValue)) {
            return JsonUtils.toReferenceType(redisValue, new TypeReference<Result<CommandDTO>>() {
            });
        }

        Result<GeneralConvertResp> response = tbApiService.generalConvert(content, objectMap);
        // 日志记录
//        csOpLogService.addLog("tb", "doExchangeTb", "tb_command", "maoche", "tb转链",
//                content, JsonUtils.toJSONString(response));
        CommandDTO commandDTO = new CommandDTO();
        if (Result.isOK(response)) {
            GeneralConvertResp data = response.getResult();
            GeneralConvertResp.ItemBasicInfo itemBasicInfo = data.getItemBasicInfo();
            GeneralConvertResp.PricePromotionInfo pricePromotionInfo = data.getPricePromotionInfo();
            Matcher matcher = tb.matcher(content);
            String replaceAll = content;
            if (matcher.find()) {
                String match = matcher.group();
                replaceAll = StringUtils.replace(content, match, data.getTbkPwd());
            } else {
                replaceAll = data.getTbkPwd();
            }

            commandDTO.setContent(replaceAll);
            List<CommandDTO.Product> products = new ArrayList<>();
            CommandDTO.Product product = new CommandDTO.Product();

            product.setCommand(data.getTbkPwd());
            if (StringUtils.isNotBlank(data.getCouponShortUrl())) {
                product.setCouponUrls(Collections.singletonList(data.getCouponShortUrl()));
                product.setCouponUrl(data.getCouponShortUrl());
            }
            CommandDTO.Item item = new CommandDTO.Item();
            if (data.getItemBasicInfo() != null) {
                String itemUrlFormat = "https://uland.taobao.com/item/edetail?id=%s";
                product.setItemUrl(String.format(itemUrlFormat, data.getItemId()));

                item.setImage(itemBasicInfo.getPictUrl());

                // 券后价
                long promotionPrice = ProductValueHelper.calVeApiPromotionPrice(JSONObject.parseObject(JsonUtils.toJSONString(data)));
                item.setReservePrice(promotionPrice);

                item.setOriginalPrice(new BigDecimal(pricePromotionInfo.getReservePrice()).multiply(new BigDecimal(100)).longValue());
//            item.setShopDsr(NumberUtils.toLong(data.getShopDsr()));
                // 接口升级为detail=2之后，shopDsr字段被移除
                item.setShopDsr(0L);
                item.setVolume(NumberUtils.toLong(itemBasicInfo.getVolume()));
                item.setNumIid(data.getItemId());
                item.setTitle(itemBasicInfo.getTitle());
                item.setCommissionRate(new BigDecimal(data.getCommissionRate()).multiply(new BigDecimal(100)).longValue());
                item.setShopTitle(itemBasicInfo.getShopTitle());

                String numIid = data.getItemId();
                // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
                String[] idArr = StringUtils.split(numIid, "-");
                String itemId = idArr[1];
                MaocheProductV2DO unionProductDO = maocheProductV2Service.getProduct(itemId, "NORMAL");
                if (unionProductDO != null) {
                    Long uiid = unionProductDO.getProductId();
                    item.setId(uiid);
                }

            } else {
                item.setImage("");
                // 券后价
                item.setReservePrice(0L);
                item.setOriginalPrice(0L);
                item.setShopDsr(0L);
                item.setVolume(0L);
                item.setNumIid(data.getTbkPwd());
                item.setTitle(data.getCpsFullTpwd());
                item.setCommissionRate(0L);
                item.setShopTitle("");
            }

            product.setItem(item);
            products.add(product);
            commandDTO.setProducts(products);
            Result<CommandDTO> result = Result.OK(commandDTO);
            if (item.getId() != null) {
                cacheService.setWithExpireTime(redisKey, JsonUtils.toJSONString(result), 600);
            }
            return result;
        }
        String message = response.getMessage();
        // 产品或活动转链失败，请检查参数。高佣转链提示 :  该宝贝已下架或非淘客宝贝
        if (message.contains("该商品已下架或非淘宝联盟") || message.contains("该宝贝已下架或非淘客宝贝")) {
            // 判断是否是库内商品，是的话直接下架
            // 判断是否是商品的itemId
            String[] split = StringUtils.split(content, "-");
            if (split.length == 2) {
                // 插叙是否再在库内
                String itemId = split[1];
                MaocheProductV2DO unionProductDO = maocheProductV2Service.getProduct(itemId, "NORMAL");
                if (unionProductDO != null) {
                    // 执行下架
                    List<Long> productIds = Collections.singletonList(unionProductDO.getProductId());
                    int auditStatus = maocheAlimamaUnionProductDao.updateSaleStatus(productIds,
                            SaleStatusEnum.AUTO_OFF_SHELF.getStatus(),
                            null);

                    if (auditStatus > 0) {
                        // 重新查一次数据库
                        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductService.listByIds(productIds);
                        cgUnionProductService.indexEs(productDOs, 10);
                    }
                    String msgFormat = "{} \n 口令获取商品失效，自动下架：{}, 下架结果：{}";
                    DingDingService.sendParseDingDingMsg(msgFormat, 1, content, JSONUtil.toJsonStr(response), auditStatus);
                }
            }
        }

        String msgFormat = "{} \n 转链结果：{}";
        DingDingService.sendParseDingDingMsg(msgFormat, 1, content, JSONUtil.toJsonStr(response));

        return Result.ERROR(response.getCode(), response.getMessage());
    }

    private Result<CommandDTO> doExchangeJd(CommandContext context) {
        if (context == null || StringUtils.isBlank(context.getContent())) {
            return Result.ERROR(500, "参数不能为空");
        }

        String content = context.getContent();
        Map<String, ShortUrlDetail> shortUrlDetailMap = new HashMap<>();
        context.setShortUrlDetailMap(shortUrlDetailMap);

        String[] split = StringUtils.split(content, "\n");
        for (String item : split) {
            Matcher matcher = jd.matcher(item);
            if (matcher.find()) {
                ShortUrlDetail detail = new ShortUrlDetail();
                String group = matcher.group();
                detail.setContentUrl(group);
                // 原始地址
                detail.addExchangeLog(group);
                shortUrlDetailMap.put(group, detail);
            }
        }
        if (MapUtils.isEmpty(shortUrlDetailMap)) {
            context.addErrors("未匹配到需要转链的地址");
            return Result.ERROR(500, "分析需要替换的链接失败");
        }

        boolean match = false;
        StringBuilder errorMsg = new StringBuilder();
        Map<String, String> commandMap = new HashMap<>();
        List<CommandDTO.Product> products = new ArrayList<>();
        for (Map.Entry<String, ShortUrlDetail> entry : shortUrlDetailMap.entrySet()) {
            match = false;
            String apiError = null;
            String url = entry.getKey();
            ShortUrlDetail detail = entry.getValue();

            long startTime = System.currentTimeMillis();
            // 获取原地址
            Result<String> redirectRes = getRedirectUrl(url, null);
            String redirectUrl = null;
            if (redirectRes != null) {
                redirectUrl = redirectRes.getResult();
                detail.addExchangeLog(redirectRes.getMessage());

                // 微信客户端内打开-直接整个单子认为失败
                if (redirectRes.getCode() == 10010 || redirectRes.getCode() == 10011) {
                    match = false;
                    break;
                } else if (redirectRes.getCode() == 20010) {
                    match = false;
                    break;
                }
            }

            Result<JdUnionIdPromotion> result = new Result<>();
            String searchUrl = Optional.ofNullable(redirectUrl).orElse(url);
            detail.setSearchUrl(searchUrl);

            if (searchUrl.contains("coupon.m.jd.com")) {
                match = true;
                result.setSuccess(true);
                // 获取到转链后的地址
                detail.setApiRes(true);
                detail.setReplaceUrl(searchUrl);
                detail.addExchangeLog(searchUrl);
                content = content.replace(url, searchUrl);
                continue;
            }

            if (needAnalysisTbUrl(searchUrl)) {
                result = Result.ERROR(304, "淘客链接");
            } else {
                result = dingDanXiaApiService.jdByUnionidPromotionWithCoupon("8On9yn1NtuJhyTMHzkz5p83YtGyXGnB6", searchUrl, 1002248572L, 3100684498L);
                List<String> retryDomains = new ArrayList<>();
                retryDomains.add("3.cn");
                retryDomains.add("m.cute-cat.cn");
                // 失败重试1次
                if (!Result.isOK(result)) {
                    redirectRes = getRedirectUrl(searchUrl, retryDomains);
                    if (redirectRes != null) {
                        redirectUrl = redirectRes.getResult();
                        searchUrl = Optional.ofNullable(redirectUrl).orElse(searchUrl);
                        detail.addExchangeLog("转链时候后重试获取结果:" + redirectRes.getMessage());
                    }

                    result = dingDanXiaApiService.jdByUnionidPromotionWithCoupon("8On9yn1NtuJhyTMHzkz5p83YtGyXGnB6", searchUrl, 1002248572L, 3100684498L);
                }
            }
            if (Result.isOK(result) && StringUtils.isNotBlank(redirectUrl) && redirectUrl.contains(".kuaizhan.com")) {
                // 单纯只有链接，非商品，而且是快站的
                JdUnionIdPromotion promotion = result.getResult();
                if (StringUtils.isBlank(promotion.getSkuId())) {
                    result.setSuccess(false);
                }
            }

//            commandMap.put(url, JsonUtils.toJSONString(result));
            if (Result.isOK(result)) {
                JdUnionIdPromotion promotion = result.getResult();

                // 获取到转链后的地址
                detail.setApiRes(true);
                detail.setReplaceUrl(promotion.getShortURL());
                detail.addExchangeLog(promotion.getShortURL());
                detail.setPromotion(promotion);
                content = content.replace(url, promotion.getShortURL());

                match = true;
                // 构建京东的商品
                CommandDTO.Product product = buildJdProduct(promotion);
                if (product == null) {
                    continue;
                }
                products.add(product);
            } else {
//                try {
//                    csOpLogService.addLog(searchUrl, "fail_exchange_jd", "jd_command", "maoche", "订单侠jd转链失败",
//                            content, JsonUtils.toJSONString(result));
//                } catch (Exception e) {
//                    log.error("京东转链失败写日志异常，searchUrl:{}", searchUrl, e);
//                }

                apiError = searchUrl + "订单侠搜索失败：" + result.getMessage();
                if (StringUtils.isNotBlank(redirectUrl)) {
                    boolean isReplace = true;
                    detail.setReplaceUrl(redirectUrl);
                    detail.addExchangeLog("存在重定向链接, 判断是否执行重定向转链：" + redirectUrl);
                    // 判断域名是否是快站的
                    if (needAnalysisTbUrl(redirectUrl)) {
                        // 获取原地址:
                        try {
                            // http://zzj.cute-cat.cn/dn2.html?taowords=nGEn3dC8n5C&image=https://img.alicdn.com/bao/uploaded/i1/2213875018643/O1CN013PW8262DiY9ILGevq_!!0-item_pic.jpg&url=https://s.tb.cn/h.gkh1uEe
                            URL urlObj = new URL(EncodeUtils.decodeUrl(redirectUrl));
//                                Map<String, String> parameters = extractParameters(urlObj);
                            Map<String, Param> parameterMap = extractParameterMap(urlObj);
                            if (MapUtils.isNotEmpty(parameterMap)) {

                                Param param = Optional.ofNullable(parameterMap.get("taowords")).orElse(parameterMap.get("word"));

                                if (param != null && CollectionUtils.isNotEmpty(param.getValues())) {
                                    // 如果只有一个值，保持和以前一样
                                    if (param.getValues().size() == 1) {
                                        // 7(nGEn3dC8n5C)/ CA1500
                                        Result<GeneralConvertResp> response = apiTb(param.getValues().get(0));

                                        String tbkPwd = null;
                                        if (Result.isOK(response)) {
                                            GeneralConvertResp tbProduct = response.getResult();
                                            tbkPwd = tbProduct.getTbkPwd();
                                            detail.setApiRes(true);
                                            if (tbkPwd != null) {
                                                detail.setReplaceUrl(tbkPwd);
                                                detail.addExchangeLog(tbkPwd);
                                            }
                                            detail.setTbProduct(tbProduct);
                                            content = content.replace(url, tbProduct.getTbkPwd());
                                            match = true;
                                            isReplace = false;
                                        }
                                    } else {

                                        StringBuilder replaceContent = new StringBuilder();
                                        Param tipParam = parameterMap.get("tips");
                                        List<String> tips = new ArrayList<>();
                                        if (tipParam != null && CollectionUtils.isNotEmpty(tipParam.getValues())) {
                                            tips = tipParam.getValues();
                                        }

                                        for (int i = 0; i < param.getValues().size(); i++) {
                                            String key = param.getValues().get(i);
                                            Result<GeneralConvertResp> response = apiTb(key);

                                            String tbkPwd = null;
                                            if (Result.isOK(response)) {
                                                GeneralConvertResp tbProduct = response.getResult();
                                                tbkPwd = tbProduct.getTbkPwd();
                                                detail.setApiRes(true);
                                                if (tbkPwd != null) {
                                                    detail.setReplaceUrl(tbkPwd);
                                                    detail.addExchangeLog(tbkPwd);
                                                }
                                                detail.setTbProduct(tbProduct);
                                                match = true;

                                                if (tips.size() > i) {
                                                    replaceContent.append(tips.get(i)).append("\n");
                                                }
                                                replaceContent.append(tbProduct.getTbkPwd()).append("\n");

                                            } else {
                                                match = false;
                                                break;
                                            }
                                        }
                                        if (match) {
                                            isReplace = false;
                                            content = content.replace(url, replaceContent);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("middlePageContent 解析链接url失败 {}", redirectUrl, e);
                        }
                    }  else if (redirectUrl.contains(".kuaizhan.com")) {
                        Map<String, String> parameterMap = UrlUtils.getParametersWithSpilt(redirectUrl);
                        String value = parameterMap.get("k");
                        if (StringUtils.isNotBlank(value)) {
                            String apiUrl = "https://api.cmsv5.iyunzk.com/apis/SuperPage/get?cms_request=1&device_type=web&typ=&key=%s&redirect_url=%s";
                            apiUrl = String.format(apiUrl, value, EncodeUtils.encodeUrl(redirectUrl));
                            String s = FlameHttpService.doGet(apiUrl);

                            if (StringUtils.isNotBlank(s)) {
                                // 获取淘客口令
                                String tkl = null;
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
//                                                objectMap.put("detail", 2);
//                                                objectMap.put("deepcoupon", 1);
//                                                objectMap.put("couponId", 1);
                                                // https://www.veapi.cn/apidoc/taobaolianmeng/283
                                                Result<GeneralConvertResp> response = tbApiService.generalConvert(tklRichText, objectMap);

                                                String tbkPwd = null;
                                                if (Result.isOK(response)) {
                                                    GeneralConvertResp tbProduct = response.getResult();
                                                    tbkPwd = tbProduct.getTbkPwd();
                                                    detail.setApiRes(true);
                                                    if (tbkPwd != null) {
                                                        detail.setReplaceUrl(tbkPwd);
                                                        detail.addExchangeLog(tbkPwd);
                                                    }

                                                    match = true;
                                                    detail.setTbProduct(tbProduct);
                                                    content = content.replace(url, tbProduct.getTbkPwd());
                                                }

                                                DingDingService.sendParseDingDingMsg("redirectUrl {}, \ntklRichText {}, \n转链后 tbProduct:{}", redirectUrl, tklRichText, tbkPwd);
                                                break;
                                            }

                                        }
                                    }
                                } else {
                                    detail.addExchangeLog("重定向链接【" + redirectUrl + "】, 转链失败" + s);
                                }
                            } else {
                                match = false;
                                isReplace = false;
                            }

                        } else {
                            detail.addExchangeLog("重定向链接【" + redirectUrl + "】, 解析获取不到k的参数，不执行转链");
                            DingDingService.sendParseDingDingMsg("快站{},解析获取不到k的参数", redirectUrl);
                        }
                    } else {
                        detail.addExchangeLog("重定向链接【" + redirectUrl + "】, 非sup331.kuaizhan.com，不执行重定向转链");
                    }

                    if (isReplace) {
                        content = content.replace(url, redirectUrl);
                    }
                }
                errorMsg.append(url).append("\n");
            }

            if (BooleanUtils.isNotTrue(detail.getApiRes()) && StringUtils.isNotBlank(apiError)) {
                detail.setErrorMsg(apiError);
            }
            detail.setTs(System.currentTimeMillis() - startTime);
            if (!match) {
                break;
            }
        }

        String resourceId = Optional.ofNullable(context.getRelationId()).orElse("jd");
        // 日志记录
//        csOpLogService.addLog(resourceId, "doExchangeJd", "jd_command", "maoche", "jd转链",
//                content, JsonUtils.toJSONString(commandMap));

        // 最终转换后的结果
        context.setResContent(content);

        if (!match) {
            String msgFormat = "{} \n 转链结果：{}";
            DingDingService.sendParseDingDingMsg(msgFormat, 1, content, errorMsg.toString());
            Result<CommandDTO> error = Result.ERROR(401, errorMsg.toString());
            CommandDTO commandDTO = new CommandDTO();
            commandDTO.setContent(content);
            error.setResult(commandDTO);
            return error;
        }

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setContent(content);
        commandDTO.setProducts(products);
        Result<CommandDTO> result = Result.OK(commandDTO);
        if (StringUtils.isNotBlank(errorMsg.toString())) {
            errorMsg.append("以上链接未转链，请辨别");
            result.setMessage(errorMsg.toString());
        }
        return result;
    }

    private Result<GeneralConvertResp> apiTb(String tbk) {
        String tbCommand = "7(" + tbk + ")/ CA1500";

        // 整个内容直接请求维易接口，做替换
        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("detail", 2);
        objectMap.put("deepcoupon", 1);
        objectMap.put("couponId", 1);
        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        return tbApiService.generalConvert(tbCommand, objectMap);
    }

    private static boolean needAnalysisTbUrl(String searchUrl) {
        if (StringUtils.isBlank(searchUrl)) {
            return false;
        }

        return searchUrl.contains("t.q5url.cn/tkl.html") ||
                searchUrl.contains("t.q5url.cn") ||
                searchUrl.contains("i.kunq5.cn") ||
                searchUrl.contains("qianwum.cn") ||
                searchUrl.contains("suz039.kuaizhan.com") ||
                searchUrl.contains("cyg888.cn");
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

    public static Map<String, Param> extractParameterMap(URL url) {
        Map<String, Param> parameterMap = new LinkedHashMap<>();
//        String query = url.getQuery();
        String string = url.toString();
        int i = string.indexOf('?');
        if (i <= 0) {
            return new HashMap<>();
        }
        String query = string.substring( i + 1);

        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = idx > 0 ? pair.substring(0, idx) : pair;
                String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
                Param param = parameterMap.get(key);
                if (param == null) {
                    param = new Param();
                    param.setKey(key);
                }

                param.addValue(value);

                parameterMap.put(key, param);
            }
        }

        return parameterMap;
    }

    @Data
    public static class Param implements Serializable {

        @Serial
        private static final long serialVersionUID = 3207166375858223999L;

        private String key;

        private List<String> values;

        public void addValue(String value) {
            if (CollectionUtils.isEmpty(values)) {
                values = new ArrayList<>();
            }
            values.add(value);
        }
    }


    /**
     * @param content
     * @return
     */
    private static String matchUrl(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String[] split = content.split(" ");
        for (String item : split) {
            Matcher matcher = jd.matcher(item);
            if (matcher.find()) {
                String match = matcher.group();
                int i = StringUtils.indexOf(match, "http");
                if (i == 0) {
                    return match;
                }

                return match.substring(i);
            }
        }

        return null;
    }

    /**
     * 获取原地址
     * @param url
     * @return
     */
    private Result<String> getRedirectUrl(String url, List<String> retryDomains) {

//        log.info("jd 获取重定向地址 {}", url);
        if (StringUtils.isBlank(url)) {
            return null;
        }
        List<String> logInfos = new ArrayList<>();
        // 重试的url命中了，才继续执行
        if (CollectionUtils.isNotEmpty(retryDomains)) {
            boolean match = false;
            for (String retryDomain : retryDomains) {
                if (url.contains(retryDomain)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return null;
            }
        }

        String redirectUrl = null;
        if (needRedirect(url, retryDomains)) {
            logInfos.add("命中重定向规则：" + url);
            // 判断是否需要解密
            if (needDecodeUrl(url)) {
                logInfos.add("命中url解密规则：" + url);
                String html = OkHttpService.doGetHtmlWithProxy(url);
                if (StringUtils.isBlank(html)) {
                    log.error("获取原地址失败，url {}", url);
                    return Result.ERROR(20010, "通过代理获取html失败");
                }
//                String html = OkHttpService.doGetHtml(url);
                Map<String, String> data = new HashMap<>();
                data.put("url", html);
                if (StringUtils.isNotBlank(html)) {
                    if (html.contains("请在微信客户端打开链接")) {
                        logInfos.add("url " + "请在微信客户端打开链接");
                        // 直接失败
                        return Result.ERROR(10010, "请在微信客户端打开链接");
                    }
                }

                // 解密的结果默认就是重定向的结果
                String res = FlameHttpService.doPost("https://cat.zhizher.com/cat_url_decrypt", JsonUtils.toJSONString(data));
                // 判断是否是Url，如果不是，不处理
                Matcher matcher = jd.matcher(res);
                if (matcher.find()) {
                    url = res;
                    redirectUrl = res;
                } else {
                    // 判断是不是小程序的链接
                    String newUrl = processWeixinUrl(res);
                    // 不相同，说明转成功了
                    if (StringUtils.isNotBlank(newUrl) && !newUrl.equals(url)) {
                        url = newUrl;
                        redirectUrl = newUrl;
                    }
                }
                logInfos.add("解密入参：" + JsonUtils.toJSONString(data));
                logInfos.add("解密结果为：" + res);
                if ("0".equals(res)) {
                    log.error("获取原地址结果不符合预期 url {}, res {}", url, JsonUtils.toJSONString(logInfos));
                }
            }

            HttpUrl httpUrl = OkHttpService.doGetHttpUrlWithProxy(url);
            if (httpUrl != null) {
                String realUrl = httpUrl.toString();
                if (StringUtils.isNotBlank(realUrl) && !realUrl.equals(url)) {
                    redirectUrl = realUrl;
                }
            }
        }
        Result<String> result = Result.OK(redirectUrl);
        if (CollectionUtils.isNotEmpty(logInfos)) {
            result.setMessage(JsonUtils.toJSONString(logInfos));
        }
        return result;
    }

    private String processWeixinUrl(String url) {
        // weixin://dl/business/?appid=wx91d27dbf599dff74&path=pages/union/proxy/proxy&query=spreadUrl=https%3A%2F%2Fu.jd.com%2FqaLkcfv
        if (StringUtils.isBlank(url) || !url.startsWith("weixin://")) {
            return url;
        }

        Map<String, String> parameters = UrlUtils.getWeixinParameters(url);
        String query = parameters.get("query");
        if (StringUtils.isBlank(query)) {
            return url;
        }

        Matcher matcher = jd.matcher(query);
        if (matcher.find()) {
            return matcher.group();
        }

        return url;
    }


    private boolean needRedirect(String uri, List<String> retryUrls) {
        if (StringUtils.isBlank(uri)) {
            return false;
        }
        // 不需要重定向的域名
        List<String> list = new ArrayList<>();
        list.add("item.jd.com");
        list.add("u.jd.com");
        list.add("coupon.m.jd.com");
        list.add("3.cn");
        list.add("pro.m.jd.com");
        list.add("prodev.m.jd.com");
        list.add("coupon.jd.com");
        String host = null;
        try {
            URL url = new URL(uri);
            host = url.getHost();
            // 重试后的白名单
            if (CollectionUtils.isNotEmpty(retryUrls) && retryUrls.contains(host)) {
                return true;
            }

            return !list.contains(host);
        } catch (Exception e) {
            log.error("needRedirect 异常， uri:{}", uri, e);
        }

        return false;
    }

    private boolean needDecodeUrl(String uri) {
        if (StringUtils.isBlank(uri)) {
            return false;
        }
        // 不需要重定向的域名
        List<String> list = new ArrayList<>();
        list.add("t.q5url.cn");
        list.add("y.q5url.cn");
//        list.add("jd.q5url.cn");
        list.add("cyg888.cn");
        list.add("i.kun7.cn");
        list.add("kurl04.cn");
        list.add("m.cute-cat.cn");
        list.add("4kma.cn");
        list.add("i.kunq5.cn");
        list.add("qianwum.cn");


        list.add("y-03.cn");

        String host = null;
        try {
            URL url = new URL(uri);
            host = url.getHost();

            return list.contains(host);
        } catch (Exception e) {
            log.error("needRedirect 异常， uri:{}", uri, e);
        }

        return false;
    }

    private CommandDTO.Product buildJdProduct(JdUnionIdPromotion promotion) {
        if (promotion == null || StringUtils.isBlank(promotion.getSkuName())) {
            return null;
        }
        CommandDTO.Product product = new CommandDTO.Product();

        product.setItemUrl(promotion.getShortURL());
        product.setCommand(null);
        // todo coupon
        CommandDTO.Item item = new CommandDTO.Item();

        item.setTitle(promotion.getSkuName());

        long reservePrice = 0L;
        long originalPrice = 0L;
        if (promotion.getPriceInfo() != null) {
            originalPrice = BigDecimal.valueOf(promotion.getPriceInfo().getPrice()).multiply(new BigDecimal(100)).longValue();
            reservePrice = BigDecimal.valueOf(promotion.getPriceInfo().getLowestPrice()).multiply(new BigDecimal(100)).longValue();
        }
        item.setReservePrice(reservePrice);
        item.setOriginalPrice(originalPrice);

        long commissionRate = 0L;
        long commission = 0L;
        if (promotion.getCommissionInfo() != null) {
            commissionRate = BigDecimal.valueOf(promotion.getCommissionInfo().getCommissionShare()).multiply(new BigDecimal(100)).longValue();
            commission = BigDecimal.valueOf(promotion.getCommissionInfo().getCommission()).multiply(new BigDecimal(100)).longValue();
        }
        item.setCommissionRate(commissionRate);
        item.setCommission(commission);

        if (promotion.getImageInfo() != null && CollectionUtils.isNotEmpty(promotion.getImageInfo().getImageList())) {
            item.setImage(promotion.getImageInfo().getImageList().get(0).getUrl());
        }

        if (promotion.getShopInfo() != null) {
            item.setShopTitle(promotion.getShopInfo().getShopName());
        }
        item.setId(null);
        product.setItem(item);

        return product;
    }


    public static void main(String[] args) {
//        String content = "weixin://dl/business/?appid=wx91d27dbf599dff74&path=pages/union/proxy/proxy&query=spreadUrl=https%3A%2F%2Fu.jd.com%2FqaLkcfv";
//        Map<String, String> parameters = UrlUtils.getWeixinParameters(content);

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <title>loading...</title>\n" +
                "  <meta http-equiv=\"refresh\" content=\"0.1;url=weixin://dl/business/?appid=wx91d27dbf599dff74&path=pages/union/proxy/proxy&query=spreadUrl%3Dhttps%3A%2F%2Fu.jd.com%2F16pIi5P\">\n" +
                "</head>\n" +
                "<body>\n" +
                "</body>";

        if (html.contains("weixin://dl") && html.contains("spreadUrl")) {


        }


        System.out.println(1);
    }
}
