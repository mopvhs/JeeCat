package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UpdateRuleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6428940332740618836L;

    private String chatWxId;

    private String robotSendId;
}
