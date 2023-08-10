package com.jeesite.modules.cat.model.task.content;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PushTaskContentDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 3619517226687520758L;

    private String detail;

    private String img;

}
