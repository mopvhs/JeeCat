package com.jeesite.modules.cgcat.dto;

import com.jeesite.modules.cat.model.UnionProductTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class PushTaskDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 3076347574713153841L;

    private String id;

    private String status;

    private UnionProductTO product;

    private String content;

    private Date finishedDate;

    private Date publishDate;

    private String pushType;
}
