package com.jeesite.modules.cat.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.jeesite.modules.cat.aop.EsItemAspect;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class CatUnionProductCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = -1494335369693991391L;

    @EsItemAspect
    private Long id;
    @EsItemAspect(queryType = "mustNotItemsQuery", field = "id")
    private List<Long> filterIds;
//    @EsItemAspect
//    private Long categoryId;
    @EsItemAspect(queryType = "itemQuery")
    private String categoryName;
//    private String commissionType;
//    private Float couponAmount;
//    private Date couponEndTime;
//    @EsItemAspect
//    private String couponId;
//    @EsItemAspect(queryType = "matchQuery")
//    private String couponInfo;

    @EsItemAspect(queryType = "itemsQuery", field = "categoryName")
    private List<String> categoryNames;

    // 优惠券数量
    @EsItemAspect(queryType = "rangeQuery", field = "couponRemainCount", rangeOp = "gte")
    private Long gteCouponRemainCount;
//    private String couponShareUrl;
//    private String couponStartFee;
//    private Date couponStartTime;
//    private Integer couponTotalCount;
//    private String includeDxjh;
//    private String includeMkt;
//    private String infoDxjh;
    // 宝贝描述（推荐理由）
    @EsItemAspect(queryType = "matchQuery")
    private String itemDescription;
//    private String itemId;
//    private String itemUrl;
//    private Integer levelOneCategoryId;
    @EsItemAspect
    private String levelOneCategoryName;
//    private String nick;
//    private String numIid;
//    private String pictUrl;
//    private String presaleDeposit;
//    @EsItemAspect(queryType = "matchQuery")
//    private String provCity;
//    private Float realPostFee;
    // 商品一口价格
    @EsItemAspect(queryType = "rangeQuery", field = "reservePrice", rangeOp = "gte")
    private Long gteReservePrice;
    @EsItemAspect(queryType = "rangeQuery", field = "reservePrice", rangeOp = "lte")
    private Long lteReservePrice;

    // 商品一口价格
    @EsItemAspect(queryType = "rangeQuery", field = "promotionPrice", rangeOp = "gte")
    private Long gtePromotionPrice;
    @EsItemAspect(queryType = "rangeQuery", field = "promotionPrice", rangeOp = "lte")
    private Long ltePromotionPrice;

    @EsItemAspect(queryType = "itemsQuery")
    private List<String> activity;

    // 佣金率
    @EsItemAspect(queryType = "rangeQuery", field = "commissionRate", rangeOp = "gte")
    private Long gteCommissionRate;
    @EsItemAspect(queryType = "rangeQuery", field = "commissionRate", rangeOp = "lte")
    private Long lteCommissionRate;
//    private Long sellerId;
    // 店铺dsr评分，筛选高于等于当前设置的店铺dsr评分的商品0-50000之间
    @EsItemAspect(queryType = "rangeQuery", field = "shopDsr", rangeOp = "gte")
    private Integer gteShopDsr;
//    @EsItemAspect(queryType = "matchQuery")
//    private String shopTitle;
//    @EsItemAspect(queryType = "matchQuery")
//    private String shortTitle;
//    private List<String> smallImages;
//    private String superiorBrand;
    // 商品标题
//    @EsItemAspect(queryType = "matchQuery")
    @EsItemAspect(queryType = "matchPhraseQuery")
    private String title;
//    private Float tkTotalCommi;
    // 月推广量
    @EsItemAspect(queryType = "rangeQuery", field = "tkTotalSales", rangeOp = "gte")
    private Long gteTkTotalSales;
//    private String url;
//    private Integer userType;
    // 30天销量
    @EsItemAspect(queryType = "rangeQuery", field = "volume", rangeOp = "gte")
    private Long gteVolume;
    @EsItemAspect(queryType = "rangeQuery", field = "volume", rangeOp = "lte")
    private Long lteVolume;
//    private String whiteImage;
//    private String xId;
//    private Float zkFinalPrice;
    // 优惠券面额
    @EsItemAspect(queryType = "rangeQuery", field = "coupon", rangeOp = "gte")
    private Long gteCoupon;
    @EsItemAspect(queryType = "rangeQuery", field = "coupon", rangeOp = "lte")
    private Long lteCoupon;
//
//    @EsItemAspect(queryType = "matchQuery")
//    private String msg;
//    @EsItemAspect(queryType = "matchQuery")
//    private String msgNew;
//    @EsItemAspect
//    private String fromGid;
//    @EsItemAspect
//    private String fromId;
//    @EsItemAspect
//    private String toId;
//    private String imageUrl;
//    @EsItemAspect
//    private String msgSvrId;
//    @EsItemAspect
//    private String fromType;
//    @EsItemAspect
//    private String msgType;
    @EsItemAspect
    private Integer auditStatus;

    @EsItemAspect(queryType = "mustNotItemQuery", field = "auditStatus")
    private Integer notAuditStatus;

    @EsItemAspect
    private Long saleStatus;
//
//    // range查询
//    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "gte")
//    private Long startCreateTime;
//    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "lte")
//    private Long endCreateTime;

    // range查询
    @EsItemAspect(queryType = "rangeQuery", field = "catDsr", rangeOp = "gte")
    private Long startCatDsr;
    @EsItemAspect(queryType = "rangeQuery", field = "catDsr", rangeOp = "lte")
    private Long endCatDsr;

    // 入库时间
    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "gte")
    private Long gteCreateTime;

    // 入库时间
    @EsItemAspect(queryType = "rangeQuery", field = "updateTime", rangeOp = "gte")
    private Long gteUpdateTime;

    @EsItemAspect(queryType = "itemsQuery")
    private List<Long> cidOnes;

    // 格式：如果没有，默认desc
    // updateTime desc
    // updateTime asc
    private List<String> sorts;

    @EsItemAspect
    private String dataSource;

    @EsItemAspect(queryType = "existsQuery", field = "rates")
    private Boolean hadRates;
}
