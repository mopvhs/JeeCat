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

    // 宝贝描述（推荐理由）
    @EsItemAspect(queryType = "matchPhraseQuery")
    private String msg;

    @EsItemAspect(queryType = "rangeQuery", field = "createDate", rangeOp = "gte")
    private Long gteCreateDate;
    @EsItemAspect(queryType = "rangeQuery", field = "createDate", rangeOp = "lte")
    private Long lteCreateDate;
//    @EsItemAspect(queryType = "rangeQuery", field = "volume", rangeOp = "lte")
//    private Long lteVolume;

    @EsItemAspect(queryType = "itemsQuery")
    private List<String> resourceIds;

    @EsItemAspect
    private Long newProduct;

    @EsItemAspect(queryType = "itemsQuery")
    private List<String> categoryNames;

    @EsItemAspect
    private String affType;

    @EsItemAspect
    private String status;

    @EsItemAspect(queryType = "itemsQuery")
    private List<String> oceanStatus;

    // 格式：如果没有，默认desc
    // updateTime desc
    // updateTime asc
    private List<String> sorts;

//    private String customMsgSearch;

    @EsItemAspect
    private String uniqueHash;

//    /**
//     * 品牌库id
//     */
//    private Long brandLibId;

    private List<String> keywords;

}
