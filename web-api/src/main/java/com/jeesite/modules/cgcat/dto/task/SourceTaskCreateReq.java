package com.jeesite.modules.cgcat.dto.task;

import com.jeesite.modules.cat.enums.task.TaskConstants;
import com.jeesite.modules.cat.enums.task.TimeTypeEnum;
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
     * 1：立即发布
     * 2：自定义
     * 3：5分钟
     * 4：10分钟
     * 5：重复任务的每日定时
     * 任务类型(通过他计算timeType)
     */
    private Integer displayTimeType;

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
    /**
     * {@link TaskConstants.TaskSource}
     */
    private String source;

    // 发布延迟时间，毫秒
    private long delayTime;

    private Date publishDate;

    // 任务详情
    private TaskDetail detail;

    public void initPublishTime() {
        if (displayTimeType == null) {
            timeType = TimeTypeEnum.NOW.name();
            publishDate = new Date();
            return;
        }

        switch (displayTimeType) {
            case 1:
                timeType = TimeTypeEnum.NOW.name();
                break;
            case 2:
                timeType = TimeTypeEnum.SCHEDULE.name();
                break;

            case 3:
                timeType = TimeTypeEnum.NOW.name();
                publishDate = new Date();
                delayTime = 5 * 60 * 1000;
                break;

            case 4:
                timeType = TimeTypeEnum.NOW.name();
                publishDate = new Date();
                delayTime = 10 * 60 * 1000;
                break;

            case 5:
                timeType = TimeTypeEnum.REPEAT_DAILY_SCHEDULE.name();
                break;

            default:
                timeType = TimeTypeEnum.NOW.name();
                break;

        }
    }


}
