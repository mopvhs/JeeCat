package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CategoryTree implements Serializable {

    @Serial
    private static final long serialVersionUID = 9032199443406537632L;

    private Long id;

    private Long parentId;

    private String name;

    private Long level;

    private List<CategoryTree> childs;
}
