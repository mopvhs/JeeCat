package com.jeesite.modules.cgcat.dto.ocean;

import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class OceanMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3161336340315210519L;

    private Long id;

    private List<String> resourceIds;

    private String affType;

    private String msg;

    private Date createDate;

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
