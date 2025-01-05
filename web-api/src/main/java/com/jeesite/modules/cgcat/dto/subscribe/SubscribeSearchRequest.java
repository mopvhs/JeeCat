package com.jeesite.modules.cgcat.dto.subscribe;

import com.jeesite.modules.cat.enums.subscribe.SubscribeTypeEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SubscribeSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -8614271256979255973L;

    private String userId;

    private String subType = SubscribeTypeEnum.BRAND_LIB_KEYWORD.getType();

    private String keyword;
}
