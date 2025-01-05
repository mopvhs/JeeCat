package com.jeesite.modules.cgcat.dto.subscribe;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SubscribeHomeDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8303688228117330067L;

    private String title;

    private List<SubscribeDetailVO> details;
}
