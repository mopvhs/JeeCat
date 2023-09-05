package com.jeesite.modules.cat.entity.meta;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TaskStatusCount implements Serializable {

    @Serial
    private static final long serialVersionUID = 4313695553835830980L;

    private String resourceId;

    private String resourceType;

    private Long cnt;
}
