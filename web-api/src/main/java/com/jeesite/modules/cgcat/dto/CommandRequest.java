package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommandRequest implements Serializable {

    private static final long serialVersionUID = -3572449391043277154L;

    private String content;

    /**
     * tb/jd
     */
    private String type;
}
