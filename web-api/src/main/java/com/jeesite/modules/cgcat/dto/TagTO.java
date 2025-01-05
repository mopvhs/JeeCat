package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TagTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -775334713423222659L;

    private Long id;

    private String name;
}
