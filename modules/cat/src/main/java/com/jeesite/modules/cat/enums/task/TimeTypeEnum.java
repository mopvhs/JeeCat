package com.jeesite.modules.cat.enums.task;

public enum TimeTypeEnum {

    NOW("立即发布"),

    // 定时
    SCHEDULE("定时发布"),

    // 每日定时
    DAILY_SCHEDULE("每日定时发布"),

    // 重复任务的每日定时
    REPEAT_DAILY_SCHEDULE("重复的任务每日定时发布"),
    ;

    private String desc;

    TimeTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static TimeTypeEnum getByName(String name) {
        for (TimeTypeEnum timeTypeEnum : TimeTypeEnum.values()) {
            if (timeTypeEnum.name().equals(name)) {
                return timeTypeEnum;
            }
        }
        return null;
    }

}
