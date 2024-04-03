package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PushTaskRuleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2998492883357036286L;

    private Long id;

    @NotNull(message = "品牌不能为空")
    // 品牌
    private String brand;

    @NotNull(message = "英文品牌不能为空")
    // 英文品牌
    private String englishBrand;

    @NotNull(message = "品名不能为空")
    // 品名
    private String productName;

    @NotNull(message = "品名不能为空")
    // 关键词
    private List<String> keywords;

//    @NotNull(message = "子类目不能为空")
    // 子类目
    private String category;

    private Long categoryId;		// category_id

    @NotNull(message = "类目不能为空")
    // 类目
    private String levelOneCategoryName;

    private Long levelOneCategoryId;		// level_one_category_id

    @NotNull(message = "星级不能为空")
    // 星级
    private Long star;

    @NotNull(message = "轮询频率不能为空")
    // 轮询频率
    private Long polling;

    // 产品描述
    private String describe;

    // 标签
    private List<String> tagIds;

    // 规格
    private List<SpecificationVO> specifications;
}
