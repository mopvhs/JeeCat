package com.jeesite.modules.cgcat.dto.ocean;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OceanMsgProductSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7673692877017328232L;

    // 搜索关键词
    private String keyword;

    // 类目
    private String category;

    private String category2;

    private String sort;

    private Long msgProductId;
}
