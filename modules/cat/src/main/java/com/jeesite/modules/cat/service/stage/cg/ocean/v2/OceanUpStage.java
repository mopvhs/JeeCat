package com.jeesite.modules.cat.service.stage.cg.ocean.v2;

import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanContext;

import java.util.regex.Pattern;

public interface OceanUpStage {

    // tb/jd
    String getAffType();

    Pattern getPattern();

    // 流程处理
    void process(OceanUpContext context);

    // 构建基础的消息结构
    void buildBaseMessageSync(OceanUpContext context);
    // 构建基础的消息结构前提下的自定义消息结构干预
    void customBuildMessage(OceanUpContext context);
    /**
     * 构建查询的商品数据
     * @param context
     */
    void buildBaseMessageProducts(OceanUpContext context);

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
    void queryProductFromThirdApi(OceanUpContext context);

    /**
     * 保存消息和商品数据
     */
    void saveMessageAndProduct(OceanUpContext context);

    /**
     * 构建索引
     * @param context
     */
    void indexEx(OceanUpContext context);

    /**
     * 相似文案判断
     */
    void similarMsgCheck(OceanUpContext context);

    /**
     * 计算相似内容code
     * @param context
     */
    void calSimilar(OceanUpContext context);



}
