package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2684552611479531109L;

    private List<Brand> brands;

    private long total;

    private boolean hasMore;

    @Data
    public static class Brand implements Serializable {

        @Serial
        private static final long serialVersionUID = -1553004425434072665L;

        private Long id;

        private String brand;
    }
}
