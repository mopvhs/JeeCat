package com.jeesite.modules.cat.service.cg.brand;

import com.jeesite.modules.cat.aop.MaocheBrandIndex;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class BrandDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -615209905054769402L;

    private String name;

    private String firstSpell;

    private String icon;

    private Long id;

    public static List<BrandDTO> convert(List<MaocheBrandIndex> indices) {

        if (CollectionUtils.isEmpty(indices)) {
            return new ArrayList<>();
        }

        List<BrandDTO> dtos = new ArrayList<>();
        for (MaocheBrandIndex index : indices) {
            BrandDTO convert = convert(index);

            if (convert == null) {
                continue;
            }
            dtos.add(convert);
        }

        return dtos;
    }

    public static BrandDTO convert(MaocheBrandIndex index) {
        if (index == null) {
            return null;
        }

        BrandDTO dto = new BrandDTO();
        dto.setId(index.getId());
        dto.setName(index.getBrand());
        dto.setFirstSpell(index.getFirstSpell());
        dto.setIcon(index.getIcon());

        return dto;

    }
}
