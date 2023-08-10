package com.jeesite.modules.cat.enums.task;

public enum TaskSwitchEnum {

    OPEN("开启"),
    CLOSE("关闭"),
    ;

    private String desc;

    private TaskSwitchEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskSwitchEnum getEnum(String name) {
        for (TaskSwitchEnum taskSwitchEnum : TaskSwitchEnum.values()) {
            if (taskSwitchEnum.name().equals(name)) {
                return taskSwitchEnum;
            }
        }
        return null;
    }
}
