package com.jeesite.modules.cat.enums;

import lombok.Getter;

@Getter
public enum SyncMarkEnum {

    INIT(0, "初始化"),
    // 入库完成
    PASS(1, "需要同步"),
    FINISH(2, "同步完成");

    private Integer type;

    private String desc;

    SyncMarkEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
