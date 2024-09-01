package com.jeesite.modules.cat.model;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class BrandLibKeywordIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = -5704715345304147006L;

    private Long id;

    private Long brandLibId;

    private String keyword;

    private String categoryName;

    private String levelOneCategoryName;

    private List<Long> tags;

    private List<Long> specialTags;

    private List<String> aliasNames;

    private String status;

    private Long updateTime;

    public static BrandLibKeywordIndex toIndex(MaocheBrandLibKeywordDO keywordDO) {
        if (keywordDO == null) {
            return null;
        }

        BrandLibKeywordIndex index = new BrandLibKeywordIndex();

        index.setId(keywordDO.getIid());
        index.setBrandLibId(keywordDO.getBrandLibId());
        index.setCategoryName(keywordDO.getCategoryName());
        index.setLevelOneCategoryName(keywordDO.getLevelOneCategoryName());
        index.setTags(JsonUtils.toReferenceType(keywordDO.getTags(), new TypeReference<List<Long>>() {
        }));
        index.setSpecialTags(JsonUtils.toReferenceType(keywordDO.getSpecialTags(), new TypeReference<List<Long>>() {
        }));
        index.setAliasNames(JsonUtils.toReferenceType(keywordDO.getAliasNames(), new TypeReference<List<String>>() {
        }));
        index.setStatus(keywordDO.getStatus());
        index.setUpdateTime(keywordDO.getUpdateDate().getTime());

        return index;
    }

//    public static <T> List<T> toList(String str, Class<T> clazz) {
//        if (StringUtils.isBlank(str)) {
//            return null;
//        }
//
//        try {
//            return JSONObject.parseArray(str, clazz);
//        } catch (Exception e) {
//            log.error("转换异常 {}", str, e);
//        }
//
//        return null;
//    }

    public static Map<String, Object> toIndexMap(MaocheBrandLibKeywordDO libDO) {
        BrandLibKeywordIndex index = toIndex(libDO);
        if (index == null) {
            return null;
        }

        return JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
        });
    }
}
