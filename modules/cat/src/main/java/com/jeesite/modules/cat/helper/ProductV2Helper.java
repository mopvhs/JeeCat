package com.jeesite.modules.cat.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.PriceChartSkuBaseTO;
import com.jeesite.modules.cat.model.ProductV2Content;
import com.jeesite.modules.cat.model.RateDetailTO;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductV2Helper {

    public static Pattern N_YUAN_REGEX = Pattern.compile("^[0-9.]+元$");

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
                                // 正则匹配n元，过滤掉
                                String replaceAll = N_YUAN_REGEX.matcher(tag).replaceAll("");
                                if (StringUtils.isBlank(replaceAll)) {
                                    continue;
                                }
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

        long finalPromotionPrice = index.getPromotionPrice();
        long predictRoundingUpPrice = index.getPromotionPrice();
        String predictRoundingUpPriceDesc = "";
        List<String> pricePromotionTagList = new ArrayList<>();
        if (productV2Content != null && productV2Content.getPricePromotionInfo() != null) {
            ProductV2Content.PricePromotionInfo pricePromotionInfo = productV2Content.getPricePromotionInfo();
            if (pricePromotionInfo.getFinalPromotionPrice() != 0) {
                finalPromotionPrice = pricePromotionInfo.getFinalPromotionPrice();
            }
            if (pricePromotionInfo.getPredictRoundingUpPrice() != 0) {
                predictRoundingUpPrice = pricePromotionInfo.getPredictRoundingUpPrice();
            }
            predictRoundingUpPriceDesc = pricePromotionInfo.getPredictRoundingUpPriceDesc();
            pricePromotionTagList = pricePromotionInfo.getPromotionTagList();
        }

        index.setFinalPromotionPrice(finalPromotionPrice);
        index.setPredictRoundingUpPrice(predictRoundingUpPrice);
        index.setPredictRoundingUpPriceDesc(predictRoundingUpPriceDesc);
        index.setPricePromotionTagList(pricePromotionTagList);
    }

    /**
     * 填充基本信息
     * @param index
     */
    public static void  fillItemBaseInfo(CarAlimamaUnionProductIndex index, CommandResponseV2 product) {
        if (index == null || product == null) {
            return;
        }
        String itemId = product.getItemId();
        String itemIdSuffix = null;
        if (StringUtils.isNotBlank(itemId)) {
            String[] split = StringUtils.split(itemId, "-");
            if (split != null && split.length >= 2) {
                itemIdSuffix = split[1];
            }
        }
        CommandResponseV2.ItemBasicInfo itemBasicInfo = product.getItemBasicInfo();
        // 获取图片地址
        String picUrl = itemBasicInfo.getPictUrl();
        if (StringUtils.isBlank(picUrl)) {
            picUrl = itemBasicInfo.getWhiteImage();
        }

        index.setItemId(itemId);
        index.setVolume(NumberUtils.toLong(itemBasicInfo.getVolume()));
        index.setTitle(itemBasicInfo.getTitle());
        index.setItemIdSuffix(itemIdSuffix);
        index.setShopTitle(itemBasicInfo.getShopTitle());
        index.setProductImage(picUrl);
        index.setItemDescription(itemBasicInfo.getSubTitle());
        index.setCategoryName(itemBasicInfo.getCategoryName());
        index.setLevelOneCategoryName(itemBasicInfo.getLevelOneCategoryName());
        index.setTkTotalSales(NumberUtils.toLong(itemBasicInfo.getTkTotalSales()));

        CommandResponseV2.PublishInfo publishInfo = product.getPublishInfo();
        if (publishInfo != null && publishInfo.getIncomeInfo() != null) {
            CommandResponseV2.IncomeInfo incomeInfo = publishInfo.getIncomeInfo();
            index.setCommissionRate(NumberUtils.toLong(incomeInfo.getCommissionRate()));
        }

    }

    /**
     * 填充优惠券信息
     * @param index
     * @param product
     */
    public static void fillCouponInfo(CarAlimamaUnionProductIndex index, CommandResponseV2 product) {
        if (index == null || product == null) {
            return;
        }

        CommandResponseV2.PricePromotionInfo pricePromotionInfo = product.getPricePromotionInfo();
        if (pricePromotionInfo == null || pricePromotionInfo.getFinalPromotionPathList() == null) {
            return;
        }
        Pattern pattern = Pattern.compile("[0-9.]+");

        List<CommandResponseV2.FinalPromotionPathDetail> coupons = pricePromotionInfo.getFinalPromotionPathList().getFinalPromotionPathMapData();
        for (CommandResponseV2.FinalPromotionPathDetail detail : coupons) {
            if (detail == null) {
                continue;
            }
            if (!"商品券".equalsIgnoreCase(detail.getPromotionTitle())) {
                continue;
            }
            Matcher matcher = pattern.matcher(detail.getPromotionDesc());
            List<String> finds = new ArrayList<>();
            while (matcher.find()) {
                finds.add(matcher.group());
            }
            String couponStartFee = null;
            index.setCoupon(PriceHelper.formatPrice(detail.getPromotionFee()));
            if (CollectionUtils.isNotEmpty(finds)) {
                couponStartFee = finds.get(0);
            }
            index.setCouponStartFee(PriceHelper.formatPrice(couponStartFee));
        }
    }

