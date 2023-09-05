package com.jeesite.modules.cat.enums.task;

public enum PushTypeEnum {

    YOU_HAO_JIA("✨有好价✨"),

    YOU_HAO_CHE("✨有豪车✨"),

    YOU_YANG_MAO("✨有羊毛✨"),

    YOU_HAO_PIN("✨有好品✨"),
    ;

    private String desc;

    PushTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static PushTypeEnum getByName(String name) {
        for (PushTypeEnum pushTypeEnum : PushTypeEnum.values()) {
            if (pushTypeEnum.name().equals(name)) {
                return pushTypeEnum;
            }
        }
        return null;
    }

}
