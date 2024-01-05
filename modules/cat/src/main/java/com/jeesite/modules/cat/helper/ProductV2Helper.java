package com.jeesite.modules.cat.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.ProductV2Content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductV2Helper {


    public static ProductV2Content processContent(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        JSONObject productV2Obj = JsonUtils.toJsonObject(content);
        if (productV2Obj == null) {
            return null;
        }

        ProductV2Content productV2Content = new ProductV2Content();

        ProductV2Content.PricePromotionInfo pricePromotionInfo = processPricePromotionInfo(productV2Obj.getJSONObject("price_promotion_info"));
        productV2Content.setPricePromotionInfo(pricePromotionInfo);

        return productV2Content;
    }

    public static ProductV2Content.PricePromotionInfo processPricePromotionInfo(JSONObject pricePromotionInfo) {
        ProductV2Content.PricePromotionInfo data = new ProductV2Content.PricePromotionInfo();
        long finalPromotionPrice = 0;
        long predictRoundingUpPrice = 0;
        String predictRoundingUpPriceDesc = "";
        List<String> pricePromotionTagList = new ArrayList<>();
        // 到手价
        if (pricePromotionInfo != null) {

            finalPromotionPrice = PriceHelper.formatPrice(pricePromotionInfo.get("final_promotion_price"));
            Object ptl = pricePromotionInfo.get("promotion_tag_list");
            if (ptl != null) {
                Object ptmd = pricePromotionInfo.getJSONObject("promotion_tag_list").get("promotion_tag_map_data");
                if (ptmd != null) {
                    if (ptmd instanceof JSONObject ptmdObj) {
                        String tag = ptmdObj.getString("tag_name");
                        if (StringUtils.isNotBlank(tag)) {
                            pricePromotionTagList.add(tag);
                        }
                    } else if (ptmd instanceof JSONArray ptmdArray) {
                        for (int i = 0; i < ptmdArray.size(); i++) {
                            JSONObject tagObj = ptmdArray.getJSONObject(i);
                            if (tagObj == null || tagObj.get("tag_name") == null) {
                                continue;
                            }
                            String tag = tagObj.getString("tag_name");
                            if (StringUtils.isNotBlank(tag)) {
                                pricePromotionTagList.add(tag);
                            }
                        }
                    }

                    predictRoundingUpPrice = PriceHelper.formatPrice(pricePromotionInfo.get("predict_rounding_up_price"));
                    predictRoundingUpPriceDesc = Optional.ofNullable(pricePromotionInfo.getString("predict_rounding_up_price_desc")).orElse("");
                }
            }
        }

        data.setFinalPromotionPrice(finalPromotionPrice);
        data.setPromotionTagList(pricePromotionTagList);
        data.setPredictRoundingUpPrice(predictRoundingUpPrice);
        data.setPredictRoundingUpPriceDesc(predictRoundingUpPriceDesc);

        return data;
    }


    public static void fillPricePromotionInfo2Index(CarAlimamaUnionProductIndex index, ProductV2Content productV2Content) {
        if (index == null) {
            return;
        }

        long finalPromotionPrice = 0;
        long predictRoundingUpPrice = 0;
        String predictRoundingUpPriceDesc = "";
        List<String> pricePromotionTagList = new ArrayList<>();
        if (productV2Content != null && productV2Content.getPricePromotionInfo() != null) {
            ProductV2Content.PricePromotionInfo pricePromotionInfo = productV2Content.getPricePromotionInfo();
            finalPromotionPrice = pricePromotionInfo.getFinalPromotionPrice();
            predictRoundingUpPrice = pricePromotionInfo.getPredictRoundingUpPrice();
            predictRoundingUpPriceDesc = pricePromotionInfo.getPredictRoundingUpPriceDesc();
            pricePromotionTagList = pricePromotionInfo.getPromotionTagList();
        }

        index.setFinalPromotionPrice(finalPromotionPrice);
        index.setPredictRoundingUpPrice(predictRoundingUpPrice);
        index.setPredictRoundingUpPriceDesc(predictRoundingUpPriceDesc);
        index.setPricePromotionTagList(pricePromotionTagList);

    }
}
