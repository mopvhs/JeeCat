package com.jeesite.modules.cat.service.cg.task.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class NameDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String id;

    // 优惠券 || 购买链接 类似的span名称
    private String title;

    private String name;

    private String content;

    private String desc;

    private boolean show = true;
}