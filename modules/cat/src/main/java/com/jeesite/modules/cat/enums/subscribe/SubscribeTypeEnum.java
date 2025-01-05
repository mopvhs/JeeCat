package com.jeesite.modules.cat.enums.subscribe;

public enum SubscribeTypeEnum {

    BRAND_LIB_KEYWORD("brand_lib_keyword", "品牌库关键词"),
    ;

    private String type;

    private String desc;

    SubscribeTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
