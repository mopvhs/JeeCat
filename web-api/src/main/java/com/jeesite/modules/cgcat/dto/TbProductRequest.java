package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TbProductRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5248096125216484273L;

    private String numIid;


}
