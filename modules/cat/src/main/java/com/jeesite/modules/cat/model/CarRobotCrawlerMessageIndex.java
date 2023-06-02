package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class CarRobotCrawlerMessageIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = -5946899978779689561L;

    private Long id;
    private String fromGid;
    private String fromId;
    private String toId;
    private String msg;
    private String msgNew;
    private String imageUrl;
    private String msgSvrId;
    private String fromType;
    private String msgType;
    private Date time;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private Long processed;

}
