package com.jeesite.modules.cat.service.cg.brandlib.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class BrandLibKeywordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8419728145249936532L;

    private Long id;

    private String keyword;

    // 是否是金标（true是，false否）
    private boolean goldMark;

    private List<String> aliasNames;

    private List<TagDTO> tags;

    private Integer subCnt;

    // 历史车单
    private Integer historyOrderCnt;

    private Date recentOrder;

    private String categoryName;

    private String levelOneCategoryName;

    public static BrandLibKeywordDTO convert(MaocheBrandLibKeywordDO keywordDO) {
        if (keywordDO == null) {
            return null;
        }
        BrandLibKeywordDTO dto = new BrandLibKeywordDTO();

        dto.setId(keywordDO.getIid());
        dto.setKeyword(keywordDO.getKeyword());
        dto.setCategoryName(keywordDO.getCategoryName());
        dto.setLevelOneCategoryName(keywordDO.getLevelOneCategoryName());

        if (StringUtils.isNotBlank(keywordDO.getAliasNames())) {
            dto.setAliasNames(JsonUtils.toReferenceType(keywordDO.getAliasNames(), new TypeReference<List<String>>() {
            }));
        }



        return dto;
    }

}
