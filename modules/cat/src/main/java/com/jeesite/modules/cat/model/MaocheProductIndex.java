package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class MaocheProductIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = -5946899978779689561L;

    private Long id;
    private String content;
    private String itemId;
    private String itemIdSuffix;
    private String uniqueHash;
    private String contentNew;
    private String title;
    private String status;
    private long syncTime;
    private long affLinkConvTime;
    private long createTime;
    private long updateTime;
    private long processed;
    private String affType;
    private String imageUrl;

}
