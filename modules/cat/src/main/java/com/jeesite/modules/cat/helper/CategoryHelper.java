package com.jeesite.modules.cat.helper;

import com.jeesite.modules.cat.entity.MaocheCategoryProductRelDO;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.ProductCategoryModel;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CategoryHelper {

    @Getter
    public enum CatRootCategoryEnum {
        CAT_HEALTH_CARE(1L, "驱虫保健"),
        CAT_LITTER(23L, "猫砂"),
        CAT_SUPPLIES(25L, "猫咪用品"),
        CAT_FOOD(50L, "猫粮"),
        ;

        CatRootCategoryEnum(Long cid, String name) {
            this.cid = cid;
            this.name = name;
        }

        private Long cid;

        private String name;

        public CategoryTree buildRoot() {

            CategoryTree root = new CategoryTree();

            root.setId(cid);
            root.setName(name);
            root.setLevel(1L);
            root.setParentId(0L);

            return root;
        }


    }

    // 猫车主目
    public static Map<Long, String> catRootCategoryMap = new HashMap<>();
    /**
     * 1,0,驱虫保健,1
     * 23,0,猫砂,1
     * 25,0,猫咪用品,1
     * 50,0,猫粮,1
     */
    static {
        catRootCategoryMap.put(CatRootCategoryEnum.CAT_HEALTH_CARE.getCid(), "驱虫保健");
        catRootCategoryMap.put(CatRootCategoryEnum.CAT_LITTER.getCid(), "猫砂");
        catRootCategoryMap.put(CatRootCategoryEnum.CAT_SUPPLIES.getCid(), "猫咪用品");
        catRootCategoryMap.put(CatRootCategoryEnum.CAT_FOOD.getCid(), "猫粮");
    }

    /**
     *
     * @param rels
     * @param categories 这个场景下，不是树结构，是全部的内容
     * @return
     */
    public static ProductCategoryModel getRelProductCategory(List<MaocheCategoryProductRelDO> rels, List<CategoryTree> categories) {

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
