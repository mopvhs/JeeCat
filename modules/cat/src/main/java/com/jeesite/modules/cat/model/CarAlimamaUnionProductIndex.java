package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CarAlimamaUnionProductIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = -5946899978779689561L;

    private Long id;
//    private Long categoryId;
    private String categoryName;
    private Long commissionRate;
//    private String commissionType;
//    private Float couponAmount;
//    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
//    private Date couponEndTime;
//    private String couponId;
//    private String couponInfo;
    // 优惠券数量
    private Long couponRemainCount;
//    private String couponShareUrl;
//    private String couponStartFee;
//    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
//    private Date couponStartTime;
//    private Integer couponTotalCount;
//    private String includeDxjh;
//    private String includeMkt;
//    private String infoDxjh;
    // 宝贝描述（推荐理由）
    private String itemDescription;
//    private String itemId;
//    private String itemUrl;
//    private Integer levelOneCategoryId;
    private String levelOneCategoryName;
//    private String nick;
//    private String numIid;
//    private String pictUrl;
//    private String presaleDeposit;
//    private String provCity;
//    private Float realPostFee;
    // 商品一口价格
    private Long reservePrice;
//    private Long sellerId;
    private Long shopDsr;

    // 猫车分
    private Long catDsr;
    private String catDsrTips;
//    private String shopTitle;
//    private String shortTitle;
//    private List<String> smallImages;
//    private String superiorBrand;
    // 商品标题
    private String title;
//    private Float tkTotalCommi;
//    private Float tkTotalSales;
//    private String url;
//    private Integer userType;
    // 30天销量
    private Long volume;
//    private String whiteImage;
//    private String xId;
//    private Float zkFinalPrice;
    // 优惠券面额
    private Long coupon;
    // 优惠券后价
    private Long promotionPrice;

    private Long auditStatus;

    // 标签
    private List<String> brand;
    private List<String> secondBrand;
    private List<String> product;
    private List<String> object;
    private List<String> season;
    private List<String> model;
    private List<String> material;
    private List<String> attribute;
    // 活动
    private List<String> activity;
    // 淘客30天推广量
    private Long tkTotalSales;
    // 上架状态
    private Long saleStatus;
    // 数据来源
    private String dataSource;

    // 入库时间
    private Long createTime;

    // 类目
    private List<Long> cidOnes;
    private List<Long> cidTwos;
    private List<Long> cidThirds;

    // 利益点
    private String benefitDesc;

    private Long syncTime;
    private Long updateTime;
}
