package com.jeesite.modules.cgcat.dto;

import com.jeesite.modules.cat.model.UnionProductTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class PushTaskEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -6778366001072499403L;

    private String id;

    private String title;

    private String status;

    private String detail;

//    private Date finishedDate;

//    private Date publishDate;

//    private String pushType;
}
