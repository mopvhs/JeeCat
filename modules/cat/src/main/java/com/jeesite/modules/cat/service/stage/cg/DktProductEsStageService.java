package com.jeesite.modules.cat.service.stage.cg;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dtk.fetch.response.DtkGoodsListResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.entity.MaocheCategoryProductRelDO;
import com.jeesite.modules.cat.entity.MaocheDataokeProductDO;
import com.jeesite.modules.cat.helper.CatEsHelper;
import com.jeesite.modules.cat.helper.CategoryHelper;
import com.jeesite.modules.cat.helper.ProductValueHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.ProductCategoryModel;
import com.jeesite.modules.cat.model.ProductScoreModel;
import com.jeesite.modules.cat.model.PromotionModel;
import com.jeesite.modules.cat.model.RateDetailTO;
import com.jeesite.modules.cat.model.ShopModel;
import com.jeesite.modules.cat.model.UnionProductModel;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 大淘客
 */
@Component
public class DktProductEsStageService extends AbstractProductEsStage<ProductEsContext, UnionProductModel> {

    @Override
    public List<String> getSources() {
        return Collections.singletonList("dataoke");
    }

    @Override
    public UnionProductModel convert(ProductEsContext context) {

        return doConvert(this::buildModel, context);
    }

    private UnionProductModel buildModel(ProductEsContext context) {
        MaocheDataokeProductDO daTaoKeProduct = context.getDaTaoKeProduct();

        if (daTaoKeProduct == null) {
            return null;
        }

        String origContent = daTaoKeProduct.getOrigContent();
        if (StringUtils.isBlank(origContent)) {
            return null;
        }

        // {"category_id":50023066,"category_name":"猫全价膨化粮","commission_rate":"780","commission_type":"mMKT","zheg ":"199","coupon_end_time":"2023-05-31 23:59:59","coupon_id":"623a075958424f03ae22504f4181583c","coupon_info":"满500元减199元","coupon_remain_count":979,"coupon_share_url":"//uland.taobao.com/coupon/edetail?e=Ql4XbInAa4UNfLV8niU3R0P2gVhX2vi1toBzp9v5BiZmV2zm%2BMk%2FFVdWysDT55rnlYmQIiIsm%2FF8OpTKLRYOCq%2BHzVvCidxqWuKwaat7Uh3nbYfZPG6qkkkcyZqjQ7wpJKjsbMpW1LIeeaVbuGCZomROszlUNAfVS7mxWDK%2BczoHRKKHKNF5%2BkZk2sIwTazt%2FuvuI92sOE3MAQLNOhwDszQWAZ9okIn8SYnmpHKjZiRx0Tmqi9%2FF9fgP4kbY50OLcKyGdLFfF%2BPiINPY4XmmYKJ7%2BkHL3AEW&app_pvid=59590_33.5.0.196_862_1682914748489&ptl=floorId:2836;app_pvid:59590_33.5.0.196_862_1682914748489;tpp_pvid:eab8e0d2-36a6-4b66-87b7-de7b22283984&xId=oYEkT6PBApZeZ4DJmpDZMv9tU28cAR1JUJt6ig6GVKwxQxzu8tBlrepXtWZnB76o1A3Bxd2oOCuRxukC810Rlt81yegiL46aHx8CL6MS7ux&union_lens=lensId%3AMAPI%401682914748%40210500c4_0d4a_187d58928b5_3932%4001%40eyJmbG9vcklkIjoyODM2fQieie","coupon_start_fee":"500","coupon_start_time":"2023-04-29 00:00:00","coupon_total_count":1000,"include_dxjh":"false","include_mkt":"true","info_dxjh":"{}","item_description":"","item_id":"nJp20jwiMtOamMdHAMbc0tP-MwP4rkSVpzP5BjYTRb","item_url":"https://uland.taobao.com/item/edetail?id=nJp20jwiMtOamMdHAMbc0tP-MwP4rkSVpzP5BjYTRb","level_one_category_id":29,"level_one_category_name":"宠物/宠物食品及用品","nick":"Seven Point海外宠物用品店","num_iid":"nJp20jwiMtOamMdHAMbc0tP-MwP4rkSVpzP5BjYTRb","pict_url":"https://img.alicdn.com/bao/uploaded/i2/2311479995/O1CN01f8nAQy2NhlVfTS4SJ_!!2311479995.jpg","presale_deposit":"","provcity":"湖北 武汉","real_post_fee":"10.00","reserve_price":"567.00","seller_id":217440529896652827,"shop_dsr":50000,"shop_title":"Seven Point海外宠物用品店","short_title":"","small_images":{"string":["https://img.alicdn.com/i2/2311479995/O1CN01jbjjXk2NhlVTLRRCc_!!2311479995.jpg","https://img.alicdn.com/i2/2311479995/O1CN01M395yG2NhlVWfQ5c4_!!2311479995.jpg","https://img.alicdn.com/i3/2311479995/O1CN012u53Lt2NhlVanwF8S_!!2311479995.jpg","https://img.alicdn.com/i3/2311479995/O1CN012ndLq02NhlVUlVYKj_!!2311479995.jpg"]},"superior_brand":"0","title":"ACANA爱肯拿海洋盛宴猫粮加拿大进口鱼肉无谷全猫5.4kg双标防伪","tk_total_commi":"","tk_total_sales":"","url":"//s.click.taobao.com/t?e=m%3D2%26s%3DsdlrUuTuUMQcQipKwQzePOeEDrYVVa64lwnaF1WLQxlyINtkUhsv0Hv36g5e7%2Fin3jzTsN33vaJRPAWXiEIX1XKGrHNQ4%2FdmtG2MJ%2BKRa4%2FdSMASiQPvQy6EJTdg%2FQ6fSBaygToy7XnHkPJqg4kCNuI1LwQI7eU5xEOBDYNrYkeQwBFhNDe0mn3ZZgkUxZ2lClGGrxVB%2BTY4Flpez3wJ3iV7JY5HYIZNIBFw%2BH3jNKw8xpZAUhPbofN2vv8Ma0EJoAzcQG3HvinGJe8N%2FwNpGw%3D%3D&scm=1007.30148.309617.0&pvid=eab8e0d2-36a6-4b66-87b7-de7b22283984&app_pvid=59590_33.5.0.196_862_1682914748489&ptl=floorId:2836;originalFloorId:2836;pvid:eab8e0d2-36a6-4b66-87b7-de7b22283984;app_pvid:59590_33.5.0.196_862_1682914748489&xId=oYEkT6PBApZeZ4DJmpDZMv9tU28cAR1JUJt6ig6GVKwxQxzu8tBlrepXtWZnB76o1A3Bxd2oOCuRxukC810Rlt81yegiL46aHx8CL6MS7ux&union_lens=lensId%3AMAPI%401682914748%40210500c4_0d4a_187d58928b5_3932%4001%40eyJmbG9vcklkIjoyODM2fQieie","user_type":0,"volume":38,"white_image":"https://img.alicdn.com/bao/uploaded/O1CN01OqphF91meQqVAsCyp_!!6000000004979-0-yinhe.jpg","x_id":"oYEkT6PBApZeZ4DJmpDZMv9tU28cAR1JUJt6ig6GVKwxQxzu8tBlrepXtWZnB76o1A3Bxd2oOCuRxukC810Rlt81yegiL46aHx8CL6MS7ux","zk_final_price":"567.0","coupon":"199"}
        DtkGoodsListResponse.ItemInfo itemInfo = JsonUtils.toReferenceType(origContent, new TypeReference<DtkGoodsListResponse.ItemInfo>() {
        });

        if (itemInfo == null) {
            return null;
        }

        MaocheAlimamaUnionProductDO unionProductDO = context.getItem();
        UnionProductModel model = new UnionProductModel();

        Context innerContext = new Context();
        innerContext.setItemInfo(itemInfo);
        innerContext.setUnionProductContent(JSONObject.parseObject(unionProductDO.getOrigContent()));
        innerContext.setModel(model);

        // 商品基础信息
        fillProductBaseInfo(context, innerContext);

        // 优惠信息
        fillProductPromotionInfo(context, innerContext);

        // 店铺信息
        fillProductShopInfo(context, innerContext);

        // 分数信息
        fillProductScoreInfo(context, innerContext);

        // 类目
        fillCategoryModel(context, innerContext);

        // 属性
        fillPropsModel(context, innerContext);

        return model;
    }

