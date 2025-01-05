package com.jeesite.modules.cat.service.cg.brandlib.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -204520041222489610L;

    private Long id;

    private String icon;

    private String productName;

    private List<String> aliasNames;

    private int subTotalCnt;

    private Boolean blacklist;


}
