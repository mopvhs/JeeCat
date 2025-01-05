package com.jeesite.modules.cat.enums;

import lombok.Getter;

@Getter
public enum OceanStatusEnum {

    INIT("初始值"),
    NORMAL("正常"),
    FAIL("失败"),
    SIMILAR("相似内容"),
    EXCEPTION("异常"),
    ;

    private String desc;

    OceanStatusEnum (String desc) {
        this.desc = desc;
    }
}
