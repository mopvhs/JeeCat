package com.jeesite.modules.cat.enums;

import lombok.Getter;

@Getter
public enum CatActivityEnum {
    GOOD_PRICE("good_price", "有好价");

    private String activity;

    private String desc;

    CatActivityEnum(String activity, String desc) {
        this.activity = activity;
        this.desc = desc;
    }
}
