package com.jeesite.modules.cgcat.dto.ocean;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.model.ProductPriceTO;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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


}
