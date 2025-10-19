package com.jeesite.modules.cat.service.tracer;

import com.jeesite.modules.cat.model.MemberUserRespVO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TraceInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -2743375143880739884L;

    public static final String USER_ID = "uid";

    public static final String USER = "user";

    private MemberUserRespVO user;
}
