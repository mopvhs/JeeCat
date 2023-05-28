package com.jeesite.modules.cat.enums.tb;

import lombok.Getter;

@Getter
public enum NavigatorEnum {

    THEMES("1", "推荐主题"),
    FSTJ("fstj", "粉丝偏好");

    private String id;

    private String desc;

    NavigatorEnum(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }
}
