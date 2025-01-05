package com.jeesite.modules.cat.model.brandlib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.text.PinyinUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

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

    private String firstSpell;

    private String productName;

    private List<String> aliasNames;

    private String status;

    // 关键词数量
    private Long keywordCnt;

    private Long updateTime;

    public static BrandLibIndex toIndex(MaocheBrandLibDO libDO, MaocheBrandDO brandDO, List<MaocheBrandLibKeywordDO> keywords) {
        if (libDO == null) {
            return null;
        }

        long keywordCnt = 0;
        if (CollectionUtils.isNotEmpty(keywords)) {
            keywordCnt = keywords.size();
        }

        BrandLibIndex index = new BrandLibIndex();

        index.setId(libDO.getIid());
        index.setBrandId(libDO.getBrandId());
        index.setProductName(brandDO.getName());
        // 获取首字母
        String firstSpell = PinyinUtils.getFirstSpell(libDO.getProductName());
        if (StringUtils.isNotBlank(firstSpell)) {
            firstSpell = StringUtils.upperCase(firstSpell.substring(0, 1));
        }
        if (brandDO != null && StringUtils.isNotBlank(brandDO.getFirstSpell())) {
            firstSpell = brandDO.getFirstSpell();
        }
        index.setFirstSpell(firstSpell);
        index.setKeywordCnt(keywordCnt);

        index.setAliasNames(JsonUtils.toReferenceType(libDO.getAliasNames(), new TypeReference<List<String>>() {
        }));
        index.setStatus(libDO.getStatus());
        index.setUpdateTime(libDO.getUpdateDate().getTime());

        return index;
    }

    public static Map<String, Object> toIndexMap(MaocheBrandLibDO libDO, MaocheBrandDO brandDO, List<MaocheBrandLibKeywordDO> keywords) {
        BrandLibIndex index = toIndex(libDO, brandDO, keywords);
        if (index == null) {
            return null;
        }

        return JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
        });
    }
}
