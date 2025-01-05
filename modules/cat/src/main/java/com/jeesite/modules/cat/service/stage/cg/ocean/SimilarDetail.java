package com.jeesite.modules.cat.service.stage.cg.ocean;

import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.helper.PriceHelper;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;

@Data
public class SimilarDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 9018294627317925391L;

    private String resourceId;

    private long price;

    private String shopId;

    private String shopName;

    // 部分字段，京东使用
    private long commissionInfoStartTime;

    private long commissionInfoEndTime;

    private long commission;

    @Override
    public String toString() {
        return "SimilarDetail{" +
                "resourceId='" + resourceId + '\'' +
                ", price=" + price +
                ", shopId='" + shopId + '\'' +
                ", shopName='" + shopName + '\'' +
                ", commissionInfoStartTime=" + commissionInfoStartTime +
                ", commissionInfoEndTime=" + commissionInfoEndTime +
                ", commission=" + commission +
                '}';
    }

    public static SimilarDetail convertProduct(JdUnionIdPromotion promotion) {
        if (promotion == null) {
            return null;
        }
        if (StringUtils.isBlank(promotion.getSkuId())) {
            return null;
        }

        long price = 0;
        long shopId = 0;
        String shopName = "";
        long commissionInfoStartTime = 0;
        long commissionInfoEndTime = 0;
        long commission = 0;

        JdUnionIdPromotion.PriceInfo priceInfo = promotion.getPriceInfo();
        if (priceInfo != null) {
            price = BigDecimal.valueOf(promotion.getPriceInfo().getPrice()).multiply(new BigDecimal(100)).longValue();
        }

        JdUnionIdPromotion.CommissionInfo commissionInfo = promotion.getCommissionInfo();
        if (commissionInfo != null) {
            commission = BigDecimal.valueOf(commissionInfo.getCommission()).multiply(new BigDecimal(100)).longValue();
            commissionInfoStartTime = Optional.ofNullable(commissionInfo.getStartTime()).orElse(0L);
            commissionInfoEndTime = Optional.ofNullable(commissionInfo.getEndTime()).orElse(0L);
        }

        JdUnionIdPromotion.ShopInfo shopInfo = promotion.getShopInfo();
        if (shopInfo != null) {
            shopId = Optional.ofNullable(shopInfo.getShopId()).orElse(0L);
            shopName = Optional.ofNullable(shopInfo.getShopName()).orElse("");
        }

        SimilarDetail detail = new SimilarDetail();
        detail.setResourceId(String.valueOf(promotion.getSkuId()));
        detail.setPrice(price);
        detail.setShopId(String.valueOf(shopId));
        detail.setShopName(shopName);
        detail.setCommission(commission);
        detail.setCommissionInfoStartTime(commissionInfoStartTime);
        detail.setCommissionInfoEndTime(commissionInfoEndTime);

        return detail;
    }

    public static SimilarDetail convertProduct(CommandResponseV2 promotion) {
        if (promotion == null) {
            return null;
        }

        long price = 0;
        String shopId = "0";
        String shopName = "";
        long commissionInfoStartTime = 0;
        long commissionInfoEndTime = 0;
        long commission = 0;

        String title = "";
        // title _ categoryId _ sellerId
        String riRule = "%s_%s_%s";
        String categoryId = "0";
        CommandResponseV2.ItemBasicInfo itemBasicInfo = promotion.getItemBasicInfo();
        if (itemBasicInfo == null) {
            return null;
        }

        shopId = itemBasicInfo.getSellerId();
        shopName = itemBasicInfo.getShopTitle();
        categoryId = itemBasicInfo.getCategoryId();
        title = itemBasicInfo.getTitle();

        CommandResponseV2.PricePromotionInfo pricePromotionInfo = promotion.getPricePromotionInfo();
        if (pricePromotionInfo != null) {
            price = PriceHelper.formatPrice(pricePromotionInfo.getFinalPromotionPrice());

        }

        commission = new BigDecimal(promotion.getCommissionRate()).multiply(new BigDecimal(100)).longValue();

        String id = Md5Utils.md5(String.format(riRule, title, categoryId, shopId));


        SimilarDetail detail = new SimilarDetail();
        detail.setResourceId(id);
        detail.setPrice(price);
        detail.setShopId(shopId);
        detail.setShopName(shopName);
        detail.setCommission(commission);
        detail.setCommissionInfoStartTime(commissionInfoStartTime);
        detail.setCommissionInfoEndTime(commissionInfoEndTime);

        return detail;
    }
}
