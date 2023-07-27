package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ProductTagTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7046139082759951675L;

    private String title;

    private String icon;

    private Integer height;

    private Integer width;

    public ProductTagTO() {
    }

    public ProductTagTO(String title, String icon, Integer height, Integer width) {
        this.title = title;
        this.icon = icon;
        this.height = height;
        this.width = width;
    }

    public void setIconInfo(String icon, Integer height, Integer width) {
        this.icon = icon;
        this.height = height;
        this.width = width;
    }
}
