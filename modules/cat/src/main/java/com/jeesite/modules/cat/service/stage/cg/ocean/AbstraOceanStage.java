package com.jeesite.modules.cat.service.stage.cg.ocean;

import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.es.OceanEsService;
import com.jeesite.modules.cat.service.stage.cg.ocean.exception.QueryThirdApiException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;

@Slf4j
public abstract class AbstraOceanStage implements OceanStage {

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private OceanEsService oceanEsService;

    @Override
    public void process(OceanContext context) {

        // 1. 构建基础的消息结构
        buildBaseMessageSync(context);

        try {
            // 2. 查询第三方接口获取商品数据
            queryProductFromThirdApi(context);

            // 3. 保存商品数据到消息中
            buildBaseMessageProducts(context);

            // 4. 保存商品数据
            saveMessageAndProduct(context);

            // 5. 构建索引
            indexEx(context);
        } catch (QueryThirdApiException e) {
            String action = e.getAction();
            // 查询失败后，是否需要保存消息
            if (QueryThirdApiException.QUERY_FAIL.equals(action)) {
                saveFailQueryProduct(context);
                return;
            }
            log.error("查询第三方接口获取商品数据失败 message :{}", JsonUtils.toJSONString(context.getCrawlerMessage()), e);
        } catch (Exception e) {
            log.error("公海流程处理异常 message :{}", JsonUtils.toJSONString(context.getCrawlerMessage()), e);
        }
    }

    @Override
    public void buildBaseMessageSync(OceanContext context) {

        MaocheRobotCrawlerMessageDO message = context.getCrawlerMessage();

        // 1. 构建基础的消息结构

        MaocheRobotCrawlerMessageSyncDO sync = new MaocheRobotCrawlerMessageSyncDO();

        sync.setMsg(message.getMsg());
        sync.setProcessed(0L);
        sync.setResourceIds("");
        sync.setUniqueHash(message.getUniqueHash());
        sync.setWxTime(message.getTime());
        sync.setAffType(message.getAffType());
        sync.setRobotMsgId(message.getIid());
        sync.setCreateBy("admin");
        sync.setCreateDate(message.getCreateTime());
        sync.setUpdateBy("admin");
        sync.setUpdateDate(new Date());
        sync.setRemarks("");
        sync.setStatus("INIT");

        // 写入到context中
        context.setMessageSync(sync);
        customBuildMessage(context);
    }

    @Override
    public void customBuildMessage(OceanContext context) {
        return;
    }

    public void saveFailQueryProduct(OceanContext context) {
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        if (messageSync == null) {
            log.error("保存失败的查询商品数据失败, messageSync is null");
            return;
        }
        messageSync.setStatus("FAIL");
        messageSync.setRemarks(context.getFailRemarks());
        maocheRobotCrawlerMessageSyncService.save(messageSync);
    }

    @Override
    public void indexEx(OceanContext context) {
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        if (messageSync == null || messageSync.getUiid() == null || messageSync.getUiid() == 0L) {
            log.error("索引构建失败, messageSync is null");
            return;
        }

        oceanEsService.indexEs(Collections.singletonList(messageSync.getUiid()), 10);
    }
}
