package com.jeesite.modules.cgcat.dto.subscribe;

import com.jeesite.modules.cgcat.dto.CategoryVO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class LibInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3069179335612908916L;

    private List<CategoryVO> categories;

    private List<LibKeywordVO> keywords;

    private long total;
}
