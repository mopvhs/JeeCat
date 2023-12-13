package com.jeesite.modules.cgcat.dto.task;

import com.jeesite.modules.cat.service.cg.task.dto.TaskDetail;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SourceTaskCreateReq implements Serializable {

    @Serial
    private static final long serialVersionUID = -7655536578625160955L;

    private String taskId;

    private String pushTaskId;

    private String title;

    /**
     * 任务类型
     * {@link com.jeesite.modules.cat.enums.task.TimeTypeEnum}
     */
    private String timeType;

    /**
     * {@link com.jeesite.modules.cat.enums.task.PushTypeEnum}
     */
    private String pushType;

    // 来源  ocean(公海)、product(选品库)、
    private String source;

    // 发布延迟时间
    private long delayTime;

    private Date publishDate;

    // 任务详情
    private TaskDetail detail;



}
