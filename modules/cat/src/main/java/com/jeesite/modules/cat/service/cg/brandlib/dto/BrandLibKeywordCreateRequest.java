package com.jeesite.modules.cat.service.cg.brandlib.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibKeywordCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4690892113003084562L;

    // 品牌id
    private Long brandLibId;

    // 别名
    private List<String> aliasNames;

    private String keyword;

    private String categoryName;

    private String levelOneCategoryName;

    private List<Long> tagIds;

    private List<Long> specialTagIds;

}
