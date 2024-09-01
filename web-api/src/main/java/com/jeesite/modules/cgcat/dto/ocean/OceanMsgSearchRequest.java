package com.jeesite.modules.cgcat.dto.ocean;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OceanMsgSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7673692877017328232L;

    // 搜索关键词
    private String keyword;

    // 类目名称
    private String categoryName;

    // 品牌库id
    private Long brandLibId;

    private String status;

    private String sort;

    private int from;

    private int size;
}
