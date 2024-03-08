package com.jeesite.modules.cat.model.condition;

import com.jeesite.modules.cat.aop.EsItemAspect;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * https://www.dingdanxia.com/doc/15/8
 */
@Data
public class PushTaskIndexCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = -1494335369693991391L;

    @EsItemAspect
    private Long id;

    // 宝贝描述（推荐理由）
    @EsItemAspect(queryType = "itemQuery", field = "brandLibIds")
    private Long brandLibId;


    // 宝贝描述（推荐理由）
    @EsItemAspect(queryType = "existsQuery", field = "brandLibIds")
    private Boolean hadBrandLibId;

    // 状态
    @EsItemAspect(needLowerCase = true)
    private String status;

    @EsItemAspect(queryType = "rangeQuery", field = "finishTime", rangeOp = "gte")
    private Long gteFinishTime;


}
