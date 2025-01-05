package com.jeesite.modules.cat.service.cg.brandlib.dto;

import com.jeesite.modules.cat.model.CategoryTree;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LibCategoryDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 2331973823031066654L;

    private Long id;

    private String name;

    private int site;

    private CategoryTree tree;

    public static LibCategoryDetail convert(CategoryTree root) {
        if (root == null) {
            return null;
        }

        LibCategoryDetail detail = new LibCategoryDetail();

        detail.setId(root.getId());
        detail.setSite(0);
        detail.setName(root.getName());
        detail.setTree(root);

        return detail;
    }
}
