package com.jeesite.modules.cat.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum OceanStatusEnum {

    INIT("初始值", "ocean"),
    NORMAL("正常", "ocean"),
    FAIL("失败", "ocean"),
    SIMILAR("相似内容", "ocean"),
    EXCEPTION("异常", "ocean"),

    SPECIAL("AI车单-待关联", "ai_ocean"),
    FINISHED("AI车单-关联完成", "ai_ocean"),
    OCEAN("AI车单-待AI解析", "ai_ocean"),
    FAILED_SIMILAR("AI车单-公海相似失败", "ai_ocean"),
    FAILED_ANALYSIS("AI车单-公海解析失败", "ai_ocean"),
    AI_FAILED_TIMEOUT("AI车单-超时忽略", "ai_ocean"),
    AI_ANALYSIS_FAIL("AI车单-静默处理", "ai_ocean"),
    AI_FINISHED("AI车单-发群成功", "ai_ocean"),
    ;

    private String desc;

    private String group;

    OceanStatusEnum (String desc, String group) {
        this.desc = desc;
        this.group = group;
    }

    public static List<String> listGroup(String group) {
        if (StringUtils.isBlank(group)) {
            return null;
        }

        List<String> status = new ArrayList<>();
        for (OceanStatusEnum obe : values()) {
            if (group.equals(obe.getGroup())) {
                status.add(obe.name());
            }
        }
        return status;
    }

    public static String getStatusDesc(String status) {
        if (StringUtils.isBlank(status)) {
            return "未知";
        }
        for (OceanStatusEnum obe : values()) {
            if (obe.name().equals(status)) {
                return obe.getDesc();
            }
        }
        return "未知";
    }
}
