package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class UnionProductSyncRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 4356428830582888193L;

    private Long id;

    private List<Long> ids;
}
