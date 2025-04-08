package com.jeesite.modules.cat.service.cg.third.tb.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.utils.UrlUtils;
import lombok.Data;
import org.apache.commons.collections.MapUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jeesite.modules.cat.helper.ProductV2Helper.N_YUAN_REGEX;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralConvertResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 2837749568497654822L;
    // 自定义字段，非维易接口返回
    private String tbkPwd;

    @JsonProperty("input_material_url")
    private String inputMaterialUrl;
    @JsonProperty("activity_id")
    private String activityId;
    @JsonProperty("coupon_amount")
    private String couponAmount;
    //   "": "25.00",
    @JsonProperty("coupon_desc")
    private String couponDesc;
    @JsonProperty("coupon_end_time")
    private String couponEndTime;
    @JsonProperty("coupon_remain_count")
    private String couponRemainCount;
    @JsonProperty("coupon_start_time")
    private Long couponStartTime;

    @JsonProperty("coupon_full_tpwd")
    private String couponFullTpwd;

    @JsonProperty("coupon_long_url")
    private String couponLongUrl;
    @JsonProperty("coupon_short_tpwd")
    private String couponShortTpwd;
    @JsonProperty("coupon_short_url")
    private String couponShortUrl;


    @JsonProperty("cps_full_tpwd")
    private String cpsFullTpwd;
    @JsonProperty("cps_long_url")
    private String cpsLongUrl;
    @JsonProperty("cps_short_tpwd")
    private String cpsShortTpwd;
    @JsonProperty("cps_short_url")
    private String cpsShortUrl;
    @JsonProperty("isv_mktid")
    private String isvMktid;
    @JsonProperty("item_id")
    private String itemId;
    @JsonProperty("material_type")
    private String materialType;
    @JsonProperty("tk_biz_type")
    private String tkBizType;
    @JsonProperty("istaote")
    private String istaote;
    @JsonProperty("commission_rate")
    private String commissionRate;
    @JsonProperty("commission_type")
    private String commissionType;
    @JsonProperty("original_para_id")
    private String originalParaId;

    @JsonProperty("item_basic_info")
    private ItemBasicInfo itemBasicInfo;

    @JsonProperty("presale_info")
    private Object presaleInfo;

    @JsonProperty("price_promotion_info")
    private PricePromotionInfo pricePromotionInfo;
    @JsonProperty("scope_info")
    private Object scopeInfo;

    @JsonProperty("publish_info")
    private PublishInfo publishInfo;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemBasicInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = -9218492843071774842L;

        @JsonProperty("brand_name")
        private String brandName;

        @JsonProperty("category_id")
        private String categoryId;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("level_one_category_id")
        private String levelOneCategoryId;

        @JsonProperty("level_one_category_name")
        private String levelOneCategoryName;

        @JsonProperty("pict_url")
        private String pictUrl;

        @JsonProperty("provcity")
        private String provcity;

        @JsonProperty("seller_id")
        private String sellerId;

        @JsonProperty("shop_title")
        private String shopTitle;

        @JsonProperty("sub_title")
        private String subTitle;

        @JsonProperty("title")
        private String title;

        @JsonProperty("short_title")
        private String shortTitle;

        @JsonProperty("tk_total_sales")
        private String tkTotalSales;

        @JsonProperty("user_type")
        private String userType;

        @JsonProperty("volume")
        private String volume;

        // 年销量，不是实时变化。是T+1更新过去365天的数据，显示规则：0，展示“0”
        // (0,100]，展示精确值
        // (100,1000]，每层100递增，如展示100+、200+、900+
        @JsonProperty("annual_vol")
        private String annualVol;

        @JsonProperty("white_image")
        private String whiteImage;

        @JsonProperty("item_url")
        private String itemUrl;

