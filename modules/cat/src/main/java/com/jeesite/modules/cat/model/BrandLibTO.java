package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class BrandLibTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1449870799385457736L;

    private Long id;

    private String brand;		// brand

    private String productName;		// product_name

    // 字母
    private String brandInitial;

    private String englishBrand;		// englishBrand

    private List<String> keywords;		// keyword

    private String categoryName;		// category_name

    private String levelOneCategoryName;		// level_one_category_name

    private Long star;		// star

    private String description;		// description

    private Long polling;		// polling

    private String specifications;		// specifications

    // 历史推送
    private Long historyPush;		// history_push

    // 群今日新增 - 外部发单数量
    private Long groupDailyInc;

    // 库今日新增 - 此关键词新抓取到的商品
    private Long productDailyInc;

    // 今日推送
    private String pushDailyIncDesc;

    // 历史推送
    private Long historyPushCnt;

    // 下次推送时间
    private Date nextPushTime;
    // yyyy-MM-dd HH:mm:ss
    private String nextPushTimeDesc;

    // 上次推送时间
    private Date lastPushTime;
    // n天前
    private String lastPushTimeDesc;

    // 标签
    private List<Long> tagIds;



}
