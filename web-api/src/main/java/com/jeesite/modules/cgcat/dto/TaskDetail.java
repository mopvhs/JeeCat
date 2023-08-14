package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class TaskDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 8839472228226038841L;

    /**
     * 任务名称
     */
    private String title;

    /**
     * 推送名称
     */
    private String subTitle;

    /**
     * 发布方式
     * {@link com.jeesite.modules.cat.enums.task.TimeTypeEnum}
     */
    private String timeType;

    /**
     * 发布时间
     */
    private Date publishDate;


}
