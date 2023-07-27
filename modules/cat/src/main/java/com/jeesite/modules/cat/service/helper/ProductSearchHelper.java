package com.jeesite.modules.cat.service.helper;

import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.model.CatUnionProductCondition;

public class ProductSearchHelper {


    public static CatUnionProductCondition buildNineSearchCondition() {

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setLevelOneCategoryName("宠物/宠物食品及用品");
        condition.setAuditStatus(AuditStatusEnum.PASS.getStatus());
        condition.setSaleStatus(SaleStatusEnum.ON_SHELF.getStatus());
        condition.setGteShopDsr(48000);
        // 5%
//        condition.setGteCommissionRate(500L);
//        condition.setStartCatDsr(44000L);
        condition.setGteCouponRemainCount(1L);
        condition.setGteVolume(100L);
        condition.setLtePromotionPrice(990L);

        condition.setHadRates(true);

        return condition;
    }

}
