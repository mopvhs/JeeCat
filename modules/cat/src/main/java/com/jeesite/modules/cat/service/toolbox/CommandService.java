package com.jeesite.modules.cat.service.toolbox;

import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponse;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
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
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    // 淘宝
    private static Pattern tb = Pattern.compile("\\((.*?)\\)\\/|\\/(.*?)\\/\\/");

    // 京东
    private static Pattern jd = Pattern.compile("(http|https):\\/\\/[a-zA-Z0-9-\\.]+\\.[a-z]{2,}(\\/\\S*)");


    public Result<CommandDTO> exchangeCommand(String content, String type) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(type)) {
            return Result.ERROR(500, "参数不能为空");
        }

        // 淘宝
        if ("tb".equals(type)) {
            return doExchangeTb(content);
        }

        if ("jd".equals(type)) {
//            content = "✨有豪车✨\n" +
//                    "以下都是预售，31日20点付尾款\n" +
//                    "阿飞和巴弟三文鱼主食罐85g*36\n" +
//                    "定金30，尾款234.9，共\uD83D\uDCB0264.9\n" +
//                    "https://u.jd.com/0QzgPaa\n" +
//                    "鸡肉主食罐85g*36\n" +
//                    "https://u.jd.com/0Qzg6hM\n" +
//                    "混合装85g*36罐\n" +
//                    "https://u.jd.com/0zzgqCP\n" +
//                    "---------------------\n" +
//                    "阿飞和巴弟猫条48支*2桶\n" +
//                    "定金20，尾款154.9，共\uD83D\uDCB0174.9\n" +
//                    "https://u.jd.com/0uzgqM3\n" +
//                    "阿飞和巴弟纯条120支袋装\n" +
//                    "定金30，尾款180.9，共\uD83D\uDCB0210.9\n" +
//                    "https://u.jd.com/0Qzg8rg\n" +
//                    "---------------------\n" +
//                    "阿飞和巴弟E76幼猫粮2kg*4袋\n" +
//                    "定金40，尾款249.9，共\uD83D\uDCB0289.9\n" +
//                    "折72.4/包\n" +
//                    "https://u.jd.com/0izgaub\n" +
//                    "E76幼猫粮2kg+湿粮*9袋\n" +
//                    "定金20，尾款145.9，共\uD83D\uDCB0165.9\n" +
//                    "https://u.jd.com/0uzg6zB\n" +
//                    "---------------------\n" +
//                    "自助查车@猫车选品官 +产品名";

//            return doAnalysisJd(content);
            return doExchangeJd(content);

        }

        return Result.OK(null);
    }

    public Result<?> analysisCommand(String content, String type) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(type)) {
            return Result.ERROR(500, "参数不能为空");
        }

        // 淘宝
        if ("tb".equals(type)) {
            return doAnalysisCommandTb(content);
        }

