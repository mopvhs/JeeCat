package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CatProductBucketTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2936643177392207852L;

    private String key;

    private String name;

    private Long count;

    private Double doubleCount;
}
