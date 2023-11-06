package com.jeesite.modules.cgcat.dto.ocean;

import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class OceanMessageProductVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3161336340315210519L;

    private Long id;

    private String resourceId;

    private String affType;

    private String innerId;

    private String title;

    private String pictUrl;

    private Long volume;

    private Long price;

    private Long commissionRate;

    private Date createDate;

    private UnionProductTO innerProduct;

    public static OceanMessageProductVO toVO(MaocheMessageProductIndex index) {
        if (index == null) {
            return null;
        }

        OceanMessageProductVO dto = new OceanMessageProductVO();
        dto.setId(index.getId());
        dto.setResourceId(index.getResourceId());
        dto.setAffType(index.getAffType());
        dto.setInnerId(index.getInnerId());
        dto.setTitle(index.getTitle());
        dto.setPictUrl(index.getPictUrl());
        dto.setVolume(index.getVolume());
        dto.setPrice(index.getPrice());
        dto.setCommissionRate(index.getCommissionRate());
        dto.setCreateDate(new Date(index.getCreateDate()));

        return dto;
    }


}
