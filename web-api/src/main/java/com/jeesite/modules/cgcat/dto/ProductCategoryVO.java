package com.jeesite.modules.cgcat.dto;

import com.jeesite.modules.cat.model.CatProductBucketTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ProductCategoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1832558853691888045L;

    private Long total;

    private List<CatProductBucketTO> categories;
}
