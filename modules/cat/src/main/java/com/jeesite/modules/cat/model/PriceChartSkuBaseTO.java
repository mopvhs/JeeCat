package com.jeesite.modules.cat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class PriceChartSkuBaseTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 119949177562604630L;

    private String key;

    private String quantity;

    private Long price;

    private Long skuId;

    private String skuProperty;

    private String compareDesc;

    private String compareDescKeyword;
}
