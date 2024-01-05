package com.jeesite.modules.cat.service.cg.task.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class TaskInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -6720817226008441222L;

    private String taskId;
    private String title;		// 标题
    private String taskSwitch;		// 是否开启
    private Date switchDate;		// 开关的时间

    /**
     * {@link com.jeesite.modules.cat.enums.task.TimeTypeEnum}
     */
    private String timeType;		// 时间类型
    private Date publishDate;	// 发布时间
    private Long delayTime;   // 发布延迟时间 （5分钟，10分钟这种）

    // 有好价 有豪车
    private String pushType;		// 推送类型

    private String status;       // 状态

    private String pushTaskId;      // 推送任务id
    private TaskDetail detail;      // 详情

    private Integer displayTimeType;

}
