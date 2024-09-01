package com.jeesite.modules.cgcat.dto.ocean;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.model.ProductPriceTO;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandContext;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

@Data
public class OceanMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3161336340315210519L;

    private Long id;

    private List<String> resourceIds;

    private String affType;

    private String msg;

    private Date createDate;

    private List<UnionProductTO> products;

    public static OceanMessageVO toVO(MaocheMessageSyncIndex index) {
        if (index == null) {
            return null;
        }

        OceanMessageVO dto = new OceanMessageVO();
        dto.setId(index.getId());
        dto.setResourceIds(index.getResourceIds());
        dto.setAffType(index.getAffType());
        dto.setMsg(index.getMsg());
        dto.setCreateDate(new Date(index.getCreateDate()));

        return dto;
    }

    public static UnionProductTO convertProduct(MaocheRobotCrawlerMessageProductDO productDO) {
        if (productDO == null) {
            return null;
        }

        UnionProductTO product = new UnionProductTO();

        JSONObject apiContentObj = JsonUtils.toJsonObject(productDO.getApiContent());

        // 佣金
        Long price = productDO.getPrice();
        long commissionRate = Optional.ofNullable(productDO.getCommissionRate()).orElse(0L);
        long commission = 0;
        if (commissionRate > 0) {
            commission = new BigDecimal(String.valueOf(price)).multiply(new BigDecimal(String.valueOf(commissionRate))).divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP ).longValue();
        }

        ProductPriceTO displayPrice = new ProductPriceTO();
        displayPrice.setPrice(price);
        product.setDisplayPrice(displayPrice);
        product.setShopDsr(NumberUtils.toLong(productDO.getShopDsr()));
        product.setTitle(productDO.getTitle());
        product.setCreateDate(productDO.getCreateDate());
        product.setCommission(commission);
        product.setCommissionRate(commissionRate);
        product.setImgUrl(productDO.getPictUrl());
        product.setVolume(productDO.getVolume());
        // todo yhq 推送次数
        product.setPushNum(0L);
        product.setShopName(productDO.getShopName());

        if (apiContentObj != null && apiContentObj.get("num_iid") != null) {
            String numIid = apiContentObj.getString("num_iid");
            product.setItemId(numIid);
            product.setItemUrl("https://uland.taobao.com/item/edetail?id=?id=" + numIid);
        }

        return product;
    }

    public static UnionProductTO convertProduct(MaocheMessageProductIndex index) {
        if (index == null) {
            return null;
        }

        UnionProductTO product = new UnionProductTO();

        // 佣金
        Long price = index.getPrice();
        Long commissionRate = index.getCommissionRate();
        long commission = 0;
        if (commissionRate != null && commissionRate > 0) {
            commission = new BigDecimal(String.valueOf(price)).multiply(new BigDecimal(String.valueOf(commissionRate))).divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP ).longValue();
        }

        long createTime = Optional.ofNullable(index.getCreateDate()).orElse(0L);

        ProductPriceTO displayPrice = new ProductPriceTO();
        displayPrice.setPrice(price);
        product.setDisplayPrice(displayPrice);
        // 优惠后的价格
        product.setPromotionPrice(price);
        // 原价
        product.setReservePrice(price);
        product.setShopDsr(NumberUtils.toLong(index.getShopDsr()));
        product.setTitle(index.getTitle());
        product.setCreateDate(new Date(createTime));
        product.setCommission(commission);
        product.setCommissionRate(commissionRate);
        product.setImgUrl(index.getPictUrl());
        product.setVolume(index.getVolume());
        // todo yhq 推送次数
        product.setPushNum(0L);
        product.setShopName(index.getShopName());
        product.setItemId(index.getItemId());

        if (StringUtils.isNotBlank(index.getItemId())) {
            product.setItemUrl("https://uland.taobao.com/item/edetail?id=?id=" + index.getItemId());
        }

        return product;
    }

    public static List<OceanMessageVO> toVOs(List<MaocheMessageSyncIndex> indies) {
        if (CollectionUtils.isEmpty(indies)) {
            return new ArrayList<>();
        }

        List<OceanMessageVO> dtos = new ArrayList<>();

        for (MaocheMessageSyncIndex index : indies) {
            OceanMessageVO vo = toVO(index);
            if (vo != null) {
                dtos.add(vo);
            }
        }

        return dtos;
    }

    public static void replaceUrl2Html(List<OceanMessageVO> msgs, Map<Long, MaocheRobotCrawlerMessageSyncDO> syncDOMap) {
        if (CollectionUtils.isEmpty(msgs)) {
            return;
        }
        String urlFormat = "<a href=\"%s\" target=\"_blank\">%s</a >";
        String colorUrlFormat = "<a href=\"%s\" style=\"color:%s\" target=\"_blank\">%s</a >";
        for (OceanMessageVO product : msgs) {
            StringBuilder msg = new StringBuilder();
            MaocheRobotCrawlerMessageSyncDO messageSyncDO = syncDOMap.get(product.getId());
            Map<String, Object> remarksMap = getRemarksMap(messageSyncDO);
            List<String> jdOtherUrls = jdOtherUrls(remarksMap);
            Map<String, String> successJdUrlMap = successJdUrlMap(remarksMap);
            List<String> successUrls = new ArrayList<>();
            if (MapUtils.isNotEmpty(successJdUrlMap)) {
                successUrls = new ArrayList<>(successJdUrlMap.values());
            }

            CommandContext commandContext = getShortUrlContext(remarksMap);
            Map<String, ShortUrlDetail> shortDetailMap = new HashMap<>();
            if (commandContext != null && CollectionUtils.isNotEmpty(commandContext.listShortDetails())) {
                for (ShortUrlDetail detail : commandContext.listShortDetails()) {
                    if (StringUtils.isNotBlank(detail.getReplaceUrl())) {
                        shortDetailMap.put(detail.getReplaceUrl(), detail);
                    } else {
                        shortDetailMap.put(detail.getContentUrl(), detail);
                    }
                }
            }

            String content = product.getMsg();
            String[] split = StringUtils.split(content, "\n");
            for (String item : split) {
                Matcher matcher = CommandService.jd.matcher(item);
                if (matcher.find()) {
                    String group = matcher.group();
                    String url = null;
                    if (jdOtherUrls.contains(group)) {
                        url = String.format(colorUrlFormat, group, "red", group);
                    } else if (successUrls.contains(group)) {
                        url = String.format(colorUrlFormat, group, "#90EE90", group);
                    } else if (shortDetailMap.get(group) != null) {
                        ShortUrlDetail detail = shortDetailMap.get(group);
                        if (BooleanUtils.isTrue(detail.getApiRes())) {
                            url = String.format(colorUrlFormat, group, "#90EE90", group);
                        } else {
                            url = String.format(colorUrlFormat, group, "red", group);
                        }
                    } else {
                        url = String.format(urlFormat, group, group);
                    }
                    msg.append(url).append("\n");
                } else {
                    msg.append(item).append("\n");
                }
            }
            product.setMsg(msg.toString());
        }
    }

    public static List<String> jdOtherUrls(Map<String, Object> remarksMap) {
        if (MapUtils.isEmpty(remarksMap)) {
            return new ArrayList<>();
        }
        Object jdOtherUrls = remarksMap.get("jdOtherUrls");
        if (jdOtherUrls == null) {
            return new ArrayList<>();
        }

        if (jdOtherUrls instanceof List) {
            return (List<String>) jdOtherUrls;
        }

        return new ArrayList<>();
    }

    public static Map<String, String> successJdUrlMap(Map<String, Object> remarksMap) {
        if (MapUtils.isEmpty(remarksMap)) {
            return new HashMap<>();
        }
        Object obj = remarksMap.get("successJdUrlMap");
        if (obj == null) {
            return new HashMap<>();
        }

        if (obj instanceof Map) {
            return (Map<String, String>) obj;
        }

        return new HashMap<>();
    }

    public static CommandContext getShortUrlContext(Map<String, Object> remarksMap) {
        if (MapUtils.isEmpty(remarksMap)) {
            return null;
        }
        Object obj = remarksMap.get("commandContext");
        if (obj == null) {
            return null;
        }
        String jsonString = JsonUtils.toJSONString(remarksMap.get("commandContext"));

        return JSONObject.parseObject(jsonString, CommandContext.class);
    }

//    public <T> T getRemarks(Map<String, Object> remarksMap, String key) {
//        if (MapUtils.isEmpty(remarksMap) || StringUtils.isBlank(key)) {
//            return null;
//        }
//
//        Object obj = remarksMap.get(key);
//
//        return JsonUtils.toReferenceType(JsonUtils.toJSONString(obj), new TypeReference<T>() {
//        });
//    }

    public static Map<String, Object> getRemarksMap(MaocheRobotCrawlerMessageSyncDO syncDO) {
        if (syncDO == null || StringUtils.isBlank(syncDO.getRemarks())) {
            return new HashMap<>();
        }

        Map<String, Object> referenceType = JsonUtils.toReferenceType(syncDO.getRemarks(), new TypeReference<Map<String, Object>>() {
        });
        if (referenceType == null) {
            referenceType = new HashMap<>();
        }

        return referenceType;
    }
}
