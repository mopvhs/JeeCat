package com.jeesite.modules.cat.enums;

import lombok.Getter;

@Getter
public enum QualityStatusEnum {

    INIT(0L, "初始化"),

    GOLD(1L, "金标");

    private Long status;

    private String desc;

    QualityStatusEnum(Long status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
