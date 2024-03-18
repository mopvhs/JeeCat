package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PushTaskRuleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1928835088910065764L;

    private Long id;

    // 品牌
    private String brand;

    // 英文品牌
    private String englishBrand;

    // 品名
    private String productName;

    // 关键词
    private List<String> keywords;

    // 子类目
    private String category;

    private Long categoryId;		// category_id

    // 类目
    private String levelOneCategoryName;

    private Long levelOneCategoryId;		// level_one_category_id

    // 星级
    private Long star;

    // 轮询频率
    private Long polling;

    // 产品描述
    private String describe;

    // 标签
    private List<Long> tagIds;
}
