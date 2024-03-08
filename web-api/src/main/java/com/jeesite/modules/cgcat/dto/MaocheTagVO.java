package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MaocheTagVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8827427076443730898L;

    private Long id;

    private Long parentId;

    private String tagName;

    private String level;
}
