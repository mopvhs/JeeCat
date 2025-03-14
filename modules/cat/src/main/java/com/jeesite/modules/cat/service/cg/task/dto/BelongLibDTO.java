package com.jeesite.modules.cat.service.cg.task.dto;

import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.service.cg.brand.dto.MatchKeywordDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BelongLibDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8189093152658370857L;

    private Long libId;
//    private Long brandId;
    private Long keywordId;
    private String brandName; // 品牌名称
    private String keyword; // 命中的关键词
//    private String matchWord; // 匹配的词语

    public static BelongLibDTO toDTO(MatchKeywordDTO keywordDO) {
        BelongLibDTO dto = new BelongLibDTO();

        dto.setLibId(keywordDO.getLibId());
//        dto.setBrandId(keywordDO.getBrandName());
        dto.setKeywordId(keywordDO.getKeywordId());
        dto.setBrandName(keywordDO.getBrandName());
        dto.setKeyword(keywordDO.getKeyword());

        return dto;
    }
}
