package com.jeesite.modules.cat.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionGoodPriceDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.enums.CatActivityEnum;
import com.jeesite.modules.cat.enums.QualityStatusEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
import com.jeesite.modules.cat.model.ProductCategoryModel;
import com.jeesite.modules.cat.model.ProductScoreModel;
import com.jeesite.modules.cat.model.PromotionModel;
import com.jeesite.modules.cat.model.RateDetailTO;
import com.jeesite.modules.cat.model.ShopModel;
import com.jeesite.modules.cat.model.UnionProductModel;
import com.jeesite.modules.cat.model.UnionProductTagTO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class CatEsHelper {

    public static CarRobotCrawlerMessageIndex buildCatIndex(MaocheRobotCrawlerMessageDO item) {
        if (item == null) {
            return null;
        }
        CarRobotCrawlerMessageIndex index = new CarRobotCrawlerMessageIndex();

        index.setId(item.getIid());
        index.setFromId(item.getFromid());
        index.setToId(item.getToid());
        index.setMsg(item.getMsg());
        index.setMsgSvrId(item.getMsgsvrid());
        index.setFromType(item.getFromtype());
        index.setMsgType(item.getMsgtype());
        index.setTime(DateTimeUtils.getDate(item.getTime()));
        index.setRemark(item.getRemark());
        index.setMsgNew(item.getMsgNew());
        index.setImageUrl(item.getImageUrl());
        index.setCreateTime(item.getCreateTime());
        index.setUpdateTime(item.getUpdateTime());
        index.setProcessed(item.getProcessed());

        String affType = null;
        if (StringUtils.isNotBlank(item.getAffType())) {
            affType = item.getAffType();
        }
        index.setAffType(affType);

        return index;
    }

    public static CarAlimamaUnionProductIndex buildCatAlimamaUnionProductIndex(MaocheAlimamaUnionProductDO item,
                                                                               MaocheAlimamaUnionTitleKeywordDO titleKeywordDO,
                                                                               MaocheAlimamaUnionGoodPriceDO goodPriceDO,
                                                                               ProductCategoryModel productCategory,
                                                                               MaocheAlimamaUnionProductDetailDO productDetailDO) {
        if (item == null) {
            return null;
        }
        CarAlimamaUnionProductIndex index = new CarAlimamaUnionProductIndex();

        String origContent = item.getOrigContent();
        if (StringUtils.isBlank(origContent)) {
            return null;
        }

        // {"category_id":50023066,"category_name":"猫全价膨化粮","commission_rate":"780","commission_type":"MKT","zheg ":"199","coupon_end_time":"2023-05-31 23:59:59","coupon_id":"623a075958424f03ae22504f4181583c","coupon_info":"满500元减199元","coupon_remain_count":979,"coupon_share_url":"//uland.taobao.com/coupon/edetail?e=Ql4XbInAa4UNfLV8niU3R0P2gVhX2vi1toBzp9v5BiZmV2zm%2BMk%2FFVdWysDT55rnlYmQIiIsm%2FF8OpTKLRYOCq%2BHzVvCidxqWuKwaat7Uh3nbYfZPG6qkkkcyZqjQ7wpJKjsbMpW1LIeeaVbuGCZomROszlUNAfVS7mxWDK%2BczoHRKKHKNF5%2BkZk2sIwTazt%2FuvuI92sOE3MAQLNOhwDszQWAZ9okIn8SYnmpHKjZiRx0Tmqi9%2FF9fgP4kbY50OLcKyGdLFfF%2BPiINPY4XmmYKJ7%2BkHL3AEW&app_pvid=59590_33.5.0.196_862_1682914748489&ptl=floorId:2836;app_pvid:59590_33.5.0.196_862_1682914748489;tpp_pvid:eab8e0d2-36a6-4b66-87b7-de7b22283984&xId=oYEkT6PBApZeZ4DJmpDZMv9tU28cAR1JUJt6ig6GVKwxQxzu8tBlrepXtWZnB76o1A3Bxd2oOCuRxukC810Rlt81yegiL46aHx8CL6MS7ux&union_lens=lensId%3AMAPI%401682914748%40210500c4_0d4a_187d58928b5_3932%4001%40eyJmbG9vcklkIjoyODM2fQieie","coupon_start_fee":"500","coupon_start_time":"2023-04-29 00:00:00","coupon_total_count":1000,"include_dxjh":"false","include_mkt":"true","info_dxjh":"{}","item_description":"","item_id":"nJp20jwiMtOamMdHAMbc0tP-MwP4rkSVpzP5BjYTRb","item_url":"https://uland.taobao.com/item/edetail?id=nJp20jwiMtOamMdHAMbc0tP-MwP4rkSVpzP5BjYTRb","level_one_category_id":29,"level_one_category_name":"宠物/宠物食品及用品","nick":"Seven Point海外宠物用品店","num_iid":"nJp20jwiMtOamMdHAMbc0tP-MwP4rkSVpzP5BjYTRb","pict_url":"https://img.alicdn.com/bao/uploaded/i2/2311479995/O1CN01f8nAQy2NhlVfTS4SJ_!!2311479995.jpg","presale_deposit":"","provcity":"湖北 武汉","real_post_fee":"10.00","reserve_price":"567.00","seller_id":217440529896652827,"shop_dsr":50000,"shop_title":"Seven Point海外宠物用品店","short_title":"","small_images":{"string":["https://img.alicdn.com/i2/2311479995/O1CN01jbjjXk2NhlVTLRRCc_!!2311479995.jpg","https://img.alicdn.com/i2/2311479995/O1CN01M395yG2NhlVWfQ5c4_!!2311479995.jpg","https://img.alicdn.com/i3/2311479995/O1CN012u53Lt2NhlVanwF8S_!!2311479995.jpg","https://img.alicdn.com/i3/2311479995/O1CN012ndLq02NhlVUlVYKj_!!2311479995.jpg"]},"superior_brand":"0","title":"ACANA爱肯拿海洋盛宴猫粮加拿大进口鱼肉无谷全猫5.4kg双标防伪","tk_total_commi":"","tk_total_sales":"","url":"//s.click.taobao.com/t?e=m%3D2%26s%3DsdlrUuTuUMQcQipKwQzePOeEDrYVVa64lwnaF1WLQxlyINtkUhsv0Hv36g5e7%2Fin3jzTsN33vaJRPAWXiEIX1XKGrHNQ4%2FdmtG2MJ%2BKRa4%2FdSMASiQPvQy6EJTdg%2FQ6fSBaygToy7XnHkPJqg4kCNuI1LwQI7eU5xEOBDYNrYkeQwBFhNDe0mn3ZZgkUxZ2lClGGrxVB%2BTY4Flpez3wJ3iV7JY5HYIZNIBFw%2BH3jNKw8xpZAUhPbofN2vv8Ma0EJoAzcQG3HvinGJe8N%2FwNpGw%3D%3D&scm=1007.30148.309617.0&pvid=eab8e0d2-36a6-4b66-87b7-de7b22283984&app_pvid=59590_33.5.0.196_862_1682914748489&ptl=floorId:2836;originalFloorId:2836;pvid:eab8e0d2-36a6-4b66-87b7-de7b22283984;app_pvid:59590_33.5.0.196_862_1682914748489&xId=oYEkT6PBApZeZ4DJmpDZMv9tU28cAR1JUJt6ig6GVKwxQxzu8tBlrepXtWZnB76o1A3Bxd2oOCuRxukC810Rlt81yegiL46aHx8CL6MS7ux&union_lens=lensId%3AMAPI%401682914748%40210500c4_0d4a_187d58928b5_3932%4001%40eyJmbG9vcklkIjoyODM2fQieie","user_type":0,"volume":38,"white_image":"https://img.alicdn.com/bao/uploaded/O1CN01OqphF91meQqVAsCyp_!!6000000004979-0-yinhe.jpg","x_id":"oYEkT6PBApZeZ4DJmpDZMv9tU28cAR1JUJt6ig6GVKwxQxzu8tBlrepXtWZnB76o1A3Bxd2oOCuRxukC810Rlt81yegiL46aHx8CL6MS7ux","zk_final_price":"567.0","coupon":"199"}
        JSONObject jsonObject = JSONObject.parseObject(origContent);
        if (jsonObject == null) {
            return null;
        }
        Long volume = ProductValueHelper.getVolume(jsonObject);
        String title = ProductValueHelper.getTitle(jsonObject);

        long coupon = ProductValueHelper.getCouponAmount(jsonObject);
        long zkFinalPrice = ProductValueHelper.getZkFinalPrice(jsonObject);
        String itemDescription = ProductValueHelper.getItemDescription(jsonObject);

        Long commissionRate = NumberUtils.toLong(jsonObject.getString("commission_rate"));
        Long shopDsr = ProductValueHelper.getShopDsr(jsonObject);
        Long auditStatus = Optional.ofNullable(item.getAuditStatus()).orElse(0L);
        String categoryName = ProductValueHelper.getCategoryName(jsonObject);
        String levelOneCategoryName = ProductValueHelper.getLevelOneCategoryName(jsonObject);

        Long tkTotalSales = ProductValueHelper.geTkTotalSales(jsonObject);
        Long couponRemainCount = ProductValueHelper.couponRemainCount(jsonObject);

        String fans = null;
        Long creditLevel = null;
        // 获取粉丝数和店铺等级
        if (productDetailDO != null && StringUtils.isNotBlank(productDetailDO.getOrigContent())) {
            JSONObject productDetail = JSONObject.parseObject(productDetailDO.getOrigContent());
            if (productDetail != null && productDetail.get("data") != null) {
                JSONObject data = productDetail.getJSONObject("data");
                JSONObject seller = data.getJSONObject("seller");
                if (seller != null) {
                    fans = seller.getString("fans");
                    creditLevel = NumberUtils.toLong(seller.getString("creditLevel"));
                }

                String detailPropsProductName = ProductValueHelper.getDetailPropsProductName(productDetail);
                String detailPropsBrand = ProductValueHelper.getDetailPropsBrand(productDetail);

                index.setPropsProductName(detailPropsProductName);
                index.setPropsBrand(detailPropsBrand);
            }

            List<RateDetailTO> details = ProductValueHelper.getDetailRates(productDetail);
            index.setRates(details);
        }

        // 猫车分
        Map<String, String> calCatDsr = CalCatDsrUtils.calCatDsr(shopDsr, volume, creditLevel, fans, commissionRate);
        String catDsrTips = calCatDsr.get("tips");
        index.setCatDsr(NumberUtils.toLong(calCatDsr.get("catDsr")));
        index.setCatDsrTips(catDsrTips);
        List<String> activity = new ArrayList<>();
        if (goodPriceDO != null) {
            activity.add(CatActivityEnum.GOOD_PRICE.getActivity());
        }

        // 券后价
        Long promotionPrice = ProductValueHelper.calVeApiPromotionPrice(jsonObject);

        index.setId(item.getIid());
        index.setVolume(volume);
        index.setTitle(title);
        index.setCoupon(coupon);
        index.setReservePrice(zkFinalPrice);
        index.setItemDescription(itemDescription);
        index.setCommissionRate(commissionRate);
        index.setShopDsr(shopDsr);
        index.setAuditStatus(auditStatus);
        index.setCategoryName(categoryName);
        index.setLevelOneCategoryName(levelOneCategoryName);
        index.setActivity(activity);
        index.setTkTotalSales(tkTotalSales);
        index.setCouponRemainCount(couponRemainCount);
        index.setPromotionPrice(promotionPrice);
        index.setSaleStatus(Optional.ofNullable(item.getSaleStatus()).orElse(SaleStatusEnum.INIT.getStatus()));
        index.setDataSource(item.getDataSource());
        index.setCreateTime(item.getCreateTime().getTime());
        index.setUpdateTime(item.getUpdateTime() != null ? item.getUpdateTime().getTime() : 0L);
        index.setSyncTime(item.getSyncTime() != null ? item.getSyncTime().getTime() : 0L);
        index.setQualityStatus(item.getQualityStatus());

        if (productCategory != null) {
            // 类目
            index.setCidOnes(productCategory.getCid1s());
            index.setCidTwos(productCategory.getCid2s());
            index.setCidThirds(productCategory.getCid3s());
        }

        // 补充标签
//        String data = "{\"brand\":\"贝贝\",\"secondbrand\":\"\",\"product\":\"纸巾\",\"object\":[],\"season\":[],\"model\":[],\"material\":[],\"attribute\":[\"贝贝\",\"乳霜\"]}\n";
        String tagContent = titleKeywordDO != null ? titleKeywordDO.getContentManual() : null;
        fillTag(index, tagContent);

        return index;
    }

    public static void fillTag(CarAlimamaUnionProductIndex index, String data) {
        if (StringUtils.isBlank(data)) {
            initTag(index);
            return;
        }
        UnionProductTagTO tagTO = UnionProductHelper.convert2TagTO(data);
        index.setBrand(tagTO.getBrand());
        index.setSecondBrand(tagTO.getSecondbrand());
        index.setProduct(tagTO.getProduct());
        index.setObject(tagTO.getObject());
        index.setSeason(tagTO.getSeason());
        index.setModel(tagTO.getModel());
        index.setMaterial(tagTO.getMaterial());
        index.setAttribute(tagTO.getAttribute());
    }

    public static void initTag(CarAlimamaUnionProductIndex index) {
        index.setBrand(new ArrayList<>());
        index.setSecondBrand(new ArrayList<>());
        index.setProduct(new ArrayList<>());
        index.setObject(new ArrayList<>());
        index.setSeason(new ArrayList<>());
        index.setModel(new ArrayList<>());
        index.setMaterial(new ArrayList<>());
        index.setAttribute(new ArrayList<>());
    }


    public static CarAlimamaUnionProductIndex buildCatUnionProductIndex(UnionProductModel model) {

        if (model == null) {
            return null;
        }

        CarAlimamaUnionProductIndex index = new CarAlimamaUnionProductIndex();
        PromotionModel promotion = model.getPromotion();
        ProductCategoryModel category = model.getCategory();
        ProductScoreModel score = model.getScore();
        ShopModel shop = model.getShop();

        index.setId(model.getId());
        index.setVolume(model.getVolume());
        index.setTitle(model.getTitle());
        index.setBenefitDesc(model.getBenefitDesc());
        index.setCoupon(promotion.getCoupon());
        index.setReservePrice(model.getReservePrice());
        index.setItemDescription(model.getItemDescription());
        index.setCommissionRate(model.getCommissionRate());
        index.setShopDsr(shop.getShopDsr());
        index.setAuditStatus(model.getAuditStatus());
        index.setQualityStatus(model.getQualityStatus());

        index.setActivity(new ArrayList<>());
        index.setTkTotalSales(null);
        index.setCouponRemainCount(promotion.getCouponRemainCount());
        // 猫车分
        Map<String, String> calCatDsr = CalCatDsrUtils.calCatDsr(score.getDsrScore(), model.getVolume(), shop.getShopLevel(), shop.getFans(), model.getCommissionRate());
        String catDsrTips = calCatDsr.get("tips");
        index.setCatDsr(NumberUtils.toLong(calCatDsr.get("catDsr")));
        index.setCatDsrTips(catDsrTips);
        index.setPromotionPrice(promotion.getPromotionPrice());
        index.setSaleStatus(Optional.ofNullable(model.getSaleStatus()).orElse(SaleStatusEnum.INIT.getStatus()));
        index.setCreateTime(model.getCreateTime());
        index.setDataSource(model.getDataSource());
        index.setPropsProductName(model.getPropsProductName());
        index.setPropsBrand(model.getPropsBrand());

        // 类目
        if (category != null) {
            index.setCategoryName(category.getCategoryName());
            index.setLevelOneCategoryName(category.getLevelOneCategoryName());
            index.setCidOnes(category.getCid1s());
            index.setCidTwos(category.getCid2s());
            index.setCidThirds(category.getCid3s());
        }

        return index;
    }
}
