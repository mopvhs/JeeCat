package com.jeesite.modules.cat.service.cg.brandlib.dto;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = 8604944755663270100L;


    @EsItemAspect
    private Long id;

    @EsItemAspect
    private Long brandId;

    @EsItemAspect
    private String firstSpell;

    @EsItemAspect(queryType = "matchPhraseQuery")
    private String productName;

    private String name;

    private List<String> sorts;

}
