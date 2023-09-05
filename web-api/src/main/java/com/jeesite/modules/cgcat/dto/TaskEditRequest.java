package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class TaskEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -6778366001072499403L;

    private String id;

    private String title;

    private String subTitle;

    // 开关状态
    private String taskSwitch;

    /**
     * 发布时间
     */
    private Date publishDate;

    /**
     * 发布时间类型
     * {@link com.jeesite.modules.cat.enums.task.TimeTypeEnum}
     */
    private String timeType;
}
