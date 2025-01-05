package com.jeesite.modules.cat.service.cg.brandlib.dto;

import com.jeesite.modules.cat.model.CategoryTree;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -204520041222489610L;

    // 品牌库
    private BrandLibDTO brandLib;

    // 关键词
    private List<BrandLibKeywordDTO> keywords;

    // 类目
    private List<LibCategoryDetail> categories;

}
