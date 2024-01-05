package com.jeesite.modules.cgcat.dto.topic;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TopicSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3831030018783569380L;

    private String id;

    private String keyword;

    private int page;

    private int pageSize;
}
