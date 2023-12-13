package com.jeesite.modules.cgcat.dto.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class TaskDetailGetReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 3270431124453277094L;

    private String taskId;

    // 创建来源（选品，公海等）
    private String source;

    // 来源id(公海的话就是消息id)
    private String sourceId;

    // tb/jd
    private String affType;

    // 商品id
    private List<String> resourceIds;



}
