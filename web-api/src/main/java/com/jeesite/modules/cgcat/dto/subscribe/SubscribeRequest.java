package com.jeesite.modules.cgcat.dto.subscribe;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SubscribeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -8614271256979255973L;

    private String userId;

    private Long subId;

    private String subType = "brand_lib_keyword";

    /**
     * true：订阅
     * false：取消订阅
     */
    private boolean subscribe;
}
