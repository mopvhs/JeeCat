package com.jeesite.modules.cat.model.dataoke;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DaTaoKeResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -5752935215051114254L;

    private Boolean cache;

    private Integer code;

    private String msg;

    private String requestId;

    private Long time;

    private T data;
}
