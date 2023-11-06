package com.jeesite.modules.cat.service.stage.cg.ocean.exception;

import lombok.Data;

import java.io.Serial;

@Data
public class QueryThirdApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8541126031250211812L;

    // 查询失败，无法获取到商品信息，查询失败
    public static final String QUERY_FAIL = "QUERY_FAIL";

    private String action;

    public QueryThirdApiException() {

    }

    public QueryThirdApiException(String action, String message) {
        super(message);
        this.action = action;
    }

}
