package com.jeesite.modules.cat.model;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MaocheProductCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = -7403840131449259727L;

    @EsItemAspect
    private Long id;
    @EsItemAspect(queryType = "matchPhraseQuery")
    private String content;
    @EsItemAspect(queryType = "function")
    private String contentNew;
    @EsItemAspect
    private String itemId;
    @EsItemAspect
    private String itemIdSuffix;
    @EsItemAspect
    private String uniqueHash;
    @EsItemAspect(queryType = "matchPhraseQuery")
    private String title;

    @EsItemAspect
    private String status;

    // range查询
    @EsItemAspect(queryType = "rangeQuery", field = "affLinkConvTime", rangeOp = "gte")
    private Long startAffLinkConvTime;
    @EsItemAspect(queryType = "rangeQuery", field = "affLinkConvTime", rangeOp = "lte")
    private Long endAffLinkConvTime;

    // range查询
    @EsItemAspect(queryType = "rangeQuery", field = "syncTime", rangeOp = "gte")
    private Long startSyncTime;
    @EsItemAspect(queryType = "rangeQuery", field = "syncTime", rangeOp = "lte")
    private Long endSyncTime;

    // range查询
    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "gte")
    private Long startCreateTime;
    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "lte")
    private Long endCreateTime;


    // range查询
    @EsItemAspect(queryType = "rangeQuery", field = "updateTime", rangeOp = "gte")
    private Long startUpdateTime;
    @EsItemAspect(queryType = "rangeQuery", field = "updateTime", rangeOp = "lte")
    private Long endUpdateTime;

    @EsItemAspect
    private long processed;

    @EsItemAspect
    private String affType;

    @EsItemAspect(queryType = "existsQuery", field = "affType")
    private Boolean hadAffType;

}
