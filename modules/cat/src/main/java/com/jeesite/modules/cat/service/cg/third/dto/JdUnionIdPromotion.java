package com.jeesite.modules.cat.service.cg.third.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * https://www.dingdanxia.com/doc/97/94
 */
@Data
public class JdUnionIdPromotion implements Serializable {

    @Serial
    private static final long serialVersionUID = 6372896366402717458L;

    private String shortURL;

    private String note;

    @JsonProperty("is_coupon")
    private Integer coupon;

    // todo
    private List<Object> couponInfo;

    private CommissionInfo commissionInfo;

    private PriceInfo priceInfo;

    // todo
    private List<Object> pinGouInfo;

    private ShopInfo shopInfo;

    private String skuName;
    private Long skuId;
    private String owner;
    private Long inOrderCount30Days;

    private ImageInfo imageInfo;

    private List<Object> documentInfo;
    private List<Object> videoInfo;


    @Data
    public static class CommissionInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 5413372506870432893L;

        private Double commission;
        private Double commissionShare;
        private Double couponCommission;
        private Long endTime;
        private Integer isLock;
        private Double plusCommissionShare;
        private Long startTime;
    }

    @Data
    public static class CouponInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 5413372506870432893L;

        private Double takeEndTime;
        private Double takeBeginTime;
        // 券剩余张数
        private Long remainNum;
        // 券有效状态（是否）
        private String yn;
        // 券总张数
        private Long num;
        // 券链接
        private String link;
        // 券面额
        private Long discount;
        // 券有效使用开始时间(时间戳，毫秒)
        private Double beginTime;
        // 券有效使用结束时间(时间戳，毫秒)
        private Double endTime;
        // 券使用平台
        private String platform;
    }

    @Data
    public static class PriceInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 5413372506870432893L;

        private Double lowestCouponPrice;
        private Double lowestPrice;
        private Integer lowestPriceType;
        private Double price;
    }

    @Data
    public static class ShopInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 5413372506870432893L;

        private String afsFactorScoreRankGrade;
        private String afterServiceScore;
        private String commentFactorScoreRankGrade;
        private String logisticsFactorScoreRankGrade;
        private String logisticsLvyueScore;
        private String scoreRankRate;
        private Long shopId;
        private String shopLabel;
        private Integer shopLevel;
        private String shopName;
        private String userEvaluateScore;
    }

    @Data
    public static class ImageInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 5413372506870432893L;

        private List<ImageList> imageList;

    }

    @Data
    public static class ImageList implements Serializable {

        @Serial
        private static final long serialVersionUID = 5413372506870432893L;

        private String url;
    }


}