    // 基础数据
    private void fillProductBaseInfo(ProductEsContext context, Context innerContext) {

        MaocheAlimamaUnionProductDO item = context.getItem();
        MaocheDataokeProductDO daTaoKeProduct = context.getDaTaoKeProduct();

        UnionProductModel model = innerContext.getModel();
        DtkGoodsListResponse.ItemInfo itemInfo = innerContext.getItemInfo();
        JSONObject productContent = innerContext.getUnionProductContent();

        // 商品原价
        long originalPrice = daTaoKeProduct.getOriginalPrice();

        Long reservePrice = new BigDecimal(Optional.ofNullable(productContent.getString("zk_final_price")).orElse("0")).multiply(new BigDecimal("100")).longValue();
        Long commissionRate = NumberUtils.toLong(productContent.getString("commission_rate"));

        Long volume = productContent.getLong("volume");
        String itemDescription = productContent.getString("item_description");
        Long tkTotalSales = NumberUtils.toLong(productContent.getString("tk_total_sales"));

        model.setId(item.getUiid());
        model.setTitle(itemInfo.getTitle());
        model.setBenefitDesc(itemInfo.getDesc());
        model.setItemDescription(itemDescription);
        // 商品主图
        model.setMainPic(itemInfo.getMainPic());
        model.setReservePrice(reservePrice);
        model.setOriginalPrice(originalPrice);
        model.setCommissionRate(commissionRate);
        model.setVolume(volume);
        model.setTkTotalSales(tkTotalSales);

        model.setCreateTime(item.getCreateTime().getTime());
        model.setItemId(item.getItemId());
        model.setItemIdSuffix(item.getItemIdSuffix());

        // 状态
        model.setSaleStatus(item.getSaleStatus());
        if (item.getSaleStatusDate() != null) {
            model.setSaleStatusTime(item.getSaleStatusDate().getTime());
        }
        model.setAuditStatus(item.getAuditStatus());
        model.setDataSource(item.getDataSource());

        model.setSyncTime(item.getSyncTime() != null ? item.getSyncTime().getTime() : 0L);
        model.setUpdateTime(item.getUpdateTime() != null ? item.getUpdateTime().getTime() : 0L);
    }

