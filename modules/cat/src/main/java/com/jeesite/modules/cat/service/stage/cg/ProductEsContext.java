package com.jeesite.modules.cat.service.stage.cg;

import com.jeesite.modules.cat.entity.MaocheAlimamaUnionGoodPriceDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.entity.MaocheCategoryProductRelDO;
import com.jeesite.modules.cat.entity.MaocheDataokeProductDO;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.ProductCategoryModel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ProductEsContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1871125324021832521L;

    private MaocheAlimamaUnionProductDO item;

    private MaocheAlimamaUnionTitleKeywordDO titleKeywordDO;

    private MaocheDataokeProductDO daTaoKeProduct;

    // 获取全部类目
    private List<CategoryTree> categoryTrees;

    private MaocheAlimamaUnionGoodPriceDO goodPriceDO;

    private List<MaocheCategoryProductRelDO> categoryRelList;

    private MaocheAlimamaUnionProductDetailDO productDetailDO;

}
