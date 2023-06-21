package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MaocheProductSyncRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long id;

    private List<Long> ids;
}
