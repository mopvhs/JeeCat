package com.jeesite.modules.cgcat;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.cg.ocean.dto.AIMessage;
import com.jeesite.modules.cat.service.cg.ocean.dto.AIMessageInfo;
import com.jeesite.modules.cat.service.message.QyWeiXinService;
import com.jeesite.modules.cat.service.stage.cg.ocean.AbstraOceanStage;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanContentHelper;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanMonitorHelper;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.OceanUpContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.OceanUpStage;
import com.jeesite.modules.cgcat.dto.ocean.SyncOceanMsgVO;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}")
public class SyncOceanController {

    // 查询
    @Resource
    private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;

    @Resource
    private CacheService cacheService;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private OceanUpStage tbUpOceanStage;

    @Resource
    private OceanUpStage jdUpOceanStage;

    @Resource
    private QyWeiXinService qyWeiXinService;

    // 线程池
    private static ExecutorService threadPool = new ThreadPoolExecutor(32, 32,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
            new DefaultThreadFactory("ocean-sync-message"));

    /**
     * 同步&分析公海
     *
     * @param query
     * @return
     */
    @RequestMapping(value = "/api/ocean/msg/sync/analysis")
    public Result<Boolean> syncAndAnalysisOceanMessage(@RequestBody SyncOceanMsgVO query) {

        if (query == null || query.getRobotMsgId() == null) {
            return Result.ERROR(500, "参数错误");
        }

        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                MaocheRobotCrawlerMessageDO message = maocheRobotCrawlerMessageDao.getById(query.getRobotMsgId());
                if (message == null) {
                    return;
                }
                List<MaocheRobotCrawlerMessageSyncDO> crawlerMessages = new ArrayList<>();
                Date date = new Date();

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
                } else {
                    log.info("robotMsg messageSync is fail {}", sync.getUniqueHash());
                }

                if (CollectionUtils.isNotEmpty(crawlerMessages)) {
                    // 需要写索引
                    List<Map<String, Object>> messageSyncIndex = OceanContentHelper.getMessageSyncIndex(crawlerMessages);
                    elasticSearch7Service.index(messageSyncIndex, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);
                }

                for (MaocheRobotCrawlerMessageSyncDO syncDO : crawlerMessages) {
                    try {
                        OceanUpContext context = new OceanUpContext(syncDO);
                        if (affType.equals("tb")) {
                            tbUpOceanStage.process(context);
                        } else if (affType.equals("jd")) {
                            jdUpOceanStage.process(context);
                        }

                    } catch (Exception e) {
                        break;
                    }
                }

            }
        });


        return Result.OK(true);
    }


    /**
     * AI处理回调
     *
     * @return
     */
    @RequestMapping(value = "/api/ocean/ai/callback")
    public Result<Boolean> oceanCallBack(@RequestBody AIMessageInfo aiMessageInfo) {

        log.info("oceanCallBack request {}", JsonUtils.toJSONString(aiMessageInfo));
        if (aiMessageInfo == null || CollectionUtils.isEmpty(aiMessageInfo.getContents())) {
            return Result.ERROR(500, "参数错误");
        }

        // todo
        String url = "http://wx-rpa.mtxtool.com:48080/app-api/tbl/wx/message/send";

        String token = "e1a061c5-6623-484b-a156-17a03da7bbe5";
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                List<String> texts = new ArrayList<>();
                // 判断单子是否可以发
                for (AIMessage aiMessage : aiMessageInfo.getContents()) {
                    int contentType = aiMessage.getContentType();
                    if (contentType == 2) {
                        continue;
                    } else {
                        if (StringUtils.isEmpty(aiMessage.getAiContent())) {
                            return;
                        }
                    }
                }
                for (AIMessage aiMessage : aiMessageInfo.getContents()) {
                    int contentType = aiMessage.getContentType();
                    if (contentType == 2) {
                        String img = aiMessage.getContent();
                        Result<String> imgRes = qyWeiXinService.sendImage(img, token);
                        if (!Result.isOK(imgRes)) {
                            // 判断错误码，40009，表示图片过大，需要压缩
                            if (imgRes.getCode() != null && imgRes.getCode() == 40009) {
                                img += "_500x500.jpg";
                                // 重新发送
                                imgRes = qyWeiXinService.sendImage(img, token);
                                log.info("二次发送图片，img:{}, imgRes: {}",img, imgRes);
                            }
                        }
                    } else {
                        if (StringUtils.isEmpty(aiMessage.getAiContent())) {
                            continue;
                        }
                        String content = aiMessage.getAiContent();
                        content = content + "\n---------------------\n" + "自助查车 dwz.cn/qveM26UV";
                        qyWeiXinService.sendText(content, token);
                    }
                }

//                    int contentType = aiMessage.getContentType();
//                    if (contentType == 2) {
//                        // 把AI的内容，发群
//                        // http://127.0.0.1:48080/app-api/tbl/wx/message/send
//
//                        Map<String, Object> message = new HashMap<>();
//                        message.put("wechatId", "wxid_5pjb7cws8bv922");
//                        // 测试群Id - 猫车自动化测试
//                        message.put("friendId", "56689803450@chatroom");
//                        message.put("content", aiMessage.getContent());
//                        message.put("contentType", 2);
//                        FlameHttpService.doPost(url, JsonUtils.toJSONString(message));
//                    } else {
//
//                        String content = aiMessage.getContent() + "\n" + aiMessage.getAiContent();
//                        texts.add(content);
//                    }
//                }
//                if (CollectionUtils.isNotEmpty(texts)) {
//
//                    String sendText = StringUtils.join(texts, "|\n|\n|\n|\n|\n|\n");
//                    Map<String, Object> message = new HashMap<>();
//                    message.put("wechatId", "wxid_5pjb7cws8bv922");
//                    // 测试群Id - 猫车自动化测试
//                    message.put("friendId", "56689803450@chatroom");
//                    message.put("content", aiMessageInfo.getTaskId() + "\n" + sendText);
//                    message.put("contentType", 1);
//                    FlameHttpService.doPost(url, JsonUtils.toJSONString(message));
                    //
            }
        });


        return Result.OK(true);
    }
}
