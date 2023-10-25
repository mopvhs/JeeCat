package com.jeesite.modules.cat.enums;

import lombok.Getter;

@Getter
public enum SaleStatusEnum {

    INIT(0L, "初始化"),
    ON_SHELF(1L, "上架完成"),

    PREPARE_SHELF(2L, "上架筹备中"),
    OFF_SHELF(1001L, "已下架"),
    AUTO_OFF_SHELF(10001L, "自动下架"),
    ;

    private Long status;

    private String desc;

    SaleStatusEnum(Long status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
