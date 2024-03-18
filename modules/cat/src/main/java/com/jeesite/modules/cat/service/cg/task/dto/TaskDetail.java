package com.jeesite.modules.cat.service.cg.task.dto;

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


}
