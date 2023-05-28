package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductCategoryModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 701051922917841442L;

    private List<Long> cid1s = new ArrayList<>();

    private List<Long> cid2s = new ArrayList<>();

    private List<Long> cid3s = new ArrayList<>();
}
