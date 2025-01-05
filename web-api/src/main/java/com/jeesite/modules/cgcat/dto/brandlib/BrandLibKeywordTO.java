package com.jeesite.modules.cgcat.dto.brandlib;

import com.jeesite.modules.cat.model.TagTree;
import com.jeesite.modules.cgcat.dto.TagTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibKeywordTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1078729539083212998L;

    private String keywordId;

    private List<String> aliasNames;

    private List<TagTO> tags;
}
