package com.jeesite.modules.cat.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UnionProductTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2354649565752124529L;

    private Long id;

    private String itemId;

    // 平台
    private String platform;

    // 分享口令
    private String shareCommand;

    // 商品标题
    private String title;

    // 短标题
    private String shortTitle;

    // dsr分
    private Long shopDsr;

    // 商品价格
    private Long reservePrice;

    // 佣金率 展示除以100
    private Long commissionRate;

    // 佣金，单位分  展示除以100
    private Long commission;

    // 销量
    private Long volume;

    // 好评率
    private Long goodRate;

    // 商品优势
    private List<String> productAdvantage;
    // 价格优势
    private List<String> priceAdvantage;
    // 活动
    private List<String> activity;

    // 有好价 优惠标签
    private List<PromotionTagTO> promotionTags;

    // 入库状态
    private Long auditStatus;

    // 商品图片
    private String imgUrl;

    // 商品标签
    private UnionProductTagTO tag;

    // 类目名称
    private String categoryName;

    // 自定义类目名称
    private List<String> cidOneNames;

    // 优惠券数量
    private Long couponRemainCount;
    // 优惠券总量
    private Long couponTotalCount;
    // 优惠券金额
    private Long coupon;
    // 优惠券开始金额
    private Long couponStartFee;
    // 优惠券后价格
    private Long promotionPrice;

    // 店铺名
    private String shopName;

    // 商品描述
    private String itemDescription;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    // 入库时间
    private Date createDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    // 更新时间
    private Date updateDate;

    // 淘客30天推广量
    private Long tkTotalSales;

    // 上架状态
    private Long saleStatus;
    // 上架时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date onShelfDate;

    // 商品所属 （超搜、有好价）
    private List<String> belongTo;

    // 猫车分
    private Long catDsr;
    // 猫车分计算公式
    private String catDsrTips;

    // 评论
    private RateTO rate;

    // 利益点
    private String benefitDesc;

    // 利益点
    private HighLightTextTO highLightBenefitDesc;

    // 数据来源
    private String dataSource;

    // 高亮利益点(有好价 -> 维易详情)
    private HighLightTextTO mainBenefit;

    private Long qualityStatus;

    private String qualityIcon;

    // 价格信息
    private ProductPriceTO displayPrice;

    // 图片价格标签
    private List<ProductTagTO> priceChartTags;

    private List<HighLightTextTO> daoDaoList;

    // 自定义标签(new)
    private List<ProductTagTO> productTags;

    // 自定义利益点
    private String customBenefit;

    // 已完成的任务推送次数
    private Long pushNum;

    private String itemUrl;

    // 活动信息
    private String predictRoundingUpPriceDesc;
    // 到手价活动优惠标签
    // 各个调整，替换优惠价格为到手价【腾讯文档】到手价修改文档
    //https://docs.qq.com/doc/DQm1FTENVTld4amFI
    private List<String> pricePromotionTagList = new ArrayList<>();

}
