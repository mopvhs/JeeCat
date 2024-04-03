package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MaocheBrandLibraryIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = 8186870095141853807L;

    private Long id;

    private String brand;

    private String productName;

    private List<String> keyword;

    private String categoryName;

    private String levelOneCategoryName;

    private String englishBrand;

    private long star;

    private long polling;

    private long createTime;

    private String status;

    private String description;

    // 品牌首字母
    private String brandInitial;

    // 群今日新增 - 外部发单数量
    private Long groupDailyInc;

    // 库今日新增 - 此关键词新抓取到的商品
    private Long productDailyInc;

    // 历史推送
    private Long historyPushCnt;

    // 今日新增 - 任务推送次数
    private Long pushDailyInc;

    // 上次推送时间
    private Long lastPushTime;

    // 下次推送时间
    private Long nextPushTime;

    // 标签
    private List<Long> tags;

    // 规格
    private String specifications;
}