    // 基础数据
    private void fillProductPromotionInfo(ProductEsContext context, Context innerContext) {
        PromotionModel promotionModel = new PromotionModel();
        UnionProductModel model = innerContext.getModel();
        JSONObject productContent = innerContext.getUnionProductContent();

        // 优惠券金额

        long coupon = ProductValueHelper.getCouponAmount(productContent);
        // 优惠券数量
        Long couponRemainCount = NumberUtils.toLong(productContent.getString("coupon_remain_count"));
        // 券后价
        Long promotionPrice = ProductValueHelper.calVeApiPromotionPrice(productContent);

        promotionModel.setCoupon(coupon);
        promotionModel.setPromotionPrice(promotionPrice);
        promotionModel.setCouponRemainCount(couponRemainCount);

        model.setPromotion(promotionModel);
    }

    // 基础数据
    private void fillProductShopInfo(ProductEsContext context, Context innerContext) {
        UnionProductModel model = innerContext.getModel();
        DtkGoodsListResponse.ItemInfo itemInfo = innerContext.getItemInfo();
        JSONObject productContent = innerContext.getUnionProductContent();

        ShopModel shopModel = new ShopModel();
        String brandName = itemInfo.getBrandName();;
        String shopName = itemInfo.getShopName();
        Long shopLevel = itemInfo.getShopLevel() != null ? itemInfo.getShopLevel().longValue() : 0L;
        MaocheAlimamaUnionProductDetailDO productDetailDO = context.getProductDetailDO();

        Long shopDsr = NumberUtils.toLong(productContent.getString("shop_dsr"));

        String fans = null;
        // 获取粉丝数和店铺等级
        if (productDetailDO != null && StringUtils.isNotBlank(productDetailDO.getOrigContent())) {
            JSONObject productDetail = JSONObject.parseObject(productDetailDO.getOrigContent());
            if (productDetail != null && productDetail.get("data") != null) {
                JSONObject data = productDetail.getJSONObject("data");
                JSONObject seller = data.getJSONObject("seller");
                if (seller != null) {
                    fans = seller.getString("fans");
                }
            }
        }

        shopModel.setFans(fans);
        shopModel.setShopDsr(shopDsr);
        shopModel.setBrandName(brandName);
        shopModel.setShopName(shopName);
        shopModel.setShopLevel(shopLevel);

        model.setShop(shopModel);
    }

