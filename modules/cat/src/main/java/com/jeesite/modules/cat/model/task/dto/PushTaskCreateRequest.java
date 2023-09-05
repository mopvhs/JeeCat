package com.jeesite.modules.cat.model.task.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建推送任务的详情信息
 */
@Data
public class PushTaskCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1333182917563987669L;

    private String title;

    private String pushType;

    private String resourceId;

    private String resourceType;

    // 发布时间
    private String PublishDate;

    private String detail;

    private String img;

}