//    public static void main(String[] args) {
//        String content = "满123减12.3";
//        Pattern pattern = Pattern.compile("[0-9.]+");
//        Matcher matcher = pattern.matcher(content);
//
//        List<String> finds = new ArrayList<>();
//        while (matcher.find()) {
//            finds.add(matcher.group());
//        }
//        System.out.println(finds);
//    }

    /**
     * 填充优惠券信息
     * @param index
     * @param product
     */
    public static void fillPriceInfo(CarAlimamaUnionProductIndex index, CommandResponseV2 product) {
        if (index == null || product == null) {
            return;
        }
        CommandResponseV2.PricePromotionInfo pricePromotionInfo = product.getPricePromotionInfo();
        if (pricePromotionInfo == null) {
            return;
        }
        // 商品信息-一口价通常显示为划线价
        long reservePrice = PriceHelper.formatPrice(pricePromotionInfo.getReservePrice());
        // 促销信息-预估到手价(元)。若属于预售商品，付定金时间内，预估到手价价=定金+尾款的预估到手价
        long finalPromotionPrice = PriceHelper.formatPrice(pricePromotionInfo.getFinalPromotionPrice());
        // 促销信息-销售价格，无促销时等于一口价，有促销时为促销价。若属于预售商品，付定金时间内，在线售卖价=预售价
        long zkFinalPrice = PriceHelper.formatPrice(pricePromotionInfo.getZkFinalPrice());


        long predictRoundingUpPrice = PriceHelper.formatPrice(pricePromotionInfo.getPredictRoundingUpPrice());


        long priceChart = 0;
        long priceChartSyncTime = 0;
        List<PriceChartSkuBaseTO> priceChartSkuBase = null;
        index.setPriceChartSkuBases(priceChartSkuBase);
        index.setPriceChartInfo(null);
        index.setPriceChart(priceChart);
        index.setPriceChartSyncTime(priceChartSyncTime);

        index.setPromotionPrice(finalPromotionPrice);
        index.setFinalPromotionPrice(finalPromotionPrice);
        index.setReservePrice(reservePrice);

        index.setPricePromotionTagList(pricePromotionInfo.getPromotionTagList());
        index.setPredictRoundingUpPrice(predictRoundingUpPrice);
        index.setPredictRoundingUpPriceDesc(pricePromotionInfo.getPredictRoundingUpPriceDesc());
    }


    /**
     * 填充审核信息
     * @param index
     * @param item
     */
    public static void fillAuditInfo(CarAlimamaUnionProductIndex index, MaocheAlimamaUnionProductDO item) {
        if (index == null || item == null) {
            return;
        }
        Long auditStatus = Optional.ofNullable(item.getAuditStatus()).orElse(0L);

        index.setCustomBenefit(item.getCustomBenefit());
        index.setId(item.getUiid());
        index.setAuditStatus(auditStatus);
        index.setSaleStatus(Optional.ofNullable(item.getSaleStatus()).orElse(SaleStatusEnum.INIT.getStatus()));
        if (item.getSaleStatusDate() != null) {
            index.setSaleStatusTime(item.getSaleStatusDate().getTime());
        }

        long updateTime = item.getUpdateTime() != null ? item.getUpdateTime().getTime() : 0L;
        long syncTime = item.getSyncTime() != null ? item.getSyncTime().getTime() : 0L;
        if (syncTime > updateTime) {
            updateTime = syncTime;
        }
        index.setDataSource(item.getDataSource());
        index.setCreateTime(item.getCreateTime().getTime());
        index.setUpdateTime(updateTime);
        index.setSyncTime(syncTime);
        index.setQualityStatus(item.getQualityStatus());
    }

    public static void fillOtherInfo(CarAlimamaUnionProductIndex index, MaocheAlimamaUnionProductDetailDO productDetailDO) {
        String fans = null;
        Long creditLevel = null;
        // 获取粉丝数和店铺等级
        if (productDetailDO != null) {
            String seller = productDetailDO.getSeller();
            if (StringUtils.isNotBlank(seller)) {
                JSONObject sellerObj = JsonUtils.toJsonObject(seller);
                if (sellerObj != null) {
                    fans = sellerObj.getString("fans");
                    creditLevel = NumberUtils.toLong(sellerObj.getString("creditLevel"));
                }
            }
            String props = productDetailDO.getProps();
            if (StringUtils.isNotBlank(props)) {
                JSONObject propsObj = JsonUtils.toJsonObject(props);
                if (propsObj != null) {
                    String detailPropsProductName = ProductValueHelper.getDetailPropsProductName(propsObj);
                    String detailPropsBrand = ProductValueHelper.getDetailPropsBrand(propsObj);

                    index.setPropsProductName(detailPropsProductName);
                    index.setPropsBrand(detailPropsBrand);
                }
            }

            String rate = productDetailDO.getRate();
            JSONObject rateObj = null;
            if (StringUtils.isNotBlank(rate)) {
                rateObj = JsonUtils.toJsonObject(rate);
            }

            List<RateDetailTO> details = ProductValueHelper.getDetailRates(rateObj);
            index.setRates(details);
        }

        // 猫车分
        Map<String, String> calCatDsr = CalCatDsrUtils.calCatDsrV2(index.getVolume(), creditLevel, fans, index.getCommissionRate());
        String catDsrTips = calCatDsr.get("tips");
        index.setCatDsr(NumberUtils.toLong(calCatDsr.get("catDsr")));
        index.setCatDsrTips(catDsrTips);

        index.setActivity(new ArrayList<>());
    }
}
