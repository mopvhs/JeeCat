package com.jeesite.modules.cat.enums.task;

public enum TaskStatusEnum {

    INIT("初始化"),

    NORMAL("正常"),

    PAUSE("暂停"),

    STOP("停止"),

    FINISHED("已发布"),

    WAITING("待发布"),

    PUSHING("发布中"),

    EXCEPTION("内容异常"),

    DELETE("删除"),
    ;

    private String desc;

    TaskStatusEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskStatusEnum getByName(String name) {
        for (TaskStatusEnum taskStatusEnum : TaskStatusEnum.values()) {
            if (taskStatusEnum.name().equals(name)) {
                return taskStatusEnum;
            }
        }
        return null;
    }
}
