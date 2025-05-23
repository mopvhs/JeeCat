package com.jeesite.modules.cat.service.stage.cg.ocean.v2;

import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.cg.third.tb.dto.GeneralConvertResp;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.stage.cg.ocean.SimilarContext;
import com.jeesite.modules.cat.service.toolbox.dto.CommandContext;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class OceanUpContext implements Serializable {

    @Serial
    private static final long serialVersionUID = -2039141823189513925L;

    public OceanUpContext() {
    }

    public OceanUpContext(MaocheRobotCrawlerMessageSyncDO messageSync) {
        this.messageSync = messageSync;
    }
    private MaocheRobotCrawlerMessageDO robotMsg;
    // 中间处理结果
    // 洗完数据后的消息
    private MaocheRobotCrawlerMessageSyncDO messageSync;
    // 解析出来的商品
    private List<MaocheRobotCrawlerMessageProductDO> messageProducts;

    // 淘宝api商品
    private Map<String, GeneralConvertResp> tbProductMap;

    // 京东api商品 (京东的消息一般会一条消息多个商品)
    private List<JdUnionIdPromotion> jdProducts;

    // 京东api商品 (京东的消息一般会一条消息多个商品)
    // 转链详情
    private CommandContext commandContext;

    /**
     * 凑单-是否忽略相似判断
     */
    private boolean ignoreSimHash;

    /**
     * 券-是否忽略相似判断
     */
    private boolean couponIgnoreSimHash;

    // 是否只存在特殊的uri
    // y-03.cn   3.cn    jd.cn   t.cn    q5url.cn    kurl06.cn
    private boolean onlySpecialUri = false;

    // 查询失败remarks
    private String failRemarks;

    // 相似的消息
    private List<MaocheMessageSyncIndex> similarMessages;

    // 是否索引成功
    private boolean indexResult = false;

    private SimilarContext similar;


}
