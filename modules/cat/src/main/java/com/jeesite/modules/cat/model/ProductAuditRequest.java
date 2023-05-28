package com.jeesite.modules.cat.model;

import com.jeesite.modules.cat.enums.AuditStatusEnum;
import lombok.Data;

import java.util.List;

@Data
public class ProductAuditRequest {

    private List<Long> ids;

    /**
     * {@link AuditStatusEnum}
     */
    private Integer auditStatus;

    /**
     * {@link com.jeesite.modules.cat.enums.SaleStatusEnum}
     */
    private Long saleStatus;

    private String itemId;

    private Long id;

    /**
     * {@link UnionProductTagTO}
     */
    private UnionProductTagTO tag;

}
