package com.jeesite.modules.cat.model.task.content;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PushTaskContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 3619517226687520758L;

    private List<Long> ids;

    private List<Long> filterIds;

    // 来源
    private String source;

    // 发布延迟时间，相对的
    private Long delayTime;

    private Integer displayTimeType;

}
