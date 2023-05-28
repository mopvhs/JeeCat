package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class HistorySearchKeywordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1154398780454449009L;
    
    private List<String> keywords;
}
