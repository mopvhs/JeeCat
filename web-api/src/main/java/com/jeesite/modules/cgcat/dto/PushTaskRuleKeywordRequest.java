package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PushTaskRuleKeywordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2998492883357036286L;

    @NotNull(message = "关键词不能为空")
    // 关键词
    private List<String> keywords;
}