//        if ("jd".equals(type)) {
//            return doExchangeJd(content);
//        }

        return Result.OK("OK");
    }



    private Result<String> doAnalysisCommandTb(String content) {
        if (StringUtils.isBlank(content)) {
            return Result.ERROR(500, "参数不能为空");
        }

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("detail", 1);
        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Result<?> result = tbApiService.getCommonCommand(content, objectMap);

        if (Result.isOK(result)) {
            return Result.OK((String) result.getResult());
        }

        return Result.ERROR(500, result.getMessage());
    }

    private Result<CommandDTO> doExchangeTb(String content) {
        if (StringUtils.isBlank(content)) {
            return Result.ERROR(500, "参数不能为空");
        }

        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("detail", 1);
        objectMap.put("deepcoupon", 1);
        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        Result<CommandResponse> response = tbApiService.getCommonCommand(content, objectMap);

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
            product.setItemUrl(data.getItemUrl());

            CommandDTO.Item item = new CommandDTO.Item();
            item.setReservePrice(new BigDecimal(data.getZkFinalPrice()).multiply(new BigDecimal(100)).longValue());
            item.setOriginalPrice(new BigDecimal(data.getReservePrice()).multiply(new BigDecimal(100)).longValue());
            item.setShopDsr(NumberUtils.toLong(data.getShopDsr()));
            item.setVolume(NumberUtils.toLong(data.getVolume()));
            item.setTitle(data.getTitle());
            item.setCommissionRate(new BigDecimal(data.getCommissionRate()).multiply(new BigDecimal(100)).longValue());
            item.setShopTitle(data.getShopTitle());

            String numIid = data.getNumIid();
            // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
            String[] idArr = StringUtils.split(numIid, "-");
            String itemId = idArr[1];
            List<MaocheAlimamaUnionProductDO> resources = maocheAlimamaUnionProductService.getByItemIdSuffix(itemId);
            if (CollectionUtils.isNotEmpty(products)) {
                MaocheAlimamaUnionProductDO unionProductDO = resources.get(0);
                Long uiid = unionProductDO.getUiid();
                item.setId(uiid);
            }
            product.setItem(item);
            products.add(product);

            commandDTO.setProducts(products);
            return Result.OK(commandDTO);
        }
        // 获取num_iid



        return Result.ERROR(response.getCode(), response.getMessage());
    }

    private Result<CommandDTO> doExchangeJd(String content) {
        if (StringUtils.isBlank(content)) {
            return Result.ERROR(500, "参数不能为空");
        }

        Map<String, String> urlMap = new HashMap<>();
        String[] split = StringUtils.split(content, "\n");
        for (String item : split) {
            Matcher matcher = jd.matcher(item);
            if (matcher.find()) {

                String group = matcher.group();
                urlMap.put(group, "");
            }
        }
        if (MapUtils.isEmpty(urlMap)) {
            return Result.ERROR(500, "分析需要替换的链接失败");
        }

        boolean match = false;
        StringBuilder errorMsg = new StringBuilder();

        List<CommandDTO.Product> products = new ArrayList<>();
        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
            String key = entry.getKey();
            Result<JdUnionIdPromotion> result = dingDanXiaApiService.jdByUnionidPromotion("FHPOsYO7zki7tcrxp0amyGMP7wxVkbU3", key, 1002248572L, 3100684498L);
            if (Result.isOK(result)) {
                JdUnionIdPromotion promotion = result.getResult();
                content = content.replaceAll(key, promotion.getShortURL());
                // 构建京东的商品
                CommandDTO.Product product = buildJdProduct(promotion);
                products.add(product);
                match = true;
            } else {
                errorMsg.append(result.getMessage()).append("\n");
            }
        }

        if (!match) {
            return Result.ERROR(500, errorMsg.toString());
        }

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setContent(content);
        commandDTO.setProducts(products);
        return Result.OK(commandDTO);
    }

    private CommandDTO.Product buildJdProduct(JdUnionIdPromotion promotion) {
        if (promotion == null) {
            return null;
        }
        CommandDTO.Product product = new CommandDTO.Product();

        product.setItemUrl(promotion.getShortURL());
        product.setCommand(null);
        // todo coupon
        CommandDTO.Item item = new CommandDTO.Item();

        item.setShopDsr(-1L);
        item.setVolume(-1L);
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
        if (promotion.getCommissionInfo() != null) {
            commissionRate = BigDecimal.valueOf(promotion.getCommissionInfo().getCommissionShare()).multiply(new BigDecimal(100)).longValue();
        }
        item.setCommissionRate(commissionRate);

        if (promotion.getShopInfo() != null) {
            item.setShopTitle(promotion.getShopInfo().getShopName());
        }
        item.setId(null);
        product.setItem(item);

        return product;
    }

    public static void main(String[] args) {
        String content = "✨有好价✨\n" +
                "K9海外旗舰店，领200-37\n" +
                "https://jd.cn.hn/aUGu\n" +
                "plus领200-10\n" +
                "https://u.jd.com/ysMPUng\n" +
                "k9鸡肉猫主食罐头170g\n" +
                "https://u.jd.com/PzN7wAE\n" +
                "k9牛肉鳕鱼猫主食罐头170g\n" +
                "https://u.jd.com/P8N7Lw1\n" +
                "k9羊心帝王鲑猫主食罐头170g\n" +
                "https://u.jd.com/PqN7bbI\n" +
                "任意加车6件 \uD83D\uDCB0114.2\n" +
                "plus\uD83D\uDCB0104.2，折\uD83D\uDCB017.3/罐\n" +
                "---------------------\n" +
                "自助查车@猫车选品官 +产品名";

        System.out.println("1");
    }
}
