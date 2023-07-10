package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class HighLightTextTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7381921985293397352L;

    private String normal;

    private String highLight;

}
