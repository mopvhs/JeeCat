package com.jeesite.modules.cat.service.stage.cg.ocean;

import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponse;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class OceanContext implements Serializable {

    @Serial
    private static final long serialVersionUID = -2039141823189513925L;

    public OceanContext() {
    }

    public OceanContext(MaocheRobotCrawlerMessageDO crawlerMessage) {
        this.crawlerMessage = crawlerMessage;
    }

    private MaocheRobotCrawlerMessageDO crawlerMessage;

    // 中间处理结果
    // 洗完数据后的消息
    private MaocheRobotCrawlerMessageSyncDO messageSync;
    // 解析出来的商品
    private List<MaocheRobotCrawlerMessageProductDO> messageProducts;

    // 淘宝api商品
    private CommandResponse tbProduct;

    // 京东api商品 (京东的消息一般会一条消息多个商品)
    private List<JdUnionIdPromotion> jdProducts;

    // 查询失败remarks
    private String failRemarks;

    // 相似的消息
    private List<MaocheMessageSyncIndex> similarMessages;

    // 是否索引成功
    private boolean indexResult = false;


}
