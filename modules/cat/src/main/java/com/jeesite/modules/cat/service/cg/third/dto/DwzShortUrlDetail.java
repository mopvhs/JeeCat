package com.jeesite.modules.cat.service.cg.third.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

@Data
public class DwzShortUrlDetail implements Serializable {

    private static final long serialVersionUID = -4361659073794798489L;

    @JSONField(defaultValue = "ShortUrl")
    private String shortUrl;
}
