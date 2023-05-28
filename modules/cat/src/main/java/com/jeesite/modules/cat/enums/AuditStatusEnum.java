package com.jeesite.modules.cat.enums;

import lombok.Getter;

@Getter
public enum AuditStatusEnum {

    INIT(0, "初始化"),
    // 入库完成
    PASS(1, "审核通过");

    private Integer status;

    private String desc;

    AuditStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
