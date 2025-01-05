package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CategoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5875925706095412378L;

    private Long id;

    private String title;

    private Long count;
}
