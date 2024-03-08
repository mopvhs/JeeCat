package com.jeesite.modules.cgcat.dto;

import com.jeesite.modules.cgcat.dto.ocean.OceanMessageVO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibPageDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1449870799385457736L;

    /**
     * 5星新增
     */
    private List<OceanMessageVO> superStarOceans;


    /**
     * 品牌库-今日已发布任务数量
     */
    private long brandLibTaskCnt;

    /**
     * 品牌库-昨日已发布任务数量
     */
    private long yesterdayBrandLibTaskCnt;

    private List<CategoryVO> categories;


    @Data
    public static class CategoryVO implements Serializable {

        @Serial
        private static final long serialVersionUID = -7419734725694144052L;

        private String name;

        private Long total;

        private Long today;

        private Long yesterday;

        // 定量
        private Long quantity;
    }

}
