package com.jeesite.modules.cat.service.cg.third.tb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CommandResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 2837749568497654822L;


    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("coupon_click_url")
    private String couponClickUrl;

    @JsonProperty("coupon_end_time")
    private String couponEndTime;

    @JsonProperty("coupon_info")
    private String couponInfo;

    @JsonProperty("coupon_remain_count")
    private String couponRemainCount;

    @JsonProperty("coupon_total_count")
    private String couponTotalCount;

    @JsonProperty("max_commission_rate")
    private String maxCommissionRate;

    @JsonProperty("reward_info")
    private String rewardInfo;

    @JsonProperty("commission_rate")
    private String commissionRate;

    @JsonProperty("istaote")
    private Integer istaote;

    @JsonProperty("scene_id")
    private String sceneId;

    @JsonProperty("num_iid")
    private String numIid;

    @JsonProperty("sclick_url")
    private String sclickUrl;

    @JsonProperty("zk_final_price")
    private String zkFinalPrice;

    @JsonProperty("white_image")
    private String whiteImage;

    @JsonProperty("volume")
    private String volume;


    @JsonProperty("user_type")
    private String userType;

    @JsonProperty("title")
    private String title;

    @JsonProperty("superior_brand")
    private String superiorBrand;

    // 暂时移除，后续需要再加
//    @JsonProperty("small_images")
//    private List<String> smallImages;

    @JsonProperty("short_title")
    private String shortTitle;

    @JsonProperty("shop_title")
    private String shopTitle;

    @JsonProperty("shop_dsr")
    private String shopDsr;

    @JsonProperty("seller_id")
    private String sellerId;

    @JsonProperty("reserve_price")
    private String reservePrice;

    @JsonProperty("real_post_fee")
    private String realPostFee;

    @JsonProperty("presale_tail_start_time")
    private Long presaleTailStartTime;

    @JsonProperty("presale_tail_end_time")
    private Long presaleTailEndTime;

    @JsonProperty("presale_start_time")
    private Long presaleStartTime;

    @JsonProperty("presale_end_time")
    private Long presaleEndTime;

    @JsonProperty("presale_deposit")
    private String presaleDeposit;

    @JsonProperty("pict_url")
    private String pictUrl;

    @JsonProperty("nick")
    private String nick;

    @JsonProperty("lock_rate_start_time")
    private Long lockRateStartTime;

    @JsonProperty("lock_rate_end_time")
    private Long lockRateEndTime;

    @JsonProperty("lock_rate")
    private String lockRate;

    @JsonProperty("item_url")
    private String itemUrl;

    @JsonProperty("info_dxjh")
    private String infoDxjh;

    @JsonProperty("include_dxjh")
    private String includeDxjh;

    @JsonProperty("coupon_id")
    private String couponId;

    @JsonProperty("cat_name")
    private String catName;

    @JsonProperty("cat_leaf_name")
    private String catLeafName;

    @JsonProperty("coupon_start_fee")
    private String couponStartFee;

    @JsonProperty("coupon_amount")
    private String couponAmount;

    @JsonProperty("coupon_short_url")
    private String couponShortUrl;

    @JsonProperty("tbk_pwd")
    private String tbkPwd;

    @JsonProperty("ios_tbk_pwd")
    private String iosTbkPwd;

    @JsonProperty("global_tbk_pwd")
    private String globalTbkPwd;

}
