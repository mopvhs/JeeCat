package com.jeesite.modules.cat.service.cg.brandlib.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4690892113003084562L;

    // 品牌id
    private Long brandId;

    // 别名
    private List<String> aliasNames;

    // 是否黑名单
    private Boolean blacklist;

    private List<String> keywords;

}
