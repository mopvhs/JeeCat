package com.jeesite.modules.cgcat.dto;

import com.jeesite.modules.cat.model.UnionProductTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ProductPushVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2146440586452151625L;

    /**
     * 下一页的cursor
     */
    private int cursor;

    private Long total;

    private String tips;

    private List<UnionProductTO> products;


}
