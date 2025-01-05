package com.jeesite.modules.cgcat.dto.subscribe;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SubscribeHomeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -9067693565478220839L;

    /**
     * 我的订阅
     */
    private List<SubscribeHomeDetailVO> blocks;

    /**
     * 推荐订阅
     */
    private SubscribeHomeDetailVO rcmdBlock;
}
