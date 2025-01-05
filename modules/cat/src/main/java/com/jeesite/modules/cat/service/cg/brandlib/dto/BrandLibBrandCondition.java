package com.jeesite.modules.cat.service.cg.brandlib.dto;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibBrandCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = 8604944755663270100L;

//    @EsItemAspect(queryType = "itemsQuery", field = "firstSpell")
//    private List<String> firstSpells;

    @EsItemAspect
    private String firstSpell;
}
