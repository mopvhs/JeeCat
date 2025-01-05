package com.jeesite.modules.cgcat.dto.subscribe;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SubscribeHomeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 9099891536773809215L;

    private String userId;
}
