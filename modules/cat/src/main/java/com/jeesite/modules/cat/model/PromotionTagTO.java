package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PromotionTagTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4520842785587232475L;

    private String tagType;

    private String tagTypeDisplay;

    private String tagDisplay;
}
