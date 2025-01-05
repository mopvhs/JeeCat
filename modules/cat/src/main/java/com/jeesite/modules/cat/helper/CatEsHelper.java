package com.jeesite.modules.cat.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionGoodPriceDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductBihaohuoDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductPriceChartDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.entity.MaocheProductV2DO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.enums.CatActivityEnum;
import com.jeesite.modules.cat.enums.QualityStatusEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
import com.jeesite.modules.cat.model.PriceChartInfoTO;
import com.jeesite.modules.cat.model.PriceChartSkuBaseTO;
import com.jeesite.modules.cat.model.ProductCategoryModel;
import com.jeesite.modules.cat.model.ProductScoreModel;
import com.jeesite.modules.cat.model.ProductV2Content;
import com.jeesite.modules.cat.model.PromotionModel;
import com.jeesite.modules.cat.model.RateDetailTO;
import com.jeesite.modules.cat.model.ShopModel;
import com.jeesite.modules.cat.model.UnionProductModel;
import com.jeesite.modules.cat.model.UnionProductTagTO;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.stage.cg.ProductEsContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

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
        index.setTime(item.getTime());
        index.setRemark(item.getRemark());
        index.setMsgNew(item.getMsgNew());
        index.setImageUrl(item.getImageUrl());
        index.setCreateTime(item.getCreateTime());
        index.setUpdateTime(item.getUpdateTime());
        index.setProcessed(0L);

        String affType = null;
        if (StringUtils.isNotBlank(item.getAffType())) {
            affType = item.getAffType();
        }
        index.setAffType(affType);

        return index;
    }

    public static CarAlimamaUnionProductIndex buildCatProductIndexV2(MaocheAlimamaUnionProductDO item,
                                                                               MaocheAlimamaUnionProductDetailDO productDetailDO,
                                                                               MaocheProductV2DO productV2DO) {
        if (item == null) {
            return null;
        }
        try {
            CommandResponseV2 product = null;
            if (productV2DO != null && StringUtils.isNotBlank(productV2DO.getOrigContent())) {
                String productOrigContent = productV2DO.getOrigContent();
                 product = JsonUtils.toReferenceType(productOrigContent, new TypeReference<CommandResponseV2>() {
                });
            }
            if (product == null) {
                return null;
            }

            CarAlimamaUnionProductIndex index = new CarAlimamaUnionProductIndex();

            // 最新的商品详情构建
            ProductV2Helper.fillItemBaseInfo(index, product);

            // 填充优惠券详情
            ProductV2Helper.fillCouponInfo(index, product);

            // 填充审核信息
            ProductV2Helper.fillAuditInfo(index, item, productV2DO);

            // 价格信息
            ProductV2Helper.fillPriceInfo(index, product);

            // 补充标签
            fillTag(index, null);

            // 其他内容
            ProductV2Helper.fillOtherInfo(index, productDetailDO);

            return index;
        } catch (Exception e) {
            log.error("buildCatProductIndexV2 error, item:{}", JsonUtils.toJSONString(item), e);
        }

        return null;
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

    public static void fillPriceChartInfo(CarAlimamaUnionProductIndex index, MaocheAlimamaUnionProductBihaohuoDO priceChartDO) {
        if (index == null) {
            return;
        }
        Long price = index.getPromotionPrice();

        long priceChart = 0;
        long priceChartMinPrice = 0;
        long priceChartSyncTime = 0;
        List<PriceChartSkuBaseTO> priceChartSkuBase = null;
        if (priceChartDO != null) {
            JSONObject jsonObject = JsonUtils.toJsonObject(priceChartDO.getOrigContent());
//            log.info("price chart id:{}, chart id:{}", index.getId(), priceChartDO.getUiid());
            if (jsonObject != null) {
                priceChartSkuBase = ProductValueHelper.getPriceChartSkuBase(jsonObject);
                priceChartMinPrice = ProductValueHelper.getPriceChartMinPrice(priceChartSkuBase);
//                priceChartInfo = ProductValueHelper.getPriceChartInfo(jsonObject);
                if (CollectionUtils.isNotEmpty(priceChartSkuBase)) {
                    for (PriceChartSkuBaseTO base : priceChartSkuBase) {
                        if (StringUtils.isNotBlank(base.getCompareDesc())) {
                            priceChart = 1;
                            break;
                        }
                    }
                }
            }
            if (priceChartDO.getSyncDate() != null) {
                priceChartSyncTime = priceChartDO.getSyncDate().getTime();
            }
        }

        if (priceChartMinPrice > 0) {
            price = Math.min(price, priceChartMinPrice);
        }

        index.setPromotionPrice(price);
        index.setPriceChartSkuBases(priceChartSkuBase);
        index.setPriceChartInfo(null);
        index.setPriceChart(priceChart);
        index.setPriceChartSyncTime(priceChartSyncTime);
    }

    public static CarAlimamaUnionProductIndex buildCatUnionProductIndex(UnionProductModel model, ProductEsContext context) {

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
        index.setItemId(model.getItemId());
        index.setProductImage(model.getMainPic());
        index.setBenefitDesc(model.getBenefitDesc());
        index.setReservePrice(model.getReservePrice());
        index.setCoupon(promotion.getCoupon());
        index.setCouponRemainCount(promotion.getCouponRemainCount());
        index.setCouponTotalCount(promotion.getCouponTotalCount());
        index.setItemDescription(model.getItemDescription());
        index.setCommissionRate(model.getCommissionRate());
        index.setShopDsr(shop.getShopDsr());
        index.setShopTitle(shop.getShopName());
        index.setAuditStatus(model.getAuditStatus());
        index.setQualityStatus(model.getQualityStatus());

        index.setActivity(new ArrayList<>());
        index.setTkTotalSales(null);
        // 猫车分
        Map<String, String> calCatDsr = CalCatDsrUtils.calCatDsr(score.getDsrScore(), model.getVolume(), shop.getShopLevel(), shop.getFans(), model.getCommissionRate());
        String catDsrTips = calCatDsr.get("tips");
        index.setCatDsr(NumberUtils.toLong(calCatDsr.get("catDsr")));
        index.setCatDsrTips(catDsrTips);
        index.setPromotionPrice(promotion.getPromotionPrice());
        index.setSaleStatus(Optional.ofNullable(model.getSaleStatus()).orElse(SaleStatusEnum.INIT.getStatus()));
        if (model.getSaleStatusTime() != null) {
            index.setSaleStatusTime(model.getSaleStatusTime());
        }
        index.setCreateTime(model.getCreateTime());
        index.setDataSource(model.getDataSource());
        index.setPropsProductName(model.getPropsProductName());
        index.setPropsBrand(model.getPropsBrand());
        index.setRates(model.getRates());
        index.setCustomBenefit(model.getCustomBenefit());

        // 类目
        if (category != null) {
            index.setCategoryName(category.getCategoryName());
            index.setLevelOneCategoryName(category.getLevelOneCategoryName());
            index.setCidOnes(category.getCid1s());
            index.setCidTwos(category.getCid2s());
            index.setCidThirds(category.getCid3s());
        }

        fillPriceChartInfo(index, context.getPriceChartDO());

        return index;
    }
}
