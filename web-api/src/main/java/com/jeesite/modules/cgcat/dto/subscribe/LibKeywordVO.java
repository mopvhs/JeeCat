package com.jeesite.modules.cgcat.dto.subscribe;

import cn.hutool.core.map.MapUtil;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheTagDO;
import com.jeesite.modules.cat.model.BrandLibKeywordIndex;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class LibKeywordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5763772302699118460L;

    private Long id;

    private String logo;

    private String icon;

    private String title;

    private List<String> subTitle;

    private Long subscribeCount;

    /**
     * 是否已经订阅
     */
    private Boolean subscribed;

    /**
     * 订阅信息需要额外查询注入
     * @param list
     * @param brandMap
     * @param tagMap
     * @return
     */
    public static List<LibKeywordVO> toVO(List<BrandLibKeywordIndex> list, Map<Long, MaocheBrandDO> brandMap, Map<Long, MaocheTagDO> tagMap) {
        if (CollectionUtils.isEmpty(list) || MapUtil.isEmpty(brandMap)) {
            return new ArrayList<>();
        }

        List<LibKeywordVO> vos = new ArrayList<>();
        for (BrandLibKeywordIndex index : list) {
            if (index == null || brandMap.get(index.getBrandId()) == null) {
                continue;
            }
            LibKeywordVO vo = new LibKeywordVO();
            MaocheBrandDO brandDO = brandMap.get(index.getBrandId());
            if (CollectionUtils.isNotEmpty(index.getSpecialTags()) && index.getSpecialTags().contains(1L)) {
                vo.setIcon("todo");
            }
            vo.setId(index.getId());
            vo.setLogo(brandDO.getIcon());
            vo.setTitle(index.getKeyword());
            vo.setSubTitle(getSubTitles(index, tagMap));
            vo.setSubscribeCount(index.getSubscribeCount());
            // 订阅信息需要额外查询注入

            vos.add(vo);
        }

        return vos;
    }

    private static List<String> getSubTitles(BrandLibKeywordIndex index, Map<Long, MaocheTagDO> tagMap) {
        if (CollectionUtils.isNotEmpty(index.getAliasNames())) {
            return index.getAliasNames();
        }
        if (CollectionUtils.isEmpty(index.getTags())) {
            return new ArrayList<>();
        }
        List<String> subTitles = new ArrayList<>();
        List<Long> tagIds = index.getTags().stream().distinct().toList();
        for (Long tagId : tagIds) {
            MaocheTagDO tagDO = tagMap.get(tagId);
            if (tagDO == null) {
                continue;
            }
            subTitles.add(tagDO.getTagName());
        }

        return subTitles;
    }
}
