package com.jeesite.modules.cgcat.dto.subscribe;

import com.jeesite.modules.cat.entity.MaocheSubscribeDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SubscribeDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7875494648298773407L;

    private Long keywordId;

    private String title;

    /**
     * 优先展示别名
     */
    private List<String> subTitles;

    private String icon;

    private String logo;

    /**
     * 是否开启 OPEN 开启， CLOSE 未开启
     */
    private String openSwitch;
}
