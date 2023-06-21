package com.jeesite.modules.cat.enums;

import lombok.Getter;

@Getter
public enum ProductDataSource {

    DINGDANXIA("dingdanxia", "订单侠"),
    VEAPI("veapi", "易维"),
    DATAOKE("dataoke", "大淘客");

    private String source;

    private String desc;

    ProductDataSource(String source, String desc) {
        this.source = source;
        this.desc = desc;
    }
}
