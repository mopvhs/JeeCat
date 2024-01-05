package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ProductV2Content implements Serializable {

    @Serial
    private static final long serialVersionUID = 9041400461621959824L;

    private PricePromotionInfo pricePromotionInfo;

    @Data
    public static class PricePromotionInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 2510189553191550465L;

        private long finalPromotionPrice;

        // price_promotion_info.promotion_tag_list.promotion_tag_map_data.tag_name
        private List<String> promotionTagList;

        private long predictRoundingUpPrice;

        private String predictRoundingUpPriceDesc;
    }
}