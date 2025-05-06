package com.jeesite.modules.cat.service.cg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.entity.MaocheSyncDataInfoDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.MaocheSyncDataInfoService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.stage.cg.ocean.AbstraOceanStage;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanStage;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanContentHelper;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanMonitorHelper;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.OceanUpContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.OceanUpStage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OceanSyncService {

    @Resource
    private MaocheSyncDataInfoService maocheSyncDataInfoService;

    @Resource
    private MaocheRobotCrawlerMessageService maocheRobotCrawlerMessageService;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

//    @Resource
//    private OceanStage tbOceanStage;
//
//    @Resource
//    private OceanStage jdOceanStage;

    @Resource
    private OceanUpStage tbUpOceanStage;

    @Resource
    private OceanUpStage jdUpOceanStage;

    @Resource
    private CacheService cacheService;

    public Result<String> robotMsg() {
        /*MaocheSyncDataInfoDO dataInfo = maocheSyncDataInfoService.getLatestSyncDataInfo("maoche_robot_crawler_message");
        long syncDataId = 0;
        long maxId = 0;
        int step = 50;
        if (dataInfo != null) {
            syncDataId = dataInfo.getIid();
            maxId = NumberUtils.toLong(dataInfo.getSyncMaxId());
            step = Optional.ofNullable(dataInfo.getStep()).orElse(step);
            if (maxId <= 0) {
                return Result.ERROR(400, "同步数据异常，最大同步id为0");
            }
        }

        step = 10;

        List<String> affTypes = new ArrayList<>();
        affTypes.add("tb");
        affTypes.add("jd");
        // 查询数据
        List<MaocheRobotCrawlerMessageDO> messages = maocheRobotCrawlerMessageService.startById(maxId, step, affTypes);
        if (CollectionUtils.isEmpty(messages)) {
            return Result.OK("暂无数据");
        }

        List<MaocheRobotCrawlerMessageSyncDO> crawlerMessages = new ArrayList<>();
        Date date = new Date();
        String offset = String.valueOf(messages.get(messages.size() - 1).getId());
        // 一个一个的解析
        for (MaocheRobotCrawlerMessageDO message : messages) {
            // afftype 干预订正
            String affType = message.getAffType();
            String msg = message.getMsg();
            affType = AbstraOceanStage.fixAffType(msg, affType);
            message.setAffType(affType);

            MaocheRobotCrawlerMessageSyncDO sync = new MaocheRobotCrawlerMessageSyncDO();

            sync.setMsg(message.getMsg());
            sync.setProcessed(0L);
            sync.setResourceIds("");
            sync.setUniqueHash(message.getUniqueHash());
            sync.setOriUniqueHash(message.getUniqueHash());
            sync.setWxTime(message.getTime());
            sync.setAffType(affType);
            sync.setRobotMsgId(message.getIid());
            sync.setCreateBy("");
            sync.setCreateDate(date);
            sync.setUpdateBy("");
            sync.setUpdateDate(date);
            sync.setRemarks("");
            sync.setStatus("INIT");

            String robotMsgNumKey = OceanMonitorHelper.getRobotMsgNumKey(affType);
            cacheService.incr(robotMsgNumKey);
            Result<List<MaocheRobotCrawlerMessageSyncDO>> res = maocheRobotCrawlerMessageSyncService.addIfAbsentV2(sync, 3);

            if (Result.isOK(res)) {
                crawlerMessages.add(sync);
                if (CollectionUtils.isNotEmpty(res.getResult())) {
                    String robotMsgSameNumKey = OceanMonitorHelper.getRobotMsgSameNumKey(affType);
                    cacheService.incr(robotMsgSameNumKey);
                    log.info("robotMsg messageSync is exist uniqueHash:{}", sync.getUniqueHash());
                }
            } else if (res != null && res.getCode() == 300) {
                crawlerMessages.addAll(res.getResult());
            } else {
                log.info("robotMsg messageSync is fail {}", sync.getUniqueHash());
            }
        }

        if (CollectionUtils.isNotEmpty(crawlerMessages)) {
            // 需要写索引
            List<Map<String, Object>> messageSyncIndex = OceanContentHelper.getMessageSyncIndex(crawlerMessages, messages);
            elasticSearch7Service.index(messageSyncIndex, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", step);
        }

        List<Long> collect = crawlerMessages.stream().map(MaocheRobotCrawlerMessageSyncDO::getRobotMsgId).collect(Collectors.toList());
        log.info("robotMsg {}", JsonUtils.toJSONString(collect));

        // 更新位点
        maocheSyncDataInfoService.addOrUpdateOffset(syncDataId, "maoche_robot_crawler_message", offset);*/

        return Result.OK("操作完成");
    }

    public Result<String> sync() {
        MaocheSyncDataInfoDO dataInfo = maocheSyncDataInfoService.getLatestSyncDataInfo("maoche_robot_crawler_message");
        long syncDataId = 0;
        long maxId = 0;
        int step = 50;
        if (dataInfo != null) {
            syncDataId = dataInfo.getIid();
            maxId = NumberUtils.toLong(dataInfo.getSyncMaxId());
            step = Optional.ofNullable(dataInfo.getStep()).orElse(step);
            if (maxId <= 0) {
                return Result.ERROR(400, "同步数据异常，最大同步id为0");
            }
        }

        step = 10;

        List<String> affTypes = new ArrayList<>();
        affTypes.add("tb");
        affTypes.add("jd");
        // 查询数据
        List<MaocheRobotCrawlerMessageDO> messages = maocheRobotCrawlerMessageService.startById(maxId, step, affTypes);
        if (CollectionUtils.isEmpty(messages)) {
            return Result.OK("暂无数据");
        }

        String offset = String.valueOf(messages.get(messages.size() - 1).getId());
        // 一个一个的解析
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (MaocheRobotCrawlerMessageDO message : messages) {
//            offset = String.valueOf(message.getId());
                    // afftype干预订正
//                    String affType = message.getAffType();
//                    String msg = message.getMsg();
//                    affType = AbstraOceanStage.fixAffType(msg, affType);
//                    message.setAffType(affType);
//                    try {
//                        OceanContext context = new OceanContext(message);
//                        if (affType.equals("tb")) {
//                            tbOceanStage.process(context);
//                        } else if (affType.equals("jd")) {
//                            jdOceanStage.process(context);
//                        }
//                    } catch (Exception e) {
//                        break;
//                    }
                };
            }
        }).start();

        // 更新位点
        maocheSyncDataInfoService.addOrUpdateOffset(syncDataId, "maoche_robot_crawler_message", offset);

        return Result.OK("操作完成");
    }

    public void analysis() {
        // 查询 init状态的数据,分tb和jd

        List<MaocheRobotCrawlerMessageSyncDO> tbs = maocheRobotCrawlerMessageSyncService.listStatusAffType(OceanStatusEnum.INIT, "tb", 10);

        List<MaocheRobotCrawlerMessageSyncDO> jds = maocheRobotCrawlerMessageSyncService.listStatusAffType(OceanStatusEnum.INIT, "jd", 10);

        // 线程单独处理
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (MaocheRobotCrawlerMessageSyncDO tb : tbs) {
                    try {
                        OceanUpContext context = new OceanUpContext(tb);
                        tbUpOceanStage.process(context);
                    } catch (Exception e) {
                        break;
                    }
                }
            }
        }).start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                for (MaocheRobotCrawlerMessageSyncDO jd : jds) {
                    try {
                        OceanUpContext context = new OceanUpContext(jd);
                        jdUpOceanStage.process(context);
                    } catch (Exception e) {
                        break;
                    }
                }
            }
        }).start();

    }


}
