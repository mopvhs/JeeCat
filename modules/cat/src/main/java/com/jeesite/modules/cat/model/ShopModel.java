package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ShopModel implements Serializable {

    @Serial
    private static final long serialVersionUID = -4315927089897035629L;

    // 店铺名称
    private String shopName;

    // 店铺等级
    private Long shopLevel;

    private Long shopDsr;

    // 品牌名称
    private String brandName;

    private String fans = null;

    private Long creditLevel = null;
}
