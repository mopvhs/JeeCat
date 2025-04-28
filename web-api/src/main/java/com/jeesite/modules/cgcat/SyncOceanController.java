package com.jeesite.modules.cgcat;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.cg.ocean.dto.AIMessage;
import com.jeesite.modules.cat.service.cg.ocean.dto.AIMessageInfo;
import com.jeesite.modules.cat.service.cg.third.WechatRobotAdapter;
import com.jeesite.modules.cat.service.message.QyWeiXinService;
import com.jeesite.modules.cat.service.stage.cg.ocean.AbstraOceanStage;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanContentHelper;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanMonitorHelper;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.AbstraUpOceanStage;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.OceanUpContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.OceanUpStage;
import com.jeesite.modules.cat.service.toolbox.CommandService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Resource
    private WechatRobotAdapter wechatRobotAdapter;

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

        MaocheRobotCrawlerMessageDO message = maocheRobotCrawlerMessageDao.getById(query.getRobotMsgId());
        if (message == null) {
            return Result.ERROR(500, "参数错误");

        }
        List<MaocheRobotCrawlerMessageSyncDO> crawlerMessages = new ArrayList<>();
        List<MaocheRobotCrawlerMessageDO> robotMessages = new ArrayList<>();
        robotMessages.add(message);

        Date date = new Date();

        // afftype 干预订正
        String affType = message.getAffType();
        String msg = message.getMsg();
        affType = AbstraOceanStage.fixAffType(msg, affType);
        message.setAffType(affType);

        MaocheRobotCrawlerMessageSyncDO sync = new MaocheRobotCrawlerMessageSyncDO();
        msg = OceanContentHelper.interposeMsg(msg);

        boolean ignoreSimHash = msg.contains("凑单参考");

        // 是否需要忽略相似度判断
        boolean couponIgnoreSimHash = ignoreSimHash(affType, msg);

        String status = OceanStatusEnum.INIT.name();
        if (ignoreSimHash || couponIgnoreSimHash) {
            status = OceanStatusEnum.NORMAL.name();
        }

        String uniqueHash = AbstraUpOceanStage.doCalSimHash(msg);

        sync.setMsg(message.getMsg());
        sync.setProcessed(0L);
        sync.setResourceIds("");
        sync.setUniqueHash(uniqueHash);
        sync.setOriUniqueHash(uniqueHash);
        sync.setWxTime(message.getTime());
        sync.setAffType(affType);
        sync.setRobotMsgId(message.getIid());
        sync.setCreateBy("");
        sync.setCreateDate(date);
        sync.setUpdateBy("");
        sync.setUpdateDate(date);
        sync.setRemarks("");
        sync.setStatus(status);
        sync.setProductHash("");

        String robotMsgNumKey = OceanMonitorHelper.getRobotMsgNumKey(affType);
        cacheService.incr(robotMsgNumKey);
        Result<List<MaocheRobotCrawlerMessageSyncDO>> res = maocheRobotCrawlerMessageSyncService.addIfAbsentV2(sync, 3);

        if (Result.isOK(res)) {
            crawlerMessages.add(sync);
            if (CollectionUtils.isNotEmpty(res.getResult())) {
                String robotMsgSameNumKey = OceanMonitorHelper.getRobotMsgSameNumKey(affType);
                cacheService.incr(robotMsgSameNumKey);
                cacheService.expire(robotMsgSameNumKey, 86400);
                log.info("robotMsg messageSync is exist uniqueHash:{}", sync.getUniqueHash());
            }
        } else {
            log.info("robotMsg messageSync is fail {}", sync.getUniqueHash());
        }

        if (CollectionUtils.isNotEmpty(crawlerMessages)) {
            // 需要写索引
            List<Map<String, Object>> messageSyncIndex = OceanContentHelper.getMessageSyncIndex(crawlerMessages, robotMessages);
            elasticSearch7Service.index(messageSyncIndex, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);
        }

        String finalAffType = affType;
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                for (MaocheRobotCrawlerMessageSyncDO syncDO : crawlerMessages) {
                    if (syncDO.getStatus().equals(OceanStatusEnum.SIMILAR.name())) {
                        log.info(String.format("robotMsg messageSync is exist uniqueHash:%s", syncDO.getUniqueHash()));
                        continue;
                    }
                    try {
                        OceanUpContext context = new OceanUpContext(syncDO);
                        context.setIgnoreSimHash(ignoreSimHash);
                        context.setCouponIgnoreSimHash(ignoreSimHash);
                        context.setRobotMsg(message);
                        if (finalAffType.equals("tb")) {
                            tbUpOceanStage.process(context);
                        } else if (finalAffType.equals("jd")) {
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
     * msg是处理过的
     * @param affType
     * @param msg
     * @return
     */
    private boolean ignoreSimHash(String affType, String msg) {
        if (!"tb".equals(affType)) {
            return false;
        }

        if (!msg.contains("券")) {
            return false;
        }

        Pattern coupon = Pattern.compile("[0-9]+");

        int num = 0;
        String[] split = msg.split("\n");
        List<Integer> coupons = new ArrayList<>();
        // 判断是否带口令或者是链接，忽略
        for (String line : split) {
            Matcher matcher = CommandService.tb.matcher(line);
            if (matcher.find()) {
                num++;
                continue;
            }
            Matcher mc = coupon.matcher(line);
            while (mc.find()) {
                coupons.add(Integer.parseInt(mc.group()));
            }
        }

        if (num != 1) {
            return false;
        }
//        // 只有一个口令+包含凑单参考
//        if (containAddOrder) {
//            return true;
//        }

        if (CollectionUtils.isEmpty(coupons)) {
            return false;
        }
        // 是否同时包含140 10或者88 5
        if (CollectionUtils.isNotEmpty(coupons)) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) {
        String line = "先领140-10/88-5券";
        Pattern coupon = Pattern.compile("[0-9]+");
        Matcher mc = coupon.matcher(line);

        while (mc.find()) {
            String group = mc.group();
            System.out.println(group);
        }
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
//        String token = "e1a061c5-6623-484b-a156-17a03da7bbe5"; // 测试
        String token = "42ee18fe-52e9-4d53-aed5-1412d2ee5d1b"; // 正式
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
                        if (aiMessage.getAiContent().contains("审核未通过，保持静默") || aiMessage.getAiContent().contains("保持静默") || aiMessage.getAiContent().contains("审核未通过")) {
                            return;
                        }
                    }
                }
                for (AIMessage aiMessage : aiMessageInfo.getContents()) {
                    int contentType = aiMessage.getContentType();
                    if (contentType == 2) {
//                        MaocheRobotCrawlerMessageDO message = maocheRobotCrawlerMessageDao.getById(aiMessage.getMsgId());
//                        if (message != null) {
//                            JSONObject messageDetail = wechatRobotAdapter.getMessageDetail(message.getToid(), message.getMsgsvrid());
//
//                        }


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

                        content = content.replaceAll("---------------------\\n", "");
                        content = content.replaceAll("自助查车 dwz.cn/qveM26UV", "");

                        String tail = "---------------------\n" + "自助查车 dwz.cn/qveM26UV";
                        if (content.charAt(content.length() - 1) == '\n') {
                            content = content + tail;
                        } else {
                            content = content + "\n" + tail;
                        }

                        qyWeiXinService.sendText(content, token);
                    }
                }
            }
        });


        return Result.OK(true);
    }
}
