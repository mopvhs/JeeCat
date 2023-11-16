package com.jeesite.modules.cat.model.ocean;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class OceanMessageCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = -1494335369693991391L;

    @EsItemAspect
    private Long id;

    @EsItemAspect(queryType = "itemsQuery", field = "id")
    private List<Long> ids;

    @EsItemAspect(queryType = "mustNotItemsQuery", field = "id")
    private List<Long> filterIds;

    @EsItemAspect(queryType = "itemQuery")
    private String category;

    // 宝贝描述（推荐理由）
    @EsItemAspect(queryType = "matchQuery")
    private String msg;

    @EsItemAspect(queryType = "rangeQuery", field = "createDate", rangeOp = "gte")
    private Long gteCreateDate;
//    @EsItemAspect(queryType = "rangeQuery", field = "volume", rangeOp = "lte")
//    private Long lteVolume;

    @EsItemAspect(queryType = "itemsQuery")
    private List<String> resourceIds;

    @EsItemAspect
    private String affType;

    // 格式：如果没有，默认desc
    // updateTime desc
    // updateTime asc
    private List<String> sorts;

}
