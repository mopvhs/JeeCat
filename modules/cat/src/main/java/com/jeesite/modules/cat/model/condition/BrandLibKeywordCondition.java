package com.jeesite.modules.cat.model.condition;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibKeywordCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = -1507028657144915965L;

    @EsItemAspect
    private Long id;

    private String keyword;

    @EsItemAspect(queryType = "itemQuery")
    private String levelOneCategoryName;

    private String userId;

    // 格式：如果没有，默认desc
    // updateTime desc
    // updateTime asc
    private List<String> sorts;

    private int page = 1;

    private int pageSize = 20;


}
