package com.jeesite.modules.cgcat.dto;

import com.jeesite.modules.cat.entity.MaocheTaskDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class TaskDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6231910600132923301L;

    private String id;

    private String title;		// 标题

    private String subTitle;		// 副标题

    private String taskType;		// 任务类型

    private String taskSwitch;		// 是否开启

    private String timeType;		// 时间类型

    private Date switchDate;		// 开关时间

    private String content;		// 内容

    private Date publishDate;	// 发布时间

    private String status;       // 状态

    private Integer finishedNum;    // 完成数量

    private Integer pushNum;    // 推送数量
}
