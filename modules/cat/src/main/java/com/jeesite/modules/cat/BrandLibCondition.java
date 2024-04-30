package com.jeesite.modules.cat;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @EsItemAspect
    private Long id;
    @EsItemAspect(queryType = "itemsQuery", field = "id")
    private List<Long> ids;

    @EsItemAspect(queryType = "mustNotItemsQuery", field = "id")
    private List<Long> filterIds;

    @EsItemAspect(queryType = "itemQuery")
    private String categoryName;

    @EsItemAspect(queryType = "itemsQuery", field = "categoryName")
    private List<String> categoryNames;

    @EsItemAspect(queryType = "itemsQuery", field = "keyword")
    private List<String> keywords;

    @EsItemAspect
    private String levelOneCategoryName;

    @EsItemAspect
    private Long star;

    @EsItemAspect
    private String status;

    // 入库时间
    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "gte")
    private Long gteCreateTime;

    // 格式：如果没有，默认desc
    // updateTime desc
    // updateTime asc
    private List<String> sorts;

    // 是否需要明细
    private boolean detail = false;

    private String rootCategoryName;

    private Integer pageNo;

    private Integer pageSize;
}
