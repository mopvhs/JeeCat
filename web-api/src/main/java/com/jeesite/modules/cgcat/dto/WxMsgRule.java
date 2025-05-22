package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class WxMsgRule implements Serializable {

    private static final long serialVersionUID = 4756451824451723304L;

    // 最少字数限制
    private Integer minNumOfWords;

    // 最多字数限制
    private Integer maxNumOfWords;

    // 关键词
    private List<String> keywords;

    // 会话id，包括群和人
    private List<String> convIds;

    // 黑名单关键词
    private List<String> blackKeywords;

    // 消息类型
    private List<Integer> contentTypes;

    // aiUrl
    private String aiUrl;

    // 是否记录过滤日志
    private boolean filterLog = false;

    private boolean log = false;

    // 秒
    private long msgOfflineTime;

    private Map<String, AutoRelation> autoRelationMap;

    @Data
    public static class AutoRelation implements Serializable {

        private static final long serialVersionUID = 1L;

        // convId
        private String friendId;

        private String fromId;

        private List<String> fromIds;

        // 此内容类型的才要
        private List<Integer> contentTypes;
    }

}
