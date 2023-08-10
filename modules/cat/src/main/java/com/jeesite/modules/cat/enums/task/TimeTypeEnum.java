package com.jeesite.modules.cat.enums.task;

public enum TimeTypeEnum {

    NOW("立即发布"),

    // 定时
    SCHEDULE("定时发布"),

    // 每日定时
    DAILY_SCHEDULE("每日定时发布"),
    ;

    private String desc;

    TimeTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static TimeTypeEnum getByDesc(String desc) {
        for (TimeTypeEnum timeTypeEnum : TimeTypeEnum.values()) {
            if (timeTypeEnum.getDesc().equals(desc)) {
                return timeTypeEnum;
            }
        }
        return null;
    }

}
