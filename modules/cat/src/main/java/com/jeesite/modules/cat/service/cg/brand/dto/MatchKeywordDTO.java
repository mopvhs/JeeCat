package com.jeesite.modules.cat.service.cg.brand.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MatchKeywordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7921723369138781050L;

    private String brandName;

    private Long libId;

    private Long keywordId;

    private String keyword;

}
