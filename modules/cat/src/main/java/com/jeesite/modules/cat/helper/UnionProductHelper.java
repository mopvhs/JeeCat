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
import com.jeesite.modules.cat.enums.QualityStatusEnum;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.HighLightTextTO;
import com.jeesite.modules.cat.model.PriceChartInfoTO;
import com.jeesite.modules.cat.model.PriceChartSkuBaseTO;
import com.jeesite.modules.cat.model.ProductPriceTO;
import com.jeesite.modules.cat.model.ProductTagTO;
import com.jeesite.modules.cat.model.PromotionTagTO;
import com.jeesite.modules.cat.model.RateDetailTO;
import com.jeesite.modules.cat.model.RateTO;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.model.UnionProductTagTO;
import com.jeesite.modules.cat.model.keytitle.UnionProductTagModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class UnionProductHelper {


    public static List<UnionProductTO> convertUnionProduct(List<CarAlimamaUnionProductIndex> indexList) {

        if (CollectionUtils.isEmpty(indexList)) {
            return new ArrayList<>();
        }

        List<UnionProductTO> products = new ArrayList<>();
        for (CarAlimamaUnionProductIndex index : indexList) {

            UnionProductTO product = new UnionProductTO();
            product.setId(index.getId());
            product.setItemId(index.getItemId());
            product.setItemUrl("https://uland.taobao.com/item/edetail?id=" + index.getItemId());
            // todo 不同的平台执行不同的流行
            product.setShareCommand("");
            product.setTitle(index.getTitle());
            product.setCategoryName(index.getCategoryName());
            product.setReservePrice(index.getReservePrice());
            product.setCommissionRate(index.getCommissionRate());

            product.setTkTotalSales(index.getTkTotalSales());
            product.setCreateDate(new Date(index.getCreateTime()));
            product.setUpdateDate(new Date(Optional.ofNullable(index.getUpdateTime()).orElse(System.currentTimeMillis())));
            // 店铺Dsr
            product.setShopDsr(index.getShopDsr());
            // 猫车分
            product.setCatDsr(index.getCatDsr());
            product.setCatDsrTips(index.getCatDsrTips());
            // 店铺名称
            product.setShopName(index.getShopTitle());
            // 设置优惠券数量信息
            product.setCouponStartFee(index.getCouponStartFee());
            product.setCoupon(index.getCoupon());
            product.setCouponRemainCount(index.getCouponRemainCount());
            product.setCouponTotalCount(index.getCouponTotalCount());
            product.setPromotionPrice(index.getPromotionPrice());

            product.setQualityStatus(index.getQualityStatus());
            if (QualityStatusEnum.GOLD.getStatus().equals(index.getQualityStatus())) {
                product.setQualityIcon("https://cat.zhizher.com/assets/jbsp.png");
            }

            long commission = -999999999L;
            if (index.getCommissionRate() != null && index.getCommissionRate() > 0) {
                commission = new BigDecimal(String.valueOf(index.getCommissionRate())).multiply(new BigDecimal(String.valueOf(index.getReservePrice()))).divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP).longValue();
            }
            product.setCommission(commission);
            product.setVolume(index.getVolume());
            product.setAuditStatus(index.getAuditStatus());
            product.setGoodRate(-1L);

            // 上架状态
            product.setSaleStatus(index.getSaleStatus());
            if (index.getSaleStatusTime() != null) {
                product.setOnShelfDate(new Date(index.getSaleStatusTime()));
            }

            product.setDataSource(Optional.ofNullable(index.getDataSource()).orElse(""));
            product.setBenefitDesc(Optional.ofNullable(index.getBenefitDesc()).orElse(""));

            // 关键利益点
            // 有好价移除
            fillBenefitInfo(product, index);

            product.setItemDescription(Optional.ofNullable(index.getItemDescription()).orElse(""));
            // 评论信息
//            fillItemRateDetail(product, productDetailMap.get(productDO.getItemIdSuffix()));

            RateTO rateTO = new RateTO();
            rateTO.setDetails(index.getRates());
            product.setRate(rateTO);

            String belongToName = "超搜";
            // 商品所属
            if (CollectionUtils.isNotEmpty(product.getActivity())) {
                product.setBelongTo(product.getActivity());
            } else {
                product.setBelongTo(Collections.singletonList(belongToName));
            }

            fillCustomTags(product, index);

//            List<String> cidOneNames = new ArrayList<>();
//            // 获取自定义类目
//            if (CollectionUtils.isNotEmpty(index.getCidOnes())) {
//                for (Long cid : index.getCidOnes()) {
//                    MaocheCategoryDO maocheCategoryDO = customCategoryMap.get(cid);
//                    if (maocheCategoryDO == null) {
//                        continue;
//                    }
//                    cidOneNames.add(maocheCategoryDO.getName());
//                }
//            }
            product.setCidOneNames(new ArrayList<>());

            product.setImgUrl(index.getProductImage());

//            UnionProductTagTO unionProductTagTO = convert2TagTO(keywordMap.get(productDO.getItemIdSuffix()));
//            product.setTag(unionProductTagTO);

            // 针对有好价的商品，替换promotionTags的数据
            replaceCouponOfPromotionTags(product);

            fillPriceInfo(product, index);

            product.setCustomBenefit(index.getCustomBenefit());

            products.add(product);
        }


        return products;
    }

    private static void replaceCouponOfPromotionTags(UnionProductTO product) {
        if (CollectionUtils.isEmpty(product.getPromotionTags())) {
            return;
        }
        List<PromotionTagTO> promotionTags = new ArrayList<>();
        for (PromotionTagTO tag : product.getPromotionTags()) {
            if (tag.getTagType().equalsIgnoreCase("COUPON")) {
                continue;
            }
            promotionTags.add(tag);
        }
        if (product.getCoupon() != null && product.getCoupon() > 0 && product.getCouponRemainCount() != null && product.getCouponRemainCount() > 0) {
            PromotionTagTO couponTag = new PromotionTagTO();
            couponTag.setTagTypeDisplay("优惠券");
            couponTag.setTagType("COUPON");
            couponTag.setTagDisplay(PriceHelper.formatPrice(product.getCoupon(), ".00", "") + "元");
            promotionTags.add(0, couponTag);
        }
        product.setPromotionTags(promotionTags);
    }

    private static void fillPriceInfo(UnionProductTO product, CarAlimamaUnionProductIndex index) {
        Long price = index.getPromotionPrice();

        ProductPriceTO priceTO = new ProductPriceTO();
        priceTO.setDesc("到手价");
        priceTO.setPrice(price);
        product.setDisplayPrice(priceTO);

        // 排序
        List<String> sortKeywords = new ArrayList<>();
        sortKeywords.add("旗舰店");
        sortKeywords.add("近");
        sortKeywords.add("淘宝同款低价");
        sortKeywords.add("同款低价");
        sortKeywords.add("低于");

        // 价格标签
        List<PriceChartSkuBaseTO> skuBases = index.getPriceChartSkuBases();
        if (CollectionUtils.isNotEmpty(skuBases)) {
            List<String> list = skuBases.stream().filter(Objects::nonNull).map(PriceChartSkuBaseTO::getCompareDesc).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

            List<ProductTagTO> sorts = new ArrayList<>();
            for (String keyword : sortKeywords) {
                Iterator<String> iterator = list.iterator();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    if (next.startsWith(keyword)) {
                        ProductTagTO tag = new ProductTagTO(next, null, 0, 0);
                        tag.setIconInfo("https://cat.zhizher.com/assets/down_arrow.png", 31, 31);
                        sorts.add(tag);
//                        if (next.equals("同款低价")) {
//                            tag.setIconInfo("https://cat.zhizher.com/assets/down_arrow.png", 31, 31);
//                        }
                        iterator.remove();
                        break;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(list)) {
                sorts.addAll(list.stream().map(i -> new ProductTagTO(i, "https://cat.zhizher.com/assets/down_arrow.png", 31, 31)).toList());
            }
            product.setPriceChartTags(sorts);

            Map<String, PriceChartSkuBaseTO> minPriceMap = new HashMap<>();
            List<HighLightTextTO> daoDaoList = new ArrayList<>();

            Map<Long, PriceChartSkuBaseTO> skuBaseTOMap = skuBases.stream().collect(Collectors.toMap(PriceChartSkuBaseTO::getSkuId, Function.identity(), (o1, o2) -> o1));
            // 叨叨
            for (PriceChartSkuBaseTO item : skuBases) {
                if (StringUtils.isBlank(item.getCompareDesc()) || skuBaseTOMap.get(item.getSkuId()) == null) {
                    continue;
                }
                PriceChartSkuBaseTO chartInfoTO = minPriceMap.get(item.getCompareDesc());
                if (chartInfoTO == null) {
                    minPriceMap.put(item.getCompareDesc(), item);
                    continue;
                }
                // 对比价格
                if (item.getPrice() < chartInfoTO.getPrice()) {
                    minPriceMap.put(item.getCompareDesc(), item);
                    continue;
                }
            }

            if (MapUtils.isNotEmpty(minPriceMap)) {
                for (Map.Entry<String, PriceChartSkuBaseTO> entry : minPriceMap.entrySet()) {
                    PriceChartSkuBaseTO value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    PriceChartSkuBaseTO skuBaseTO = skuBaseTOMap.get(value.getSkuId());
                    if (StringUtils.isBlank(skuBaseTO.getSkuProperty())) {
                        continue;
                    }
                    HighLightTextTO textTO = new HighLightTextTO();
                    textTO.setHighLight(value.getCompareDesc() + "￥" + PriceHelper.formatPrice(value.getPrice(), ".00", ""));
                    textTO.setNormal("选择" + skuBaseTO.getSkuProperty() + "下单");
                    daoDaoList.add(textTO);
                }
            }

            product.setDaoDaoList(daoDaoList);
        }
    }

    private static void fillBenefitInfo(UnionProductTO product,
                                        CarAlimamaUnionProductIndex index) {


        // 叨叨
        String benefitDesc = Optional.ofNullable(product.getBenefitDesc()).orElse("");
        if (StringUtils.isNotBlank(benefitDesc)) {
            int start = StringUtils.indexOf(benefitDesc, "【");
            int end = StringUtils.indexOf(benefitDesc, "】");

            if (start >= 0 && end > 0) {
                String highLight = benefitDesc.substring(start, end + 1);
                String normal = benefitDesc.substring(end  + 1);
                HighLightTextTO benefitDescTO = new HighLightTextTO();
                benefitDescTO.setNormal(normal);
                benefitDescTO.setHighLight(highLight);
                product.setHighLightBenefitDesc(benefitDescTO);
            } else {
                HighLightTextTO benefitDescTO = new HighLightTextTO();
                benefitDescTO.setNormal(benefitDesc);
                product.setHighLightBenefitDesc(benefitDescTO);
            }
        }

        String itemDescription = index.getItemDescription();
        if (StringUtils.isBlank(itemDescription)) {
            return;
        }

        HighLightTextTO textTO = new HighLightTextTO();
        // 普通的全部高亮
        textTO.setHighLight(itemDescription);
        product.setMainBenefit(textTO);
    }

    private static String getProductImage(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        String smallImage = jsonObject.getString("pict_url");
        if (StringUtils.isNotBlank(smallImage)) {
            return smallImage;
        }
        smallImage = getSmallImage(jsonObject.get("small_images"));
        if (StringUtils.isNotBlank(smallImage)) {
            return smallImage;
        }
        smallImage = getSmallImage(jsonObject.get("smallImages"));
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
        if (rate == null || rate.get("keywords") == null || !(rate.get("keywords") instanceof JSONArray)) {
            return;
        }

        List<RateDetailTO> details = new ArrayList<>();
        try {
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
        } catch (Exception e) {
            log.error("fillItemRateDetail error productDetailId:{}", productDetailDO.getId(), e);
        }

        RateTO rateTO = new RateTO();
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
    }


    private static HighLightTextTO buildGoodPriceBenefit(MaocheAlimamaUnionGoodPriceDO goodPriceDO) {
        if (goodPriceDO == null) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(goodPriceDO.getContent());
        if (jsonObject == null) {
            return null;
        }

        String goodPriceBenefit = jsonObject.getString("goodPriceBenefit");
        if (StringUtils.isNotBlank(goodPriceBenefit)) {
            try {
                HighLightTextTO highLightTextTO = null;
                List<String> keywords = new ArrayList<>();
                keywords.add("低于");
                keywords.add("近");
                for (String keyword : keywords) {
                    int i = StringUtils.indexOf(goodPriceBenefit, keyword);
                    if (i < 0) {
                        continue;
                    }
                    highLightTextTO = new HighLightTextTO();
                    highLightTextTO.setNormal(goodPriceBenefit.substring(0, i));
                    highLightTextTO.setHighLight(goodPriceBenefit.substring(i));
                    return highLightTextTO;
                }
            } catch (Exception e) {
                log.error("fillItemAdvantage set highLight exception goodPriceBenefit:{}", goodPriceBenefit, e);
            }
        }

        return null;
    }

    private static void fillCustomTags(UnionProductTO product, CarAlimamaUnionProductIndex index) {
        if (product == null) {
            return;
        }
        try {
            List<ProductTagTO> customTags = Optional.ofNullable(product.getProductTags()).orElse(new ArrayList<>());

            // new 标
            Long createTime = index.getCreateTime();
            // 一天内
            if (createTime != null && (System.currentTimeMillis() - createTime) <= 86400000) {
                customTags.add(new ProductTagTO("new", null, 0, 0));
            }
            // 金标商品
            if (QualityStatusEnum.GOLD.getStatus().equals(index.getQualityStatus())) {
                customTags.add(new ProductTagTO(null, "https://cat.zhizher.com/assets/jbsp.png", 32, 100));
            }

            product.setProductTags(customTags);
        } catch (Exception e) {
            log.error("fillCustomTags exception e", e);
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

    /**
     * 获取商品itemId
     * @param productDOs
     * @return
     */
    public static List<String> getIids(List<MaocheAlimamaUnionProductDO> productDOs) {
        if (CollectionUtils.isEmpty(productDOs)) {
            return new ArrayList<>();
        }

        return productDOs.stream().map(MaocheAlimamaUnionProductDO::getIid).distinct().collect(Collectors.toList());
    }


    public static void main(String[] args) {

//        String text = "12【1212】打算撒记得撒娇";
//        int start = StringUtils.indexOf(text, "【");
//        if (start < 0) {
//            return;
//        }
//        int end = StringUtils.indexOf(text, "】");
//        if (end < 0) {
//            return;
//        }
//
//        String highLight = text.substring(0, start) + text.substring(start, end + 1) + text.substring(end  + 1);
//
//        System.out.println(highLight);

        String s1 = "旗舰店90天最低价";
        String s2 = "淘宝同款低价";
        String s3 = "同款低价";
        String s4 = "近42天最低价";


        List<String> list = new ArrayList<>();
        list.add(s3);
        list.add(s4);
        list.add(s1);
        list.add(s2);

        List<String> sortKeywords = new ArrayList<>();
        sortKeywords.add("旗舰店");
        sortKeywords.add("淘宝同款低价");
        sortKeywords.add("同款低价");
        sortKeywords.add("近");

        // 正则



    }
}
