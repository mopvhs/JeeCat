package com.jeesite.modules.cat.service.cg.task.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PropsInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private String content;

    // todo yhq 枚举
    private String type;
}