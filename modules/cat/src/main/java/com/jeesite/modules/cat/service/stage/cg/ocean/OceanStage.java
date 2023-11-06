package com.jeesite.modules.cat.service.stage.cg.ocean;

import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;

public interface OceanStage {

    // tb/jd
    String getAffType();

    // 流程处理
    void process(OceanContext context);

    // 构建基础的消息结构
    void buildBaseMessageSync(OceanContext context);
    // 构建基础的消息结构前提下的自定义消息结构干预
    void customBuildMessage(OceanContext context);
    /**
     * 构建查询的商品数据
     * @param context
     */
    void buildBaseMessageProducts(OceanContext context);

    /**
     * 为商品信息填错消息信息
     * @param message
     * @param productDO
     */
    void fillMessageInfo2Product(MaocheRobotCrawlerMessageSyncDO message, MaocheRobotCrawlerMessageProductDO productDO);

    /**
     * 查询第三方接口获取商品数据
     * @param context
     */
    void queryProductFromThirdApi(OceanContext context);

    /**
     * 保存消息和商品数据
     */
    void saveMessageAndProduct(OceanContext context);

    /**
     * 构建索引
     * @param context
     */
    void indexEx(OceanContext context);



}
