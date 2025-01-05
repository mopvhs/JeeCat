package com.jeesite.modules.cgcat.dto.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BelongLibReq implements Serializable {

    @Serial
    private static final long serialVersionUID = -5699848799670256773L;

    private String content;
}
