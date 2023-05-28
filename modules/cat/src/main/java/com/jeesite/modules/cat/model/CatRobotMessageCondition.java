package com.jeesite.modules.cat.model;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CatRobotMessageCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = -7403840131449259727L;

    @EsItemAspect
    private Long id;
    @EsItemAspect(queryType = "matchQuery")
    private String msg;
    @EsItemAspect(queryType = "matchQuery")
    private String msgNew;
    @EsItemAspect
    private String fromGid;
    @EsItemAspect
    private String fromId;
    @EsItemAspect
    private String toId;
    private String imageUrl;
    @EsItemAspect
    private String msgSvrId;
    @EsItemAspect
    private String fromType;
    @EsItemAspect
    private String msgType;

    // range查询
    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "gte")
    private Long startCreateTime;
    @EsItemAspect(queryType = "rangeQuery", field = "createTime", rangeOp = "lte")
    private Long endCreateTime;

}
