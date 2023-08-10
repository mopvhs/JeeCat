package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PushTaskResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 5142568595904948135L;

    private String taskId;

    private List<PushTaskDetail> details;
}
