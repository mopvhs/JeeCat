package com.jeesite.modules.cgcat.dto.subscribe;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LibKeywordHomeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5238704528103447300L;

    private Long keywordId;

    private int page = 1;

    private int pageSize = 20;
}
