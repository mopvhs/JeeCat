package com.jeesite.modules.cat.service.cg.third.tb.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.helper.PriceHelper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.jeesite.modules.cat.helper.ProductV2Helper.N_YUAN_REGEX;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandResponseV2 implements Serializable {

    @Serial
    private static final long serialVersionUID = 2837749568497654822L;

    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("coupon_click_url")
    private String coupon_click_url;
    @JsonProperty("isv_mktid")
    private String isvMktid;
    //   "": "25.00",
    @JsonProperty("max_commission_rate")
    private String maxCommissionRate;
    @JsonProperty("reward_info")
    private String rewardInfo;
    @JsonProperty("commission_rate")
    private String commissionRate;
    @JsonProperty("istaote")
    private Long istaote;

    @JsonProperty("scene_id")
    private String sceneId;

    @JsonProperty("num_iid")
    private String numIid;
    @JsonProperty("sclick_url")
    private String sclickUrl;
    @JsonProperty("item_basic_info")
    private ItemBasicInfo itemBasicInfo;
    @JsonProperty("item_id")
    private String itemId;
    @JsonProperty("presale_info")
    private Object presaleInfo;
    @JsonProperty("price_promotion_info")
    private PricePromotionInfo pricePromotionInfo;
    @JsonProperty("scope_info")
    private Object scopeInfo;
    @JsonProperty("coupon_start_time")
    private String couponStartTime;
    @JsonProperty("coupon_end_time")
    private String couponEndTime;
    @JsonProperty("coupon_info")
    private String couponInfo;
    @JsonProperty("coupon_remain_count")
    private String couponRemainCount;
    @JsonProperty("coupon_total_count")
    private String couponTotalCount;
    @JsonProperty("coupon_type")
    private String couponType;
    @JsonProperty("coupon_start_fee")
    private String couponStartFee;
    @JsonProperty("coupon_amount")
    private String couponAmount;
    @JsonProperty("tbk_pwd")
    private String tbkPwd;

    @JsonProperty("ios_tbk_pwd")
    private String iosTbkPwd;

    @JsonProperty("global_tbk_pwd")
    private String globalTbkPwd;

    @JsonProperty("coupon_short_url")
    private String couponShortUrl;

    @JsonProperty("coupon_id")
    private String couponId;

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

        @JsonProperty("white_image")
        private String whiteImage;

        @JsonProperty("item_url")
        private String itemUrl;
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
}
