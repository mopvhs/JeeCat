package com.jeesite.modules.cat.service.cg.brandlib.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TagDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7652396595733938472L;

    private Long id;

    private String name;
}
