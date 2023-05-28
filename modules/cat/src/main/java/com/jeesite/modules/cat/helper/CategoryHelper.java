package com.jeesite.modules.cat.helper;

import com.jeesite.modules.cat.entity.MaocheCategoryProductRelDO;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.ProductCategoryModel;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CategoryHelper {


    /**
     *
     * @param rels
     * @param categories 这个场景下，不是树结构，是全部的内容
     * @return
     */
    public static ProductCategoryModel getProductCategory(List<MaocheCategoryProductRelDO> rels, List<CategoryTree> categories) {

        if (CollectionUtils.isEmpty(categories) || CollectionUtils.isEmpty(rels)) {
            return new ProductCategoryModel();
        }

        Map<Long, CategoryTree> categoryMap = categories.stream().collect(Collectors.toMap(CategoryTree::getId, Function.identity(), (o1, o2) -> o1));
        ProductCategoryModel productCategory = new ProductCategoryModel();
        for (MaocheCategoryProductRelDO relDO : rels) {
            Long categoryId = relDO.getCategoryId();
            if (categoryId == null || categoryId <= 0) {
                continue;
            }

            // 最多3层，就不写递归了
            CategoryTree self = categoryMap.get(categoryId);
            if (self != null) {
                setCategory(productCategory, self);
                // 二级
                if (self.getParentId() != null && self.getParentId() != 0) {
                    self = categoryMap.get(self.getParentId());
                    if (self != null) {
                        setCategory(productCategory, self);
                        // 一级
                        if (self.getParentId() != null && self.getParentId() != 0) {
                            self = categoryMap.get(self.getParentId());
                            if (self != null) {
                                setCategory(productCategory, self);
                            }
                        }
                    }
                }

            }
        }

        productCategory.setCid1s(productCategory.getCid1s().stream().distinct().collect(Collectors.toList()));
        productCategory.setCid2s(productCategory.getCid2s().stream().distinct().collect(Collectors.toList()));
        productCategory.setCid3s(productCategory.getCid3s().stream().distinct().collect(Collectors.toList()));

        return productCategory;
    }

    public static void setCategory(ProductCategoryModel productCategory, CategoryTree category) {
        Long level = category.getLevel();
        Long categoryId = category.getId();

        if (level == 1) {
            productCategory.getCid1s().add(categoryId);
        } else if (level == 2) {
            productCategory.getCid2s().add(categoryId);
        } else if (level == 3) {
            productCategory.getCid3s().add(categoryId);
        }
    }
}
