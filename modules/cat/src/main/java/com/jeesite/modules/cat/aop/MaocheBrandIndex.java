package com.jeesite.modules.cat.aop;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MaocheBrandIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = -5857577750759591836L;

    private Long id;

    private String brand;

    private String firstSpell;

    private String icon;
}
