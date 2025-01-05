package com.jeesite.modules.cat.model;

import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
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

    public static CategoryTree convert(MaocheCategoryMappingDO category) {
        if (category == null) {
            return null;
        }

        CategoryTree tree = new CategoryTree();
        tree.setId(category.getIid());
        tree.setLevel(category.getLevel());
        tree.setParentId(category.getParentId());
        tree.setName(category.getName());
        tree.setChilds(new ArrayList<>());

        return tree;
    }
}
