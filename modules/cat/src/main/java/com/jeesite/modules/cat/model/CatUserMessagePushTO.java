package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CatUserMessagePushTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5238877692600588401L;

    private String openId;

    private String keyword;

    /**
     * 自定义一级类目id
     */
    private String cidOne;

    private Integer pageNo;

    private Integer pageSize;

    private String title;

    /**
     * 0 无
     * 1 只看有券
     */
    private Integer onlyCoupon;

    /**
     * 0 无
     * 1 只看有券
     */
    private Integer onlyPriceChart;

    /**
     * xxx desc
     * yyy asc
     */
    private List<String> sorts;
}
