package com.jeesite.modules.cgcat.dto.task;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.helper.PriceHelper;
import com.jeesite.modules.cat.helper.ProductValueHelper;
import com.jeesite.modules.cat.service.cg.task.dto.NameDetail;
import com.jeesite.modules.cat.service.cg.task.dto.ProductDetail;
import com.jeesite.modules.cat.service.cg.task.dto.TaskDetail;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponse;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
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

import static com.jeesite.modules.cat.service.toolbox.CommandService.jd;

@Component
public class TaskDetailHelper {

    @Resource
    private TbApiService tbApiService;

    @Resource
    private DingDanXiaApiService dingDanXiaApiService;

    public TaskDetail convertTb(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("detail", 2);
        objectMap.put("deepcoupon", 1);
        // 口令解析
        Result<CommandResponseV2> result = tbApiService.getCommonCommand(content, objectMap);
        if (!Result.isOK(result)) {
            return null;
        }

        CommandResponseV2 product = result.getResult();
        CommandResponseV2.ItemBasicInfo itemBasicInfo = product.getItemBasicInfo();
        CommandResponseV2.PricePromotionInfo pricePromotionInfo = product.getPricePromotionInfo();

        // todo short title需要加索引
        String title = itemBasicInfo.getShortTitle();
        if (StringUtils.isBlank(title)) {
            title = itemBasicInfo.getTitle();
        }

        JSONObject productObject = JsonUtils.toJsonObject(JsonUtils.toJSONString(itemBasicInfo));

        TaskDetail detail = new TaskDetail();
        ProductDetail productDetail = new ProductDetail();
        productDetail.setResourceId(product.getNumIid());
        productDetail.setResourceType("tb");
        // todo yhq
        productDetail.setUniqueId("");
        productDetail.setPrice(PriceHelper.formatPrice(pricePromotionInfo.getReservePrice()));
        productDetail.setPayPrice(ProductValueHelper.calVeApiPromotionPrice(productObject));
        productDetail.setTitle(title);
        productDetail.setCategoryName(itemBasicInfo.getCategoryName());
        productDetail.setCommand(product.getTbkPwd());

        String pictUrl = itemBasicInfo.getPictUrl();
        productDetail.setImgs(Collections.singletonList(pictUrl));

        productDetail.setNum(1);
//        productDetail.setDiscountPrice(productDetail.getPayPrice());
        List<NameDetail> coupons = new ArrayList<>();
        // 优惠券是否可用
        if (StringUtils.isNotBlank(product.getCouponInfo())) {
            NameDetail couponDetail = new NameDetail();
            couponDetail.setName(product.getCouponInfo());
            couponDetail.setContent(product.getCouponShortUrl());

            coupons.add(couponDetail);
        }

        productDetail.setCoupons(coupons);

        detail.setDesc(content);
        detail.setProducts(Collections.singletonList(productDetail));
        return detail;
    }


    public TaskDetail convertJd(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

//        AbstraOceanStage.MatchContent matchContent = AbstraOceanStage.calMatchContent(CommandService.tb, messageSyncDO.getMsg());
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
            return null;
        }

        TaskDetail detail = new TaskDetail();
        List<ProductDetail> products = new ArrayList<>();
        List<NameDetail> actLinks = new ArrayList<>();
        for (String url : urls) {
            Result<JdUnionIdPromotion> result = dingDanXiaApiService.jdByUnionidPromotion("FHPOsYO7zki7tcrxp0amyGMP7wxVkbU3", url, 1002248572L, 3100684498L);
            if (Result.isOK(result)) {
                JdUnionIdPromotion promotion = result.getResult();
                if (promotion == null) {
                    continue;
                }
                // 判断是链接还是商品
                if (promotion.getSkuId() == null || promotion.getSkuId() <= 0) {
                    NameDetail actLink = new NameDetail();
                    actLink.setName("活动券");
                    actLink.setContent(promotion.getShortURL());
                    actLinks.add(actLink);
                    continue;
                }

                long reservePrice = 0L;
                long originalPrice = 0L;
                if (promotion.getPriceInfo() != null) {
                    originalPrice = BigDecimal.valueOf(promotion.getPriceInfo().getPrice()).multiply(new BigDecimal(100)).longValue();
                    reservePrice = BigDecimal.valueOf(promotion.getPriceInfo().getLowestPrice()).multiply(new BigDecimal(100)).longValue();
                }

                String imgUrl = "";
                if (promotion.getImageInfo() != null && CollectionUtils.isNotEmpty(promotion.getImageInfo().getImageList())) {
                    imgUrl = promotion.getImageInfo().getImageList().get(0).getUrl();
                }

                ProductDetail productDetail = new ProductDetail();
                productDetail.setPrice(originalPrice);
                productDetail.setPayPrice(reservePrice);
                productDetail.setResourceId(String.valueOf(promotion.getSkuId()));
                productDetail.setResourceType("jd");
                productDetail.setUniqueId(String.valueOf(promotion.getSkuId()));
                productDetail.setImgs(Collections.singletonList(imgUrl));
                productDetail.setTitle(promotion.getSkuName());
                productDetail.setCommand(promotion.getShortURL());
                productDetail.setCategoryName("京东");

                productDetail.setNum(1);
//                productDetail.setDiscountPrice(productDetail.getPayPrice());
                List<NameDetail> coupons = new ArrayList<>();
                // 优惠券是否可用
                if (CollectionUtils.isNotEmpty(promotion.getCouponInfo())) {

                    for (JdUnionIdPromotion.CouponInfo couponInfo : promotion.getCouponInfo()) {
                        String subTitle = "";
                        if (StringUtils.isNotBlank(couponInfo.getPlatform())) {
                            subTitle += " " + couponInfo.getPlatform();
                        }

                        NameDetail couponDetail = new NameDetail();
                        couponDetail.setName(subTitle + "满" + couponInfo.getQuota() + "元减" + couponInfo.getDiscount() + "元");
                        couponDetail.setContent(couponInfo.getLink());
                        coupons.add(couponDetail);
                    }
                }
                productDetail.setCoupons(coupons);
                products.add(productDetail);
            }
        }

        detail.setDesc(content);
        detail.setProducts(products);
        detail.setActCoupons(actLinks);

        return detail;
    }
}
