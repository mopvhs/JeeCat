package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class TaskRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 6489579789589390832L;

    private List<Long> productIds;

    private String taskId;
}
