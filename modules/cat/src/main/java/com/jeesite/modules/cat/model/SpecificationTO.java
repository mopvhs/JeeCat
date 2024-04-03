package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SpecificationTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5191781787972723861L;

    private String id;

    // 规格	制式	数量	日常价	有好价	有豪车	备注	操作
    private String specification;

    private String unit;

    private String num;

    private String price;

    private String goodPrice;

    private String luxuryCarPrice;

    private String remark;
}
