package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BrandCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4027569664315718448L;

    private String brand;

    private String icon;

}
