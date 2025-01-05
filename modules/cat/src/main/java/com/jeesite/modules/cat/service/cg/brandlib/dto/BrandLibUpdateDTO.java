package com.jeesite.modules.cat.service.cg.brandlib.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4141890342678869967L;

    private Long id;

    private List<String> aliasNames;

    private Boolean blacklist;
}
