package com.jeesite.modules.cat.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum ElasticSearchIndexEnum {
    CAT_PRODUCT_INDEX("product_index", "_doc", "猫车商品索引"),
    CAT_ROBOT_CRAWLER_MESSAGE_INDEX("maoche_robot_crawler_message_index", "_doc", "猫车机器人抓取信息索引"),
    MAOCHE_PRODUCT_INDEX("maoche_product", "_doc", "老谭猫车商品信息索引"),
    ;

    private String index;

    private String type;

    private String desc;

    ElasticSearchIndexEnum(String index, String type, String desc) {
        this.index = index;
        this.type = type;
        this.desc = desc;
    }

    public static ElasticSearchIndexEnum getByIndex(String index) {
        if (StringUtils.isBlank(index)) {
            return null;
        }

        for (ElasticSearchIndexEnum obe : values()) {
            if (obe.getIndex().equals(index)) {
                return obe;
            }
        }

        return null;
    }

}
