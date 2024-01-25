package com.jeesite.modules.cat.service.cg.task.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ProductDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private int num;

    // 商品规格
    private List<PropsInfo> propsInfos;

    private List<String> imgs;

    // 日常价
    private Long price;

    // 实付
    private Long payPrice;

    // 折后单价
    private Long discountPrice;

    // 88VIP价格 / PLUS
    private Long vipPrice;

    // 官旗，百补等前缀
    private NameDetail subTitle;

    private String title;

    private String resourceId;

    private String resourceType;

    // 淘宝的是数字的id，京东是skuId
    private String uniqueId;

    // 口令
    private String command;

    // 券活动 -》 列表 保存对象 （标题，券口令）
    private List<NameDetail> coupons;

}