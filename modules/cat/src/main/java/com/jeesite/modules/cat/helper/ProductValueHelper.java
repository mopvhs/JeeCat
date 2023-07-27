package com.jeesite.modules.cat.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.model.PriceChartInfoTO;
import com.jeesite.modules.cat.model.PriceChartSkuBaseTO;
import com.jeesite.modules.cat.model.RateDetailTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

    /**
     * maoche_alimama_union_product_price_chart.orig_content
     * @param skuBaseList
     */
    public static long getPriceChartMinPrice(List<PriceChartSkuBaseTO> skuBaseList) {

        if (CollectionUtils.isEmpty(skuBaseList)) {
            return 0;
        }

        return getPriceChartSkuBaseMinPrice(skuBaseList);
    }

    private static long getPriceChartSkuBaseMinPrice(List<PriceChartSkuBaseTO> priceChartSkuBase) {
        if (CollectionUtils.isEmpty(priceChartSkuBase)) {
            return 0L;
        }

        long price = 0l;
        for (PriceChartSkuBaseTO item : priceChartSkuBase) {
            if (item.getPrice() == null || item.getPrice() <= 0) {
                continue;
            }
            if (price == 0) {
                price = item.getPrice();
            } else if (price > item.getPrice()) {
                price = item.getPrice();
            }
        }

        return price;
    }

    /**
     * maoche_alimama_union_product_price_chart.orig_content
     * @param jsonObject
     */
    public static List<PriceChartSkuBaseTO> getPriceChartSkuBase(JSONObject jsonObject) {

        if (jsonObject == null || jsonObject.get("data") == null) {
            return new ArrayList<>();
        }

        JSONObject data = jsonObject.getJSONObject("data");
        if (data.get("sku") == null) {
            return new ArrayList<>();
        }
        JSONObject sku = data.getJSONObject("sku");

        if (sku.get("skuBase") == null) {
            return new ArrayList<>();
        }

        JSONObject skuBase = sku.getJSONObject("skuBase");
        Set<String> keySet = skuBase.keySet();

        List<PriceChartSkuBaseTO> skuBaseTOs = new ArrayList<>();
        for (String key : keySet) {
            JSONObject object = skuBase.getJSONObject(key);
            if (object == null) {
                continue;
            }
            PriceChartSkuBaseTO skuBaseTO = new PriceChartSkuBaseTO();
            String quantity = Optional.ofNullable(object.getString("quantity")).orElse("0");
            String skuProperty = Optional.ofNullable(object.getString("skuProperty")).orElse("");
            String compareDesc = object.getString("compareDesc");
            long price = PriceHelper.formatPrice(object.getString("price"));
            long skuId = object.getLong("skuId");

            skuBaseTO.setKey(key);
            skuBaseTO.setQuantity(quantity);
            skuBaseTO.setSkuProperty(skuProperty);
            skuBaseTO.setPrice(price);
            skuBaseTO.setSkuId(skuId);
            skuBaseTO.setCompareDesc(compareDesc);
            skuBaseTO.setCompareDescKeyword(compareDesc);

            skuBaseTOs.add(skuBaseTO);
        }


        return skuBaseTOs;
    }

    /**
     * maoche_alimama_union_product_price_chart.orig_content
     * @param jsonObject
     */
    public static List<PriceChartInfoTO> getPriceChartInfo(JSONObject jsonObject) {

        if (jsonObject == null || jsonObject.get("data") == null) {
            return new ArrayList<>();
        }

        JSONObject data = jsonObject.getJSONObject("data");
        if (data.get("chartInfo") == null || data.get("chartInfo") instanceof JSONArray) {
            return new ArrayList<>();
        }

        JSONObject chartInfo = data.getJSONObject("chartInfo");
        Set<String> keySet = chartInfo.keySet();

        List<PriceChartInfoTO> list = new ArrayList<>();
        for (String key : keySet) {
            JSONObject object = chartInfo.getJSONObject(key);
            if (object == null || object.get("compareInfoDesc") == null) {
                continue;
            }
            PriceChartInfoTO item = new PriceChartInfoTO();
            String compareInfoDesc = Optional.ofNullable(object.getString("compareInfoDesc")).orElse("");
            long price = PriceHelper.formatPrice(object.getString("price"));
            long skuId = NumberUtils.toLong(key);

            item.setCompareInfoDesc(compareInfoDesc);
            item.setPrice(price);
            item.setSkuId(skuId);

            list.add(item);
        }


        return list;
    }

    public static void main(String[] args) {
        String s = "{\"error\":\"0\",\"msg\":\"获取成功！\",\"data\":{\"itemId\":\"724870481619\",\"item\":{},\"sku\":{\"skuList\":[{\"propertyDetail\":[{\"valueId\":\"25872700355\",\"valueText\":\"【12罐】鸭肉牛蛙100g*12罐\",\"selected\":false},{\"valueId\":\"25744950874\",\"valueText\":\"【4罐】鸡肉乳鸽100g*4罐\",\"selected\":false},{\"valueId\":\"25744950873\",\"valueText\":\"【4罐】鲣鱼兔肉100g*4罐\",\"selected\":false},{\"valueId\":\"25872700356\",\"valueText\":\"【12罐】鳕鱼鹌鹑100g*12罐\",\"selected\":false},{\"valueId\":\"25872700357\",\"valueText\":\"【12罐】混合装100g*12罐\",\"selected\":false},{\"valueId\":\"25744950876\",\"valueText\":\"【4罐】鳕鱼鹌鹑100g*4罐\",\"selected\":false},{\"valueId\":\"25744950875\",\"valueText\":\"【4罐】鸭肉牛蛙100g*4罐\",\"selected\":false},{\"valueId\":\"25979224151\",\"valueText\":\"【4罐】混合装100g*4罐（散装随机发\",\"selected\":true},{\"valueId\":\"25872700358\",\"valueText\":\"【12罐】鲣鱼兔肉 100g*12罐\",\"selected\":false}],\"propertyText\":\"食品口味\",\"propertyId\":\"122216494\"},{\"propertyDetail\":[{\"valueId\":\"75366083\",\"valueText\":\"100g\",\"selected\":true}],\"propertyText\":\"净含量\",\"propertyId\":\"147956252\"}],\"skuBase\":{\"122216494:25979224151;147956252:75366083\":{\"quantity\":\"0\",\"price\":\"34.86\",\"skuId\":5221581998307,\"skuProperty\":\"【4罐】混合装100g*4罐（散装随机发 + 100g\"},\"122216494:25744950874;147956252:75366083\":{\"quantity\":\"0\",\"price\":\"34.86\",\"skuId\":5221581998300,\"skuImage\":\"//img.alicdn.com/i2/2019505434/O1CN016K1AxB1q0p4p8KwTJ_!!2019505434.jpg\",\"skuProperty\":\"【4罐】鸡肉乳鸽100g*4罐 + 100g\"},\"122216494:25744950873;147956252:75366083\":{\"quantity\":\"6\",\"price\":\"34.86\",\"skuId\":5221581998299,\"skuImage\":\"//img.alicdn.com/i1/2019505434/O1CN01DIoNKG1q0p4rMqeHK_!!2019505434.jpg\",\"skuProperty\":\"【4罐】鲣鱼兔肉100g*4罐 + 100g\"},\"122216494:25872700358;147956252:75366083\":{\"quantity\":\"4\",\"price\":\"99.63\",\"skuId\":5221581998306,\"skuImage\":\"//img.alicdn.com/i2/2019505434/O1CN01yhRYmC1q0p4sd7NfZ_!!2019505434.jpg\",\"skuProperty\":\"【12罐】鲣鱼兔肉 100g*12罐 + 100g\"},\"122216494:25872700357;147956252:75366083\":{\"quantity\":\"0\",\"price\":\"99.63\",\"skuId\":5221581998305,\"skuImage\":\"//img.alicdn.com/i1/2019505434/O1CN01Gi8tJR1q0p4qEOX2g_!!2019505434.jpg\",\"skuProperty\":\"【12罐】混合装100g*12罐 + 100g\"},\"122216494:25872700356;147956252:75366083\":{\"quantity\":\"6\",\"price\":\"99.63\",\"skuId\":5221581998304,\"skuImage\":\"//img.alicdn.com/i1/2019505434/O1CN010mkrU21q0p4x8D1PU_!!2019505434.jpg\",\"skuProperty\":\"【12罐】鳕鱼鹌鹑100g*12罐 + 100g\"},\"122216494:25872700355;147956252:75366083\":{\"quantity\":\"1\",\"price\":\"99.63\",\"skuId\":5221581998303,\"skuImage\":\"//img.alicdn.com/i4/2019505434/O1CN01uLSUYa1q0p4rGcCpG_!!2019505434.jpg\",\"skuProperty\":\"【12罐】鸭肉牛蛙100g*12罐 + 100g\"},\"122216494:25744950875;147956252:75366083\":{\"quantity\":\"3\",\"price\":\"34.86\",\"skuId\":5221581998301,\"skuImage\":\"//img.alicdn.com/i4/2019505434/O1CN01qXr3YJ1q0p4qtIHTH_!!2019505434.jpg\",\"skuProperty\":\"【4罐】鸭肉牛蛙100g*4罐 + 100g\"},\"122216494:25744950876;147956252:75366083\":{\"quantity\":\"14\",\"price\":\"34.86\",\"skuId\":5221581998302,\"skuImage\":\"//img.alicdn.com/i1/2019505434/O1CN019NxAOq1q0p4sR4JzR_!!2019505434.jpg\",\"skuProperty\":\"【4罐】鳕鱼鹌鹑100g*4罐 + 100g\"}}},\"chartInfo\":{\"5221581998304\":{\"chartInfo\":{\"chartData\":[{\"date\":\"2023-07-11\",\"price\":\"100\"},{\"date\":\"2023-07-13\",\"price\":\"100.01\"},{\"date\":\"2023-07-15\",\"price\":\"99.63\"}],\"chartTitle\":\"价格曲线\",\"minPrice\":100,\"chartType\":\"line\",\"maxPrice\":100}},\"5221581998307\":{\"chartInfo\":{\"chartData\":[{\"date\":\"2023-07-11\",\"price\":\"34.9\"},{\"date\":\"2023-07-14\",\"price\":\"34.9\"},{\"date\":\"2023-07-15\",\"price\":\"34.86\"}],\"chartTitle\":\"价格曲线\",\"minPrice\":34,\"chartType\":\"line\",\"maxPrice\":34}},\"5221581998302\":{\"chartInfo\":{\"chartData\":[{\"date\":\"2023-07-12\",\"price\":\"34.9\"},{\"date\":\"2023-07-14\",\"price\":\"39.26\"},{\"date\":\"2023-07-15\",\"price\":\"34.86\"}],\"chartTitle\":\"价格曲线\",\"minPrice\":34,\"chartType\":\"line\",\"maxPrice\":39}},\"5221581998299\":{\"chartInfo\":{\"chartData\":[{\"date\":\"2023-07-12\",\"price\":\"39.26\"},{\"date\":\"2023-07-13\",\"price\":\"34.9\"},{\"date\":\"2023-07-14\",\"price\":\"34.9\"},{\"date\":\"2023-07-15\",\"price\":\"34.86\"}],\"chartTitle\":\"价格曲线\",\"minPrice\":34,\"chartType\":\"line\",\"maxPrice\":39}}}},\"request_id\":\"w4tMBdl\"}\n";

        JSONObject jsonObject = JsonUtils.toJsonObject(s);
        List<PriceChartSkuBaseTO> priceChartSkuBase = getPriceChartSkuBase(jsonObject);

        System.out.println(JsonUtils.toJSONString(priceChartSkuBase));
    }

}
