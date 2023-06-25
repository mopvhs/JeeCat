package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ProductRobotResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -3876823693769299369L;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 图片地址
     */
    private String img;

    /**
     * 跳转协议
     */
    private String targetUrl;
}
