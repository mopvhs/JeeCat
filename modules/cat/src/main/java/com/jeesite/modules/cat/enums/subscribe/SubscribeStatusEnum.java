package com.jeesite.modules.cat.enums.subscribe;

public enum SubscribeStatusEnum {

    SUBSCRIBE("已订阅"),
    UNSUBSCRIBE("取消订阅"),
    ;
    private String desc;

    SubscribeStatusEnum(String desc) {
        this.desc = desc;
    }

    public static SubscribeStatusEnum isSubscribe(boolean subscribe) {
        if (subscribe) {
            return SUBSCRIBE;
        }
        return UNSUBSCRIBE;
    }

}
