package com.jeesite.modules.robot;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SearchRequest<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -763068353194798715L;

    private int from = 0;

    private int size = 10;

    private T condition;

    // 格式：如果没有，默认desc
    // updateTime desc
    // updateTime asc
    private List<String> sorts;
}
