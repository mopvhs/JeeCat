package com.jeesite.modules.cat.model.task.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TaskRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 6489579789589390832L;

    private List<Long> productIds;

    private String taskId;

    private String title;

    private String subTitle;

    private String timeType;

    private Date publishDate;	// 发布时间

    // 同步创建子任务
    private List<PushTaskCreateRequest> pushTaskCreateDetails;
}
