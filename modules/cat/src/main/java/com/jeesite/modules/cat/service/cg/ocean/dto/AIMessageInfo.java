package com.jeesite.modules.cat.service.cg.ocean.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class AIMessageInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 6025388397866860359L;

    private String taskId;

    private List<AIMessage> contents;
}