    private void fillProductScoreInfo(ProductEsContext context, Context innerContext) {
        UnionProductModel model = innerContext.getModel();
        DtkGoodsListResponse.ItemInfo itemInfo = innerContext.getItemInfo();

        ProductScoreModel scoreModel = new ProductScoreModel();

        scoreModel.setDescScore(ProductScoreModel.formatScore(itemInfo.getDescScore()));
        scoreModel.setDsrScore(ProductScoreModel.formatScore(itemInfo.getDsrScore()));
        scoreModel.setDsrPercent(ProductScoreModel.formatScore(itemInfo.getDsrPercent()));
        scoreModel.setShipScore(ProductScoreModel.formatScore(itemInfo.getShipScore()));
        scoreModel.setShipPercent(ProductScoreModel.formatScore(itemInfo.getShipPercent()));
        scoreModel.setServiceScore(ProductScoreModel.formatScore(itemInfo.getServiceScore()));
        scoreModel.setServicePercent(ProductScoreModel.formatScore(itemInfo.getServicePercent()));
        scoreModel.setHotPush(ProductScoreModel.formatScore(itemInfo.getHotPush()));

        model.setScore(scoreModel);
    }

    private void fillCategoryModel(ProductEsContext context, Context innerContext) {
        UnionProductModel model = innerContext.getModel();
        MaocheAlimamaUnionProductDO item = context.getItem();
        List<MaocheCategoryProductRelDO> categoryRelList = context.getCategoryRelList();
//        List<CategoryTree> categoryTrees = context.getCategoryTrees();

        ProductCategoryModel productCategory = CategoryHelper.getRelProductCategory(categoryRelList, new ArrayList<>());

        String categoryName = ProductValueHelper.getCategoryName(innerContext.getUnionProductContent());
        String levelOneCategoryName = ProductValueHelper.getLevelOneCategoryName(innerContext.unionProductContent);

        productCategory.setCategoryName(categoryName);
        productCategory.setLevelOneCategoryName(levelOneCategoryName);

        model.setCategory(productCategory);

    }

    private void fillPropsModel(ProductEsContext context, Context innerContext) {
        UnionProductModel model = innerContext.getModel();
        MaocheAlimamaUnionProductDetailDO productDetailDO = context.getProductDetailDO();
        if (productDetailDO == null) {
            return;
        }
        String origContent = productDetailDO.getOrigContent();
        if (StringUtils.isBlank(origContent)) {
            return;
        }

        try {
            JSONObject jsonObject = JSONObject.parseObject(origContent);
            String detailPropsProductName = ProductValueHelper.getDetailPropsProductName(jsonObject);
            String detailPropsBrand = ProductValueHelper.getDetailPropsBrand(jsonObject);

            model.setPropsBrand(detailPropsBrand);
            model.setPropsProductName(detailPropsProductName);

            List<RateDetailTO> details = ProductValueHelper.getDetailRates(jsonObject);
            model.setRates(details);
        } catch (Exception e) {

        }

    }

    /**
     * 中间态的上下文
     */
    @Data
    private static class Context implements Serializable {

        @Serial
        private static final long serialVersionUID = -6362560810533686128L;

        private DtkGoodsListResponse.ItemInfo itemInfo;

        private JSONObject unionProductContent;

        private UnionProductModel model;
    }

    /**
     * 人民币 元转分
     * @param price
     * @return
     */
    private static long formatPrice(Object price) {
        if (Objects.isNull(price)) {
            return 0;
        }

        return new BigDecimal(String.valueOf(price)).multiply(new BigDecimal("100")).longValue();
    }
}
