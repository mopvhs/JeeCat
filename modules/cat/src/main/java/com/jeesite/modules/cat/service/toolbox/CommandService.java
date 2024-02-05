package com.jeesite.modules.cat.service.toolbox;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.helper.ProductValueHelper;
import com.jeesite.modules.cat.service.CsOpLogService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponse;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private DingDingService dingDingService;

    @Resource
    private CsOpLogService csOpLogService;

    @Resource
    private CacheService cacheService;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

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

        if ("jd".equals(type)) {
            return doExchangeJd(content);

        }

        return Result.OK(null);
    }

    private Result<CommandDTO> doExchangeTb(String content) {
        if (StringUtils.isBlank(content)) {
            return Result.ERROR(500, "参数不能为空");
        }

        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("detail", 1);
        objectMap.put("deepcoupon", 1);

        Result<CommandResponse> response = tbApiService.getCommonCommand(content, objectMap);

        // 日志记录
        csOpLogService.addLog("tb", "doExchangeTb", "tb_command", "maoche", "tb转链",
                content, JsonUtils.toJSONString(response));

        CommandDTO commandDTO = new CommandDTO();

        if (Result.isOK(response)) {
            CommandResponse data = response.getResult();
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
            item.setImage(data.getPictUrl());

            // 券后价
            long promotionPrice = ProductValueHelper.calVeApiPromotionPrice(JSONObject.parseObject(JsonUtils.toJSONString(data)));
            item.setReservePrice(promotionPrice);

            item.setOriginalPrice(new BigDecimal(data.getReservePrice()).multiply(new BigDecimal(100)).longValue());
            item.setShopDsr(NumberUtils.toLong(data.getShopDsr()));
            item.setVolume(NumberUtils.toLong(data.getVolume()));
            item.setNumIid(data.getNumIid());
            item.setTitle(data.getTitle());
            item.setCommissionRate(new BigDecimal(data.getCommissionRate()).multiply(new BigDecimal(100)).longValue());
            item.setShopTitle(data.getShopTitle());

            String numIid = data.getNumIid();
            // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
            String[] idArr = StringUtils.split(numIid, "-");
            String itemId = idArr[1];
            MaocheAlimamaUnionProductDO unionProductDO = maocheAlimamaUnionProductService.getProduct(itemId, "NORMAL");
            if (unionProductDO != null) {
                Long uiid = unionProductDO.getUiid();
                item.setId(uiid);
            }
            product.setItem(item);
            products.add(product);

            commandDTO.setProducts(products);
            return Result.OK(commandDTO);
        }

        String msgFormat = "{} \n 转链结果：{}";
        dingDingService.sendParseDingDingMsg(msgFormat, 1, content, JSONUtil.toJsonStr(response));

        return Result.ERROR(response.getCode(), response.getMessage());
    }

    private Result<CommandDTO> doExchangeJd(String content) {
        if (StringUtils.isBlank(content)) {
            return Result.ERROR(500, "参数不能为空");
        }

        Map<String, String> urlMap = new HashMap<>();
        List<String> urls = new ArrayList<>();
        String[] split = StringUtils.split(content, "\n");
        for (String item : split) {
            Matcher matcher = jd.matcher(item);
            if (matcher.find()) {

                String group = matcher.group();
                urlMap.put(group, "");
                urls.add(group);
            }
        }
        if (MapUtils.isEmpty(urlMap)) {
            return Result.ERROR(500, "分析需要替换的链接失败");
        }

        boolean match = false;
        StringBuilder errorMsg = new StringBuilder();
        Map<String, String> commandMap = new HashMap<>();
        List<CommandDTO.Product> products = new ArrayList<>();
        for (String url : urls) {
            Result<JdUnionIdPromotion> result = dingDanXiaApiService.jdByUnionidPromotionWithCoupon("FHPOsYO7zki7tcrxp0amyGMP7wxVkbU3", url, 1002248572L, 3100684498L);
            commandMap.put(url, JsonUtils.toJSONString(result));
            if (Result.isOK(result)) {
                JdUnionIdPromotion promotion = result.getResult();
                content = content.replace(url, promotion.getShortURL());

                match = true;
                // 构建京东的商品
                CommandDTO.Product product = buildJdProduct(promotion);
                if (product == null) {
                    continue;
                }
                products.add(product);
            } else {
                errorMsg.append(result.getMessage()).append("\n");
            }
        }

        // 日志记录
        csOpLogService.addLog("jd", "doExchangeJd", "jd_command", "maoche", "jd转链",
                content, JsonUtils.toJSONString(commandMap));

        if (!match) {
            String msgFormat = "{} \n 转链结果：{}";
            dingDingService.sendParseDingDingMsg(msgFormat, 1, content, errorMsg.toString());
            return Result.ERROR(500, errorMsg.toString());
        }

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setContent(content);
        commandDTO.setProducts(products);
        return Result.OK(commandDTO);
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
