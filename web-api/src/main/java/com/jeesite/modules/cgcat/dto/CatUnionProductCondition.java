package com.jeesite.modules.cgcat.dto;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CatUnionProductCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = -1494335369693991391L;

    @EsItemAspect
    private Long id;

    // 宝贝描述（推荐理由）
    @EsItemAspect(queryType = "matchQuery")
    private String itemDescription;

    // 商品一口价格
    @EsItemAspect(queryType = "rangeQuery", field = "reservePrice", rangeOp = "gte")
    private Long getReservePrice;
    @EsItemAspect(queryType = "rangeQuery", field = "reservePrice", rangeOp = "lte")
    private Long lteReservePrice;

    // 商品标题
    @EsItemAspect(queryType = "matchQuery")
    private String title;

    // 30天销量
    @EsItemAspect(queryType = "rangeQuery", field = "volume", rangeOp = "gte")
    private Long getVolume;
    @EsItemAspect(queryType = "rangeQuery", field = "volume", rangeOp = "lte")
    private Long lteVolume;

    // 优惠券面额
    @EsItemAspect(queryType = "rangeQuery", field = "coupon", rangeOp = "gte")
    private Long getCoupon;
    @EsItemAspect(queryType = "rangeQuery", field = "coupon", rangeOp = "lte")
    private Long lteCoupon;
}
