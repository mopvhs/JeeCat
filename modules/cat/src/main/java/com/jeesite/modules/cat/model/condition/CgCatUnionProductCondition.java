package com.jeesite.modules.cat.model.condition;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * https://www.dingdanxia.com/doc/15/8
 */
@Data
public class CgCatUnionProductCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = -1494335369693991391L;

    @EsItemAspect
    private Long id;

    // 宝贝描述（推荐理由）
    @EsItemAspect(queryType = "matchQuery")
    private String itemDescription;
//    private String itemId;
//    private String itemUrl;
//    private Integer levelOneCategoryId;
//    @EsItemAspect(queryType = "matchQuery")
//    private String levelOneCategoryName;
//    private String nick;
//    private String numIid;
//    private String pictUrl;
//    private String presaleDeposit;
//    @EsItemAspect(queryType = "matchQuery")
//    private String provCity;
//    private Float realPostFee;
    // 商品一口价格
    @EsItemAspect(queryType = "rangeQuery", field = "reservePrice", rangeOp = "gte")
    private Long getPrice;
    @EsItemAspect(queryType = "rangeQuery", field = "reservePrice", rangeOp = "lte")
    private Long ltePrice;

    // 佣金比例 1550表示15.5%
    @EsItemAspect(queryType = "rangeQuery", field = "commissionRate", rangeOp = "gte")
    private Long getCommissionRate;
    @EsItemAspect(queryType = "rangeQuery", field = "commissionRate", rangeOp = "lte")
    private Long lteCommissionRate;

//    private Long sellerId;
//    private Integer shopDsr;
//    @EsItemAspect(queryType = "matchQuery")
//    private String shopTitle;
//    @EsItemAspect(queryType = "matchQuery")
//    private String shortTitle;
//    private List<String> smallImages;
//    private String superiorBrand;
    // 商品标题
    @EsItemAspect(queryType = "matchQuery")
    private String title;
//    private Float tkTotalCommi;
//    private Float tkTotalSales;
//    private String url;
//    private Integer userType;
    // 30天销量
    @EsItemAspect(queryType = "rangeQuery", field = "volume", rangeOp = "gte")
    private Long getVolume;
    @EsItemAspect(queryType = "rangeQuery", field = "volume", rangeOp = "lte")
    private Long lteVolume;
//    private String whiteImage;
//    private String xId;
//    private Float zkFinalPrice;
    // 优惠券面额
    @EsItemAspect(queryType = "rangeQuery", field = "coupon", rangeOp = "gte")
    private Long getCoupon;
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
//
//    // range查询
//    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "gte")
//    private Long startCreateTime;
//    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "lte")
//    private Long endCreateTime;

}
