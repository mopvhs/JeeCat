package com.jeesite.modules.cat.service.cg.task.dto;

import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class TaskDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 8698512082705516349L;

    // sku
    private List<ProductDetail> products;

    // 描述
    private String desc;

    // 专题 -》 列表 保存对象
    private List<String> topics;

    // 活动券
    private List<NameDetail> actCoupons;

    // 凑单品-》列表  标题，口令
    private List<NameDetail> addOnProducts;

    // 小程序
    private NameDetail miniProgram;

    // 作业图 -》 列表
    private List<String> jobImgs;

    // 曝光（小程序1，双端全发2）
    private Integer exposureRange;

    /**
     * 所属类别
     */
    private List<NameDetail> catTypes;

    /**
     * 项目前缀
     */
    private List<NameDetail> projectTags;

    /**
     * 关联关键词
     */
    private List<BelongLibDTO> libs;
}
