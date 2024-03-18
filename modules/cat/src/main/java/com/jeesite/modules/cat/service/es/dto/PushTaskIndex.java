package com.jeesite.modules.cat.service.es.dto;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.enums.task.TaskResourceTypeEnum;
import com.jeesite.modules.cat.model.task.content.PushTaskContent;
import com.jeesite.modules.cat.model.task.content.PushTaskContentDetail;
import com.jeesite.modules.cat.service.cg.task.dto.NameDetail;
import com.jeesite.modules.cat.service.cg.task.dto.ProductDetail;
import com.jeesite.modules.cat.service.cg.task.dto.TaskDetail;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public class PushTaskIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = 1088931852952890817L;

    private String id;

    // 任务id
    private String taskId;

    // 推送任务名称
    private String title;

    /**
     * {@link com.jeesite.modules.cat.enums.task.PushTypeEnum}
     */
    private String pushType;

    // 推送任务完成时间
    private Long finishTime;

    // 推送任务计划时间
    private Long publishedTime;

    // 创建时间
    private Long createTime;

    // 资源id
    private String resourceId;

    // 来源
    private String source;

    /**
     * {@link TaskResourceTypeEnum}
     */
    private String resourceType;

    // 推送任务状态 push task维度
    private String status;

    // 推送任务消息
    private String contentMsg;

    // 任务状态 task维度
    private String taskStatus;

    // 任务开关
    private String taskSwitch;

    // 任务标题
    private String taskTitle;

    /**
     * 任务时间类型
     * {@link com.jeesite.modules.cat.enums.task.TimeTypeEnum}
     */
    private String taskTimeType;

    // 全部推送任务完成时间
    private Long taskFinishedTime;

    private long displayTimeType;

    private long delayTime;

    private List<Long> brandLibIds;

    private List<String> categorys;

    public static PushTaskIndex toIndex(MaochePushTaskDO push, MaocheTaskDO task) {
        if (push == null || task == null) {
            return null;
        }

        String content = push.getContent();

        PushTaskContentDetail detail = JsonUtils.toReferenceType(content, new TypeReference<PushTaskContentDetail>() {
        });
        if (detail == null) {
            return null;
        }

        TaskDetail taskProductDetail = null;
        if (StringUtils.isNotBlank(push.getDetail())) {
            taskProductDetail = JsonUtils.toReferenceType(push.getDetail(), new TypeReference<TaskDetail>() {
            });
        }

        PushTaskContent taskContent = JsonUtils.toReferenceType(task.getContent(), new TypeReference<PushTaskContent>() {
        });
        String source = "";
        int displayTimeType = 0;
        long delayTime = 0;
        if (taskContent != null) {
            displayTimeType = Optional.ofNullable(taskContent.getDisplayTimeType()).orElse(0);
            delayTime = Optional.ofNullable(taskContent.getDelayTime()).orElse(0L);
            source = Optional.ofNullable(taskContent.getSource()).orElse("");
        }

        PushTaskIndex index = new PushTaskIndex();
        index.setId(push.getId());
        index.setTaskId(push.getTaskId());
        index.setTitle(push.getTitle());
        index.setPushType(push.getPushType());
        index.setFinishTime(getTime(push.getFinishedDate()));
        index.setPublishedTime(getTime(push.getPublishDate()));
        index.setCreateTime(getTime(push.getCreateDate()));
        index.setResourceId(push.getResourceId());
        index.setResourceType(push.getResourceType());
        index.setStatus(push.getStatus());
        index.setContentMsg(detail.getDetail());
        index.setTaskStatus(task.getStatus());
        index.setTaskSwitch(task.getTaskSwitch());
        index.setTaskTitle(task.getTitle());
        index.setTaskTimeType(task.getTimeType());
        index.setTaskFinishedTime(getTime(task.getFinishedDate()));
        index.setSource(source);
        index.setDisplayTimeType(displayTimeType);
        index.setDelayTime(delayTime);

        index.setBrandLibIds(new ArrayList<>());
        if (taskProductDetail != null) {
            if (CollectionUtils.isNotEmpty(taskProductDetail.getTopics())) {
                List<Long> brandLibIds = taskProductDetail.getTopics().stream().map(NumberUtils::toLong).filter(i -> i > 0).distinct().collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(brandLibIds)) {
                    index.setBrandLibIds(brandLibIds);
                }
            }
            List<ProductDetail> products = taskProductDetail.getProducts();
            if (CollectionUtils.isNotEmpty(products)) {
                List<String> categoryNames = products.stream().map(ProductDetail::getCategoryName).filter(StringUtils::isNotBlank).toList();
                index.setCategorys(categoryNames);
            }
        }


        return index;
    }

    public static long getTime(Date time) {
        if (time == null) {
            return 0;
        }

        return time.getTime();
    }
}