//        @JsonProperty("small_images")
//        private String smallImages;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PricePromotionInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 6202328785269475277L;

        @JsonProperty("final_promotion_price")
        private String finalPromotionPrice;

        @JsonProperty("more_promotion_list")
        private Object morePromotionList;

        @JsonProperty("predict_rounding_up_price")
        private String predictRoundingUpPrice;

        @JsonProperty("predict_rounding_up_price_desc")
        private String predictRoundingUpPriceDesc;

        @JsonProperty("promotion_tag_list")
        private Object promotionTagList;

        @JsonProperty("reserve_price")
        private String reservePrice;

        @JsonProperty("zk_final_price")
        private String zkFinalPrice;

        @JsonProperty("final_promotion_path_list")
        private FinalPromotionPathList finalPromotionPathList;

        public List<String> getPromotionTagList() {
            if (promotionTagList == null) {
                return new ArrayList<>();
            }
            String json = JsonUtils.toJSONString(promotionTagList);
            JSONObject ptl = JSON.parseObject(json);
            List<String> pricePromotionTagList = new ArrayList<>();
            if (ptl != null) {
                Object ptmd = ptl.get("promotion_tag_map_data");
                if (ptmd != null) {
                    if (ptmd instanceof JSONObject ptmdObj) {
                        String tag = ptmdObj.getString("tag_name");
                        if (StringUtils.isNotBlank(tag)) {
                            pricePromotionTagList.add(tag);
                        }
                    } else if (ptmd instanceof JSONArray ptmdArray) {
                        for (int i = 0; i < ptmdArray.size(); i++) {
                            JSONObject tagObj = ptmdArray.getJSONObject(i);
                            if (tagObj == null || tagObj.get("tag_name") == null) {
                                continue;
                            }
                            String tag = tagObj.getString("tag_name");
                            if (StringUtils.isNotBlank(tag)) {
                                // 正则匹配n元，过滤掉
                                String replaceAll = N_YUAN_REGEX.matcher(tag).replaceAll("");
                                if (StringUtils.isBlank(replaceAll)) {
                                    continue;
                                }
                                pricePromotionTagList.add(tag);
                            }
                        }
                    }
                }
            }
            return pricePromotionTagList;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FinalPromotionPathList implements Serializable {

        @Serial
        private static final long serialVersionUID = -2072384217787944335L;

        @JsonProperty("final_promotion_path_map_data")
        private List<FinalPromotionPathDetail> finalPromotionPathMapData;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FinalPromotionPathDetail implements Serializable {

        @Serial
        private static final long serialVersionUID = -2072384217787944335L;

        // 满759减374
        @JsonProperty("promotion_desc")
        private String promotionDesc;

        // 1710691199000
        @JsonProperty("promotion_end_time")
        private String promotionEndTime;

        // 374
        @JsonProperty("promotion_fee")
        private String promotionFee;

        // 1344064653397
        @JsonProperty("promotion_id")
        private String promotionId;

        // 1710432000000
        @JsonProperty("promotion_start_time")
        private String promotionStartTime;

        // 满元减 || 商品券
        @JsonProperty("promotion_title")
        private String promotionTitle;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 8246818614184429566L;
        @JsonProperty("click_url")
        private String clickUrl;

        @JsonProperty("commission_type")
        private String commissionType;

        @JsonProperty("coupon_share_url")
        private String couponShareUrl;

        @JsonProperty("income_info")
        private IncomeInfo incomeInfo;

        // 10.0
        @JsonProperty("income_rate")
        private String incomeRate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IncomeInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 8246818614184429566L;

        @JsonProperty("commission_amount")
        private String commissionAmount;

        @JsonProperty("commission_rate")
        private String commissionRate;

        @JsonProperty("subsidy_amount")
        private String subsidyAmount;

        @JsonProperty("subsidy_rate")
        private String subsidyRate;
    }

    public static String analyzingItemId(GeneralConvertResp tbProduct) {
        if (tbProduct == null) {
            return null;
        }
        String itemId = tbProduct.getItemId();
        GeneralConvertResp.ItemBasicInfo itemBasicInfo = tbProduct.getItemBasicInfo();
        GeneralConvertResp.PricePromotionInfo pricePromotionInfo = tbProduct.getPricePromotionInfo();
        if (StringUtils.isBlank(itemId)) {
            if (itemBasicInfo == null || StringUtils.isBlank(itemBasicInfo.getItemUrl())) {
                return null;
            }
            String itemUrl = itemBasicInfo.getItemUrl();
            // https://uland.taobao.com/item/edetail?id=g4kQxqPU3t20YWJwR5iYMOibUr-QA0RODib7nd058YtB
            Map<String, String> parameters = UrlUtils.getParameters(itemUrl);
            if (MapUtils.isNotEmpty(parameters)) {
                itemId = parameters.get("id");
            }
        }
        if (StringUtils.isBlank(itemId)) {
            return null;
        }

        return itemId;
    }
}
