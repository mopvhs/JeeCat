package com.jeesite.modules.cat.es.config.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ElasticSearchData<T, A> implements Serializable {

    @Serial
    private static final long serialVersionUID = -5019211522862444714L;

//    private RestStatus restStatus;

    private long total;

    private List<T> documents;

    private Map<String, List<A>> bucketMap;
}
