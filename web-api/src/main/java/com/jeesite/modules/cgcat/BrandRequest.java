package com.jeesite.modules.cgcat;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BrandRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4027569664315718448L;

    private String keyword;

    private String firstSpell;

    private int page;

    private int size;
}
