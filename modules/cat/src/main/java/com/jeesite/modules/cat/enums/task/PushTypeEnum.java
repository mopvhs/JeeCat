package com.jeesite.modules.cat.enums.task;

public enum PushTypeEnum {

    NOW("立即发布"),

    // 定时
    SCHEDULE("定时发布"),
    ;

    private String desc;

    PushTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static PushTypeEnum getByDesc(String desc) {
        for (PushTypeEnum pushTypeEnum : PushTypeEnum.values()) {
            if (pushTypeEnum.getDesc().equals(desc)) {
                return pushTypeEnum;
            }
        }
        return null;
    }

}
