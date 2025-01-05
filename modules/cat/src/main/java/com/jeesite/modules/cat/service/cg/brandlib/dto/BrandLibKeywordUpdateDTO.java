package com.jeesite.modules.cat.service.cg.brandlib.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class BrandLibKeywordUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4141890342678869967L;

    private Long id;

    private List<String> aliasNames;

//    private String keyword;

    private List<Long> specialTagIds;

    private List<Long> tagIds;

    private String status;

//    private String categoryName;
//
//    private String levelOneCategoryName;
}
