package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UnionProductModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 8092228226504839452L;

    private Long id;

    private String itemId;

    private String itemIdSuffix;

    // 标题
    private String title;

    // 利益点
    private String benefitDesc;

    // 宝贝描述（推荐理由）
    private String itemDescription;

    private Long reservePrice;

    private Long originalPrice;

    // 入库转态
    private Long auditStatus;

    private Long qualityStatus;

    // 上架状态
    private Long saleStatus;
    private Long saleStatusTime;

    // 数据来源
    private String dataSource;

    // 入库时间
    private Long createTime;

    // 商品主图链接
    private String mainPic;

    // 佣金比例
    private Long commissionRate;

    // 30天销量
    private Long volume;

    // 淘客30天推广量
    private Long tkTotalSales;

    // 店铺信息
    private ShopModel shop;

    // 优惠信息
    private PromotionModel promotion;

    // 分数服务
    private ProductScoreModel score;

    private ProductCategoryModel category;

    private Long updateTime;

    private Long syncTime;

    // 商品品明(detail接口)
    private String propsBrand;
    private String propsProductName;
    private List<RateDetailTO> rates;

    private String customBenefit;
}
