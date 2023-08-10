package com.jeesite.modules.cat.enums.task;

public enum TaskTypeEnum {

    PUSH("推送"),
    ;

    private String desc;

    TaskTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskTypeEnum getByDesc(String desc) {
        for (TaskTypeEnum taskTypeEnum : TaskTypeEnum.values()) {
            if (taskTypeEnum.getDesc().equals(desc)) {
                return taskTypeEnum;
            }
        }
        return null;
    }
}
