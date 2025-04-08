package com.jeesite.modules.cat.service.cg.ocean.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AIMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 6025388397866860359L;

    private Long msgId;

    private int contentType;

    private String content;

    private String aiContent;

    private Long time;
}