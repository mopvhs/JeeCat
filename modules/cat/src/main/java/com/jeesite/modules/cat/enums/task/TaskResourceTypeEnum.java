package com.jeesite.modules.cat.enums.task;

import lombok.Getter;

@Getter
public enum TaskResourceTypeEnum {

    PRODUCT("猫车商品"),
    JD("外部的京东商品"),
    NO_RESOURCE("图文"),
    ;
    private String desc;

    TaskResourceTypeEnum(String desc) {
        this.desc = desc;
    }

    public static TaskResourceTypeEnum getByName(String name) {
        for (TaskResourceTypeEnum value : TaskResourceTypeEnum.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
