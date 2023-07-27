package com.jeesite.modules.cat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class PriceChartInfoTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8345355547663373908L;

    private Long price;

    private Long skuId;

    private String compareInfoDesc;
}
