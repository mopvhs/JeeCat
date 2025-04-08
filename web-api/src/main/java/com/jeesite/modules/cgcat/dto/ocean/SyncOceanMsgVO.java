package com.jeesite.modules.cgcat.dto.ocean;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SyncOceanMsgVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2998263601488157036L;

    private Long robotMsgId;
}
