package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ProductPriceTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3665095261298244337L;

    private String desc;

    private Long price;
}
