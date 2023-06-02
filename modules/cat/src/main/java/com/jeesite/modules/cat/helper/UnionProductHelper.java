package com.jeesite.modules.cat.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionGoodPriceDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.entity.MaocheCategoryDO;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.PromotionTagTO;
import com.jeesite.modules.cat.model.RateDetailTO;
import com.jeesite.modules.cat.model.RateTO;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.model.UnionProductTagTO;
import com.jeesite.modules.cat.model.keytitle.UnionProductTagModel;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UnionProductHelper {


    public static List<UnionProductTO> convertUnionProduct(List<CarAlimamaUnionProductIndex> indexList,
                                                           List<MaocheAlimamaUnionProductDO> productDOs,
                                                           List<MaocheAlimamaUnionTitleKeywordDO> keywordDOs,
                                                           List<MaocheAlimamaUnionGoodPriceDO> unionGoodPriceDOs,
                                                           List<MaocheAlimamaUnionProductDetailDO> productDetailDOs,
                                                           List<MaocheCategoryDO> categoryDOs) {

        if (CollectionUtils.isEmpty(indexList) || CollectionUtils.isEmpty(productDOs)) {
            return new ArrayList<>();
        }

        if (CollectionUtils.isEmpty(keywordDOs)) {
            keywordDOs = new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(unionGoodPriceDOs)) {
            unionGoodPriceDOs = new ArrayList<>();
        }
        Map<Long, CarAlimamaUnionProductIndex> indexMap = indexList.stream().collect(Collectors.toMap(CarAlimamaUnionProductIndex::getId, Function.identity(), (o1, o2) -> o1));
        Map<Long, MaocheAlimamaUnionProductDO> productDOMap = productDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductDO::getIid, Function.identity(), (o1, o2) -> o1));
        Map<String, MaocheAlimamaUnionTitleKeywordDO> keywordMap = keywordDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionTitleKeywordDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));
        Map<String, MaocheAlimamaUnionGoodPriceDO> unionGoodPriceMap = unionGoodPriceDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionGoodPriceDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));
        Map<String, MaocheAlimamaUnionProductDetailDO> productDetailMap = productDetailDOs.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductDetailDO::getItemIdSuffix, Function.identity(), (o1, o2) -> o1));
        Map<Long, MaocheCategoryDO> customCategoryMap = categoryDOs.stream().collect(Collectors.toMap(MaocheCategoryDO::getIid, Function.identity(), (o1, o2) -> o1));

        /**
         *      {
         *             "shareCommand":"分享口令",
         *             "title":"商品标题",
         *             "price": 10000, // "售价，单位分  展示除以100"
         *             "activity":"活动 有好价",
         *             "commissionRate": 975, // 佣金率 展示除以100,
         *             "commission": 1131, // "佣金，单位分  展示除以100",
         *             "volume":231, // 销量
         *             "priceAdvantage":"价格优势",
         *             "goodRate": 123, // 好评率
         *             "productAdvantage":"价格优势"
         *         }
         */

        List<UnionProductTO> products = new ArrayList<>();
        for (CarAlimamaUnionProductIndex index : indexList) {
            MaocheAlimamaUnionProductDO productDO = productDOMap.get(index.getId());
            if (productDO == null || StringUtils.isBlank(productDO.getOrigContent())) {
                continue;
            }
            // {"category_id":50023066,"category_name":"猫全价膨化粮","commission_rate":"780","commission_type":"MKT","zheg ":"199","coupon_end_time":"2023-05-31 23:59:59","coupon_id":"623a075958424f03ae22504f4181583c","coupon_info":"满500元减199元","coupon_remain_count":979,"coupon_share_url":"//uland.taobao.com/coupon/edetail?e=Ql4XbInAa4UNfLV8niU3R0P2gVhX2vi1toBzp9v5BiZmV2zm%2BMk%2FFVdWysDT55rnlYmQIiIsm%2FF8OpTKLRYOCq%2BHzVvCidxqWuKwaat7Uh3nbYfZPG6qkkkcyZqjQ7wpJKjsbMpW1LIeeaVbuGCZomROszlUNAfVS7mxWDK%2BczoHRKKHKNF5%2BkZk2sIwTazt%2FuvuI92sOE3MAQLNOhwDszQWAZ9okIn8SYnmpHKjZiRx0Tmqi9%2FF9fgP4kbY50OLcKyGdLFfF%2BPiINPY4XmmYKJ7%2BkHL3AEW&app_pvid=59590_33.5.0.196_862_1682914748489&ptl=floorId:2836;app_pvid:59590_33.5.0.196_862_1682914748489;tpp_pvid:eab8e0d2-36a6-4b66-87b7-de7b22283984&xId=oYEkT6PBApZeZ4DJmpDZMv9tU28cAR1JUJt6ig6GVKwxQxzu8tBlrepXtWZnB76o1A3Bxd2oOCuRxukC810Rlt81yegiL46aHx8CL6MS7ux&union_lens=lensId%3AMAPI%401682914748%40210500c4_0d4a_187d58928b5_3932%4001%40eyJmbG9vcklkIjoyODM2fQieie","coupon_start_fee":"500","coupon_start_time":"2023-04-29 00:00:00","coupon_total_count":1000,"include_dxjh":"false","include_mkt":"true","info_dxjh":"{}","item_description":"","item_id":"nJp20jwiMtOamMdHAMbc0tP-MwP4rkSVpzP5BjYTRb","item_url":"https://uland.taobao.com/item/edetail?id=nJp20jwiMtOamMdHAMbc0tP-MwP4rkSVpzP5BjYTRb","level_one_category_id":29,"level_one_category_name":"宠物/宠物食品及用品","nick":"Seven Point海外宠物用品店","num_iid":"nJp20jwiMtOamMdHAMbc0tP-MwP4rkSVpzP5BjYTRb","pict_url":"https://img.alicdn.com/bao/uploaded/i2/2311479995/O1CN01f8nAQy2NhlVfTS4SJ_!!2311479995.jpg","presale_deposit":"","provcity":"湖北 武汉","real_post_fee":"10.00","reserve_price":"567.00","seller_id":217440529896652827,"shop_dsr":50000,"shop_title":"Seven Point海外宠物用品店","short_title":"","small_images":{"string":["https://img.alicdn.com/i2/2311479995/O1CN01jbjjXk2NhlVTLRRCc_!!2311479995.jpg","https://img.alicdn.com/i2/2311479995/O1CN01M395yG2NhlVWfQ5c4_!!2311479995.jpg","https://img.alicdn.com/i3/2311479995/O1CN012u53Lt2NhlVanwF8S_!!2311479995.jpg","https://img.alicdn.com/i3/2311479995/O1CN012ndLq02NhlVUlVYKj_!!2311479995.jpg"]},"superior_brand":"0","title":"ACANA爱肯拿海洋盛宴猫粮加拿大进口鱼肉无谷全猫5.4kg双标防伪","tk_total_commi":"","tk_total_sales":"","url":"//s.click.taobao.com/t?e=m%3D2%26s%3DsdlrUuTuUMQcQipKwQzePOeEDrYVVa64lwnaF1WLQxlyINtkUhsv0Hv36g5e7%2Fin3jzTsN33vaJRPAWXiEIX1XKGrHNQ4%2FdmtG2MJ%2BKRa4%2FdSMASiQPvQy6EJTdg%2FQ6fSBaygToy7XnHkPJqg4kCNuI1LwQI7eU5xEOBDYNrYkeQwBFhNDe0mn3ZZgkUxZ2lClGGrxVB%2BTY4Flpez3wJ3iV7JY5HYIZNIBFw%2BH3jNKw8xpZAUhPbofN2vv8Ma0EJoAzcQG3HvinGJe8N%2FwNpGw%3D%3D&scm=1007.30148.309617.0&pvid=eab8e0d2-36a6-4b66-87b7-de7b22283984&app_pvid=59590_33.5.0.196_862_1682914748489&ptl=floorId:2836;originalFloorId:2836;pvid:eab8e0d2-36a6-4b66-87b7-de7b22283984;app_pvid:59590_33.5.0.196_862_1682914748489&xId=oYEkT6PBApZeZ4DJmpDZMv9tU28cAR1JUJt6ig6GVKwxQxzu8tBlrepXtWZnB76o1A3Bxd2oOCuRxukC810Rlt81yegiL46aHx8CL6MS7ux&union_lens=lensId%3AMAPI%401682914748%40210500c4_0d4a_187d58928b5_3932%4001%40eyJmbG9vcklkIjoyODM2fQieie","user_type":0,"volume":38,"white_image":"https://img.alicdn.com/bao/uploaded/O1CN01OqphF91meQqVAsCyp_!!6000000004979-0-yinhe.jpg","x_id":"oYEkT6PBApZeZ4DJmpDZMv9tU28cAR1JUJt6ig6GVKwxQxzu8tBlrepXtWZnB76o1A3Bxd2oOCuRxukC810Rlt81yegiL46aHx8CL6MS7ux","zk_final_price":"567.0","coupon":"199"}
            JSONObject jsonObject = JSONObject.parseObject(productDO.getOrigContent());
            if (jsonObject == null) {
                continue;
            }

            UnionProductTO product = new UnionProductTO();
            product.setId(index.getId());
            product.setItemId(productDO.getItemId());
            // todo 不同的平台执行不同的流行
            product.setShareCommand("");
            product.setTitle(index.getTitle());
            product.setCategoryName(index.getCategoryName());
            product.setReservePrice(index.getReservePrice());
            product.setCommissionRate(index.getCommissionRate());

            product.setTkTotalSales(index.getTkTotalSales());
            product.setCreateDate(productDO.getCreateTime());
            product.setUpdateDate(productDO.getUpdateTime());
            // 店铺Dsr
            product.setShopDsr(index.getShopDsr());
            // 猫车分
            product.setCatDsr(index.getCatDsr());
            // 店铺名称
            product.setShopName(jsonObject.getString("shop_title"));
            // 设置优惠券数量信息
            product.setCoupon(index.getCoupon());
            product.setCouponRemainCount(index.getCouponRemainCount());
            product.setCouponTotalCount(jsonObject.getLong("coupon_total_count"));
            product.setPromotionPrice(index.getPromotionPrice());


            long commission = -999999999L;
            if (index.getCommissionRate() != null && index.getCommissionRate() > 0) {
                commission = new BigDecimal(String.valueOf(index.getCommissionRate())).multiply(new BigDecimal(String.valueOf(index.getReservePrice()))).divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP).longValue();
            }
            product.setCommission(commission);
            product.setVolume(index.getVolume());
            product.setAuditStatus(productDO.getAuditStatus());
            product.setGoodRate(-1L);

            // 上架状态
            product.setSaleStatus(productDO.getSaleStatus());
            product.setOnShelfDate(productDO.getSaleStatusDate());

            // 评论信息
            fillItemRateDetail(product, productDetailMap.get(productDO.getItemIdSuffix()));

            fillItemAdvantage(product, unionGoodPriceMap.get(productDO.getItemIdSuffix()));
            // 商品所属
            if (CollectionUtils.isNotEmpty(product.getActivity())) {
                product.setBelongTo(product.getActivity());
            } else {
                product.setBelongTo(Collections.singletonList("超搜"));
            }

            List<String> cidOneNames = new ArrayList<>();
            // 获取自定义类目
            if (CollectionUtils.isNotEmpty(index.getCidOnes())) {
                for (Long cid : index.getCidOnes()) {
                    MaocheCategoryDO maocheCategoryDO = customCategoryMap.get(cid);
                    if (maocheCategoryDO == null) {
                        continue;
                    }
                    cidOneNames.add(maocheCategoryDO.getName());
                }
            }
            product.setCidOneNames(cidOneNames);

            product.setImgUrl(getProductImage(jsonObject));

            UnionProductTagTO unionProductTagTO = convert2TagTO(keywordMap.get(productDO.getItemIdSuffix()));
            product.setTag(unionProductTagTO);

            products.add(product);
        }


        return products;
    }

    private static String getProductImage(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        String smallImage = getSmallImage(jsonObject.get("small_images"));
        if (StringUtils.isNotBlank(smallImage)) {
            return smallImage;
        }
        smallImage = jsonObject.getString("pict_url");
        if (StringUtils.isNotBlank(smallImage)) {
            return smallImage;
        }

        return jsonObject.getString("white_image");
    }

    private static String getSmallImage(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof JSONObject obj) {
            JSONArray jsonArray = obj.getJSONArray("string");
            if (jsonArray != null && jsonArray.size() > 0) {
                return jsonArray.getString(0) + "_180x180.jpg";
            }
        } else if (object instanceof JSONArray obj) {
            if (obj.size() > 0) {
                return obj.getString(0) + "_180x180.jpg";
            }
        }

        return "";
    }

    private static void fillItemRateDetail(UnionProductTO product, MaocheAlimamaUnionProductDetailDO productDetailDO) {

        if (product == null || productDetailDO == null) {
            return;
        }
        String origContent = productDetailDO.getOrigContent();
        JSONObject jsonObject = JSONObject.parseObject(origContent);
        if (jsonObject == null || jsonObject.getJSONObject("data") == null) {
            return;
        }
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject rate = data.getJSONObject("rate");
        if (rate == null || rate.get("keywords") == null) {
            return;
        }

        RateTO rateTO = new RateTO();
        product.setRate(rateTO);
        List<RateDetailTO> details = new ArrayList<>();
        JSONArray keywords = rate.getJSONArray("keywords");
        for (int i = 0; i < keywords.size(); i++) {
            Object o = keywords.get(i);
            if (o instanceof JSONObject item) {
                RateDetailTO rateDetailTO = JsonUtils.toReferenceType(item.toJSONString(), new TypeReference<RateDetailTO>() {
                });
                if (rateDetailTO == null) {
                    continue;
                }
                details.add(rateDetailTO);
            }
        }

        rateTO.setDetails(details);
        product.setRate(rateTO);
    }

    private static void fillItemAdvantage(UnionProductTO product, MaocheAlimamaUnionGoodPriceDO goodPriceDO) {
        if (product == null) {
            return;
        }
        product.setProductAdvantage(new ArrayList<>());
        product.setPriceAdvantage(new ArrayList<>());
        product.setActivity(new ArrayList<>());
        if (goodPriceDO != null) {
            List<String> activity = Optional.ofNullable(product.getActivity()).orElse(new ArrayList<>());
            activity.add("有好价");
            product.setActivity(activity);
        }
        if (goodPriceDO == null || StringUtils.isBlank(goodPriceDO.getContent())) {
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(goodPriceDO.getContent());
        if (jsonObject == null) {
            return;
        }

        JSONArray promotionTags = jsonObject.getJSONArray("promotionTags");
        if (promotionTags != null) {
            product.setPromotionTags(promotionTags.toJavaList(PromotionTagTO.class));
        }

        JSONArray algoTags = jsonObject.getJSONArray("algoTags");
        if (algoTags != null && algoTags.size() > 0) {
            product.setProductAdvantage(algoTags.toJavaList(String.class));
        }
        String goodPriceBenefit = jsonObject.getString("goodPriceBenefit");
        if (StringUtils.isNotBlank(goodPriceBenefit)) {
            product.getPriceAdvantage().add(goodPriceBenefit);
        }
    }

    public static UnionProductTagTO convert2TagTO(MaocheAlimamaUnionTitleKeywordDO keywordDO) {

        UnionProductTagTO productTagTO = new UnionProductTagTO();
        if (keywordDO == null || StringUtils.isBlank(keywordDO.getContent())) {
            return productTagTO;
        }

        productTagTO = JSONObject.parseObject(keywordDO.getContent(), UnionProductTagTO.class);
        if (productTagTO == null) {
            return new UnionProductTagTO();
        }

        return productTagTO;
    }

    /**
     * 解析标签
     * {
     *     "brand": "Grain",
     *     "secondbrand": "",
     *     "product": "干猫粮",
     *     "object": [],
     *     "season": [],
     *     "model": [],
     *     "material": [],
     *     "attribute": [
     *         "天然",
     *         "美国"
     *     ]
     * }
     * @param data
     */
    public static UnionProductTagTO convert2TagTO(String data) {

        // String data = "{\"brand\":\"贝贝\",\"secondbrand\":\"\",\"product\":\"纸巾\",\"object\":[],\"season\":[],\"model\":[],\"material\":[],\"attribute\":[\"贝贝\",\"乳霜\"]}\n";
        UnionProductTagTO productTagTO = new UnionProductTagTO();
        if (StringUtils.isBlank(data)) {
            return productTagTO;
        }

        UnionProductTagModel model = JSONObject.parseObject(data, UnionProductTagModel.class);
        if (model == null) {
            return new UnionProductTagTO();
        }

        return new UnionProductTagTO(model);
    }

    /**
     * 获取商品itemId
     * @param productDOs
     * @return
     */
    public static List<String> getItemIds(List<MaocheAlimamaUnionProductDO> productDOs) {
        if (CollectionUtils.isEmpty(productDOs)) {
            return new ArrayList<>();
        }

        return productDOs.stream().map(MaocheAlimamaUnionProductDO::getItemIdSuffix).distinct().collect(Collectors.toList());
    }


    public static void main(String[] args) {

        int rate = 602;
        int price = 1900;
        long commission = new BigDecimal(String.valueOf(rate)).multiply(new BigDecimal(String.valueOf(price))).divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP).longValue();
        System.out.println(commission);
    }
}
