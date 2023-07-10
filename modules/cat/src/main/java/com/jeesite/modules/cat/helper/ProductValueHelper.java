package com.jeesite.modules.cat.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.model.RateDetailTO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class ProductValueHelper {

    /**
     * 获取商品标题
     * @param content
     * @return
     */
    public static String getTitle(JSONObject content) {
        if (content == null) {
            return "";
        }

        String title = content.getString("title");
        if (StringUtils.isBlank(title)) {
            title = content.getString("itemName");
        }

        return title;
    }

    /**
     * 30天销量
     * @param content
     * @return
     */
    public static long getVolume(JSONObject content) {
        if (content == null) {
            return 0;
        }

        Long volume = content.getLong("volume");
        if (volume == null) {
            volume = content.getLong("monthSellCount");
        }

        return volume;
    }

    /**
     * 获取优惠券面额
     * @param jsonObject
     * @return
     */
    public static long getCouponAmount(JSONObject jsonObject) {
        if (jsonObject == null) {
            return 0;
        }
        long coupon = new BigDecimal(Optional.ofNullable(jsonObject.getString("coupon")).orElse("0")).multiply(new BigDecimal("100")).longValue();
        long coupon_amount = new BigDecimal(Optional.ofNullable(jsonObject.getString("coupon_amount")).orElse("0")).multiply(new BigDecimal("100")).longValue();

        return coupon > 0 ? coupon : coupon_amount;
    }

    /**
     * 获取商品价格
     * @param jsonObject
     * @return
     */
    public static Long getReservePrice(JSONObject jsonObject) {
        Object obj = jsonObject.get("zk_final_price");

//        if (Objects.isNull(obj)) {
//            obj = jsonObject.get("price");
//        }

        return PriceHelper.formatPrice(obj);
    }

    /**
     * 获取商品价格
     * @param jsonObject
     * @return
     */
    public static long getZkFinalPrice(JSONObject jsonObject) {
        Object obj = jsonObject.get("zk_final_price");

        return PriceHelper.formatPrice(obj);
    }

    /**
     * 商品描述
     * @param jsonObject
     * @return
     */
    public static String getItemDescription(JSONObject jsonObject) {
        String item = jsonObject.getString("item_description");

        if (StringUtils.isBlank(item)) {
            item = jsonObject.getString("subTitle");
        }

        return Optional.ofNullable(item).orElse("");
    }

    /**
     * 佣金比例
     * @param jsonObject
     * @return
     */
    public static long getCommissionRate(JSONObject jsonObject) {
        String commissionRate = jsonObject.getString("commission_rate");

        if (StringUtils.isBlank(commissionRate)) {
            commissionRate = jsonObject.getString("subTitle");
        }

        return NumberUtils.toLong(commissionRate);
    }

    /**
     * 店铺dsr
     * @param jsonObject
     * @return
     */
    public static long getShopDsr(JSONObject jsonObject) {
        String shopDsr = jsonObject.getString("shop_dsr");

        if (StringUtils.isBlank(shopDsr)) {
            shopDsr = jsonObject.getString("dsr");
        }

        return NumberUtils.toLong(shopDsr);
    }

    /**
     * 店铺dsr
     * @param jsonObject
     * @return
     */
    public static String getShortTitle(JSONObject jsonObject) {
        String shortTitle = jsonObject.getString("short_title");

        return shortTitle;
    }

    /**
     * 类目名称
     * @param jsonObject
     * @return
     */
    public static String getCategoryName(JSONObject jsonObject) {
        String categoryName = jsonObject.getString("category_name");

        if (StringUtils.isBlank(categoryName)) {
            categoryName = jsonObject.getString("categoryName");
        }

        return Optional.ofNullable(categoryName).orElse("");
    }

    /**
     * 类目名称
     * @param jsonObject
     * @return
     */
    public static String getLevelOneCategoryName(JSONObject jsonObject) {
        String categoryName = jsonObject.getString("level_one_category_name");

        if (StringUtils.isBlank(categoryName)) {
            categoryName = jsonObject.getString("levelOneCategoryName");
        }

        return Optional.ofNullable(categoryName).orElse("");
    }

    /**
     * 淘客
     * @param jsonObject
     * @return
     */
    public static long geTkTotalSales(JSONObject jsonObject) {
        String value = jsonObject.getString("tk_total_sales");

        if (StringUtils.isBlank(value)) {
            value = jsonObject.getString("tkTotalSales");
        }

        return NumberUtils.toLong(value);
    }

    /**
     * 优惠券
     * @param jsonObject
     * @return
     */
    public static long couponRemainCount(JSONObject jsonObject) {
        String value = jsonObject.getString("coupon_remain_count");

        if (StringUtils.isBlank(value)) {
            value = jsonObject.getString("couponTotalCount");
        }

        return NumberUtils.toLong(value);
    }

    /**
     * 是否优惠券有效日期内
     * @param jsonObject
     * @return
     */
    public static boolean isCouponValidTime(JSONObject jsonObject) {
        String couponStartTime = jsonObject.getString("coupon_start_time");
        String couponEndTime = jsonObject.getString("coupon_end_time");
        if (StringUtils.isBlank(couponStartTime) || StringUtils.isBlank(couponEndTime)) {
            return false;
        }
        Date start = DateTimeUtils.getDate(couponStartTime);
        Date end = DateTimeUtils.getDate(couponEndTime);
        if (start == null || end == null) {
            return false;
        }
        long current = System.currentTimeMillis();

        return current > start.getTime() && current <= end.getTime();
    }

    public static long getCouponStartFee(JSONObject jsonObject) {

        if (jsonObject == null) {
            return 0;
        }
        long couponStartFee = new BigDecimal(Optional.ofNullable(jsonObject.getString("coupon_start_fee")).orElse("0")).multiply(new BigDecimal("100")).longValue();

        return couponStartFee;
    }

    /**
     * 获取维易券后价
     * @param jsonObject
     * @return
     */
    public static long calVeApiPromotionPrice(JSONObject jsonObject) {
        long zkFinalPrice = getZkFinalPrice(jsonObject);

        if (!isCouponValidTime(jsonObject)) {
            return zkFinalPrice;
        }

        long couponAmount = getCouponAmount(jsonObject);
        // 优惠券最低门槛
        long couponStartFee = getCouponStartFee(jsonObject);
        if (zkFinalPrice < couponStartFee) {
            return zkFinalPrice;
        }

        return zkFinalPrice - couponAmount;
    }

    /**
     * 获取详情基本信息品牌
     * @param jsonObject
     * @return
     */
    public static String getDetailPropsBrand(JSONObject jsonObject) {
        JSONArray baseInfos = getDetailBaseInfoProps(jsonObject);

        if (baseInfos == null || baseInfos.size() <= 0) {
            return null;
        }

        for (Object o : baseInfos) {
            if (o instanceof JSONObject item) {
                Object brandName = item.get("品牌");
                if (brandName instanceof String) {
                    return brandName.toString();
                }
            }
        }

        return null;
    }

    /**
     * 获取详情基本信息品名
     * @param jsonObject
     * @return
     */
    public static String getDetailPropsProductName(JSONObject jsonObject) {
        JSONArray baseInfos = getDetailBaseInfoProps(jsonObject);

        if (baseInfos == null || baseInfos.size() <= 0) {
            return null;
        }

        for (Object o : baseInfos) {
            if (o instanceof JSONObject item) {
                Object brandName = item.get("品名");
                if (brandName instanceof String) {
                    return brandName.toString();
                }
            }
        }

//        for (Object o : baseInfos) {
//            if (o instanceof JSONObject item) {
//                Object brandName = item.get("型号");
//                if (brandName instanceof String) {
//                    log.info("brandName is other : {}", brandName);
//                    return brandName.toString();
//                }
//            }
//        }

        return null;
    }

    public static List<RateDetailTO> getDetailRates(JSONObject jsonObject) {
        if (jsonObject == null || jsonObject.get("data") == null) {
            return new ArrayList<>();
        }

        List<RateDetailTO> details = new ArrayList<>();
        // 获取粉丝数和店铺等级
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject rate = data.getJSONObject("rate");
        if (rate == null) {
            return new ArrayList<>();
        }
        if (rate.get("keywords") != null && rate.get("keywords") instanceof JSONArray) {
            JSONArray keywords = rate.getJSONArray("keywords");
            for (int i = 0; i < keywords.size(); i++) {
                Object o = keywords.get(i);
                if (o instanceof JSONObject item) {
                    RateDetailTO rateDetailTO = JsonUtils.toReferenceType(item.toJSONString(), new TypeReference<RateDetailTO>() {
                    });
                    if (rateDetailTO == null) {
                        continue;
                    }
                    details.add(rateDetailTO);
                }
            }
        }

        return details;
    }

    private static JSONArray getDetailBaseInfoProps(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        Object data = jsonObject.get("data");
        if (!(data instanceof JSONObject obj)) {
            return null;
        }

        Object props = obj.get("props");
        if (!(props instanceof JSONObject propsObj)) {
            return null;
        }

        Object groupProps = propsObj.get("groupProps");
        if (!(groupProps instanceof JSONArray groupPropsObj)) {
            return null;
        }

        JSONArray baseInfos = null;
        for (int i = 0; i < groupPropsObj.size(); i++) {
            Object o = groupPropsObj.get(i);
            if (o instanceof JSONObject item) {
                if (item.get("基本信息") instanceof JSONArray array) {
                    baseInfos = array;
                    break;
                }
            }
        }

        return baseInfos;
    }

}
