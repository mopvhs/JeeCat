package com.jeesite.modules.cgcat.dto.brandlib;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1078729539083212998L;

    private Long brandId;

    private Long libId;

    private String productName;

    private List<String> aliasNames;

    private String icon;

    private List<BrandLibKeywordTO> keywords;
}
