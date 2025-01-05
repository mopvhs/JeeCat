package com.jeesite.modules.cgcat.dto.subscribe;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LibKeywordHomeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6428508767849058214L;

    private LibKeywordVO keyword;

    private String descTitle;

    private String desc;
}
