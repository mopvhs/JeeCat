package com.jeesite.modules.cgcat.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.model.task.content.PushTaskContentDetail;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class PushTaskDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 3076347574713153841L;

    private String id;

    private String status;

    private String title;

    private String subTitle;

    private UnionProductTO product;

    private String detail;

    private String img;

    private Date finishedDate;

    private Date publishDate;

    private String pushType;

    public static PushTaskDetail convert(MaochePushTaskDO pushTaskDO) {
        if (pushTaskDO == null) {
            return null;
        }
        PushTaskDetail detail = new PushTaskDetail();
        detail.setId(pushTaskDO.getId());
        detail.setTitle(pushTaskDO.getTitle());
        detail.setSubTitle(pushTaskDO.getSubTitle());
        detail.setStatus(pushTaskDO.getStatus());
        detail.setFinishedDate(pushTaskDO.getFinishedDate());
        detail.setPublishDate(pushTaskDO.getPublishDate());
        detail.setPushType(pushTaskDO.getPushType());

        PushTaskContentDetail pushTaskContent = JsonUtils.toReferenceType(pushTaskDO.getContent(), new TypeReference<PushTaskContentDetail>() {
        });
        if (pushTaskContent != null) {
            detail.setDetail(pushTaskContent.getDetail());
            detail.setImg(pushTaskContent.getImg());
        }

        return detail;
    }
}
