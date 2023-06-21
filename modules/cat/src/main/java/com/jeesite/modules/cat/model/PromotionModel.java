package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PromotionModel implements Serializable {

    @Serial
    private static final long serialVersionUID = -4517542052676399091L;

    // 优惠券数量
    private Long couponRemainCount;

    // 优惠券面额
    private Long coupon;

    // 优惠后价格
    private Long promotionPrice;

}
