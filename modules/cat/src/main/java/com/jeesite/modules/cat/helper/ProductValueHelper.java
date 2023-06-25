package com.jeesite.modules.cat.helper;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

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
}
