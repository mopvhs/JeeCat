package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CatNineProductTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2671689814736896270L;

    private Integer pageNo;

    private String cidOne;
}
