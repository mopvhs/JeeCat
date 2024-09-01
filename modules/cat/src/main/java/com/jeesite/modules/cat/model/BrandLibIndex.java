package com.jeesite.modules.cat.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class BrandLibIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = -5704715345304147006L;

    private Long id;

    private Long brandId;

    private String productName;

    private List<String> aliasNames;

    private String status;

    private Long updateTime;

    public static BrandLibIndex toIndex(MaocheBrandLibDO libDO) {
        if (libDO == null) {
            return null;
        }

        BrandLibIndex index = new BrandLibIndex();

        index.setId(libDO.getIid());
        index.setBrandId(libDO.getBrandId());
        index.setProductName(libDO.getProductName());
        index.setAliasNames(JsonUtils.toReferenceType(libDO.getAliasNames(), new TypeReference<List<String>>() {
        }));
        index.setStatus(libDO.getStatus());
        index.setUpdateTime(libDO.getUpdateDate().getTime());

        return index;
    }

    public static Map<String, Object> toIndexMap(MaocheBrandLibDO libDO) {
        BrandLibIndex index = toIndex(libDO);
        if (index == null) {
            return null;
        }

        return JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
        });
    }
}
