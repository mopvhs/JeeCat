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

    /**
     * {@link com.jeesite.modules.cat.enums.task.PushTypeEnum}
     */
    private String pushType;

    private String detail;

    private String img;
}
