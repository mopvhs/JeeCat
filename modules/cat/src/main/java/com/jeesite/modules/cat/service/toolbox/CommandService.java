package com.jeesite.modules.cat.service.toolbox;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheProductV2DO;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.helper.ProductValueHelper;
import com.jeesite.modules.cat.service.CsOpLogService;
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
import com.jeesite.modules.cat.service.message.DingDingService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandContext;
import com.jeesite.modules.cat.service.toolbox.dto.CommandDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    public static Pattern tb = Pattern.compile("\\((.*?)\\)\\/|\\/(.*?)\\/\\/");

    // 京东
    public static Pattern jd = Pattern.compile("(http|https):\\/\\/[a-zA-Z0-9-\\.]+\\.[a-z]{2,}(\\/\\S*)");

    public Result<CommandDTO> exchangeCommand(String content, String type) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(type)) {
            return Result.ERROR(500, "参数不能为空");
        }

        // 淘宝
        if ("tb".equals(type)) {
            return doExchangeTb(content);
        }

        if ("jd".equals(type) || "dwz".equals(type)) {
            CommandContext context = new CommandContext();
            context.setContent(content);
            return doDwz(context);
        }

        return Result.OK(null);
    }

    public Result<CommandDTO> doDwz(CommandContext context) {

        Result<CommandDTO> exchangeJd = doExchangeJd(context);
        if (exchangeJd == null) {
            return exchangeJd;
        }
        CommandDTO data = exchangeJd.getResult();

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

            String oriUrl = detail.getSupportDwzUrl();
            // 获取短地址md5
            String shortUrl = getDwzByUrl(oriUrl);
            if (StringUtils.isBlank(shortUrl)) {
                continue;
            }

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

    private Result<CommandDTO> doExchangeTb(String content) {
        if (StringUtils.isBlank(content)) {
            return Result.ERROR(500, "参数不能为空");
        }

        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("detail", 2);
        objectMap.put("deepcoupon", 2);

        String redisKey = Md5Utils.md5(content) + "_v2";
        String redisValue = cacheService.get(redisKey);
        if (StringUtils.isNotBlank(redisValue)) {
            return JsonUtils.toReferenceType(redisValue, new TypeReference<Result<CommandDTO>>() {
            });
        }

        Result<CommandResponseV2> response = tbApiService.getCommonCommand(content, objectMap);
        // 日志记录
//        csOpLogService.addLog("tb", "doExchangeTb", "tb_command", "maoche", "tb转链",
//                content, JsonUtils.toJSONString(response));
        CommandDTO commandDTO = new CommandDTO();
        if (Result.isOK(response)) {
            CommandResponseV2 data = response.getResult();
            CommandResponseV2.ItemBasicInfo itemBasicInfo = data.getItemBasicInfo();
            CommandResponseV2.PricePromotionInfo pricePromotionInfo = data.getPricePromotionInfo();
            String replaceAll = tb.matcher(content).replaceAll(data.getTbkPwd());

            commandDTO.setContent(replaceAll);
            List<CommandDTO.Product> products = new ArrayList<>();
            CommandDTO.Product product = new CommandDTO.Product();

            product.setCommand(data.getTbkPwd());
            if (StringUtils.isNotBlank(data.getCouponShortUrl())) {
                product.setCouponUrls(Collections.singletonList(data.getCouponShortUrl()));
                product.setCouponUrl(data.getCouponShortUrl());
            }
            String itemUrlFormat = "https://uland.taobao.com/item/edetail?id=%s";
            product.setItemUrl(String.format(itemUrlFormat, data.getNumIid()));

            CommandDTO.Item item = new CommandDTO.Item();
            item.setImage(itemBasicInfo.getPictUrl());

            // 券后价
            long promotionPrice = ProductValueHelper.calVeApiPromotionPrice(JSONObject.parseObject(JsonUtils.toJSONString(data)));
            item.setReservePrice(promotionPrice);

            item.setOriginalPrice(new BigDecimal(pricePromotionInfo.getReservePrice()).multiply(new BigDecimal(100)).longValue());
//            item.setShopDsr(NumberUtils.toLong(data.getShopDsr()));
            // 接口升级为detail=2之后，shopDsr字段被移除
            item.setShopDsr(0L);
            item.setVolume(NumberUtils.toLong(itemBasicInfo.getVolume()));
            item.setNumIid(data.getNumIid());
            item.setTitle(itemBasicInfo.getTitle());
            item.setCommissionRate(new BigDecimal(data.getCommissionRate()).multiply(new BigDecimal(100)).longValue());
            item.setShopTitle(itemBasicInfo.getShopTitle());

            String numIid = data.getNumIid();
            // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
            String[] idArr = StringUtils.split(numIid, "-");
            String itemId = idArr[1];
            MaocheProductV2DO unionProductDO = maocheProductV2Service.getProduct(itemId, "NORMAL");
            if (unionProductDO != null) {
                Long uiid = unionProductDO.getProductId();
                item.setId(uiid);
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
            return Result.ERROR(500, "分析需要替换的链接失败");
        }

        boolean match = false;
        StringBuilder errorMsg = new StringBuilder();
        Map<String, String> commandMap = new HashMap<>();
        List<CommandDTO.Product> products = new ArrayList<>();
        for (Map.Entry<String, ShortUrlDetail> entry : shortUrlDetailMap.entrySet()) {
            String url = entry.getKey();
            ShortUrlDetail detail = entry.getValue();

            // 获取原地址
            String redirectUrl = getRedirectUrl(url);
            String searchUrl = Optional.ofNullable(redirectUrl).orElse(url);
            detail.setSearchUrl(searchUrl);

            Result<JdUnionIdPromotion> result = dingDanXiaApiService.jdByUnionidPromotionWithCoupon("FHPOsYO7zki7tcrxp0amyGMP7wxVkbU3", searchUrl, 1002248572L, 3100684498L);
            commandMap.put(url, JsonUtils.toJSONString(result));
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
                if (StringUtils.isNotBlank(redirectUrl)) {
                    content = content.replace(url, redirectUrl);
                    detail.setReplaceUrl(redirectUrl);
                    detail.addExchangeLog(redirectUrl);
                }
                errorMsg.append(url).append("\n");
            }
        }

        String resourceId = Optional.ofNullable(context.getRelationId()).orElse("jd");
        // 日志记录
        csOpLogService.addLog(resourceId, "doExchangeJd", "jd_command", "maoche", "jd转链",
                content, JsonUtils.toJSONString(commandMap));

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

    /**
     * 获取原地址
     * @param url
     * @return
     */
    private String getRedirectUrl(String url) {

        log.info("jd 获取重定向地址 {}", url);
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String redirectUrl = null;
        if (needRedirect(url)) {
            HttpUrl httpUrl = OkHttpService.doGetHttpUrl(url);
            if (httpUrl != null) {
                String realUrl = httpUrl.toString();
                if (StringUtils.isNotBlank(realUrl) && !realUrl.equals(url)) {
                    redirectUrl = realUrl;
                }
            }
        }

        return redirectUrl;
    }

    private boolean needRedirect(String uri) {
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

            return !list.contains(host);
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
        String content = "https://item.m.jd.com/product/10079453257810.html?gx=RnAowmBfaDHYzZgQsoF2WYOUcHsyAco&gxd=RnAoxm5ZPWGLn50cqYJwX3b0-0RovQg&ad_od=share&utm_source=androidapp&utm_medium=appshare&utm_campaign=t_335139774&utm_term=CopyURL";
        String url = "https://item.m.jd.com/product/10079453257810.html?gx=RnAowmBfaDHYzZgQsoF2WYOUcHsyAco&gxd=RnAoxm5ZPWGLn50cqYJwX3b0-0RovQg&ad_od=share&utm_source=androidapp&utm_medium=appshare&utm_campaign=t_335139774&utm_term=CopyURL";
//        content = content.replaceAll(url, "123");

        content = content.replace(url, "123");

        System.out.println(content);
    }
}
