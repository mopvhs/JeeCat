package com.jeesite.modules.cat.xxl.job.ocean;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.utils.excel.ExcelException;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.cg.ocean.dto.AIMessage;
import com.jeesite.modules.cat.service.cg.ocean.dto.AIMessageInfo;
import com.jeesite.modules.cat.service.cg.third.WechatRobotAdapter;
import com.jeesite.modules.cat.service.es.OceanEsService;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InterruptedIOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jeesite.modules.cat.enums.OceanStatusEnum.AI_FAILED_TIMEOUT;

@Slf4j
@Component
public class AIOceanXxlJob extends IJobHandler {

    @Resource
    private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;

    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

    @Resource
    private WechatRobotAdapter wechatRobotAdapter;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private CacheService cacheService;

    @Resource
    private OceanEsService oceanEsService;

    @Override
    @XxlJob("aiOceanXxlJob")
    public void execute() throws Exception {

        // 30分钟内车单
        long time = System.currentTimeMillis() - (30 * 60 * 1000L);
//        Date date = new Date(time);
//        Date dayFirst = DateUtils.getOfDayFirst(DateUtils.addDays(new Date(), -1));
//        String formatDate = DateUtils.formatDateTime(date);

        // 拉取最近的100条 status = finished 按照 time倒序
        List<MaocheRobotCrawlerMessageDO> dos = maocheRobotCrawlerMessageDao.listFinishedRelationMessage(null);
        if (CollectionUtils.isEmpty(dos)) {
            return;
        }

        // 分桶 <关联id, <List<消息>>
        Map<Long, List<MaocheRobotCrawlerMessageDO>> group = new HashMap<>();
        dispatchGroup(group, dos);

        // 发消息给ai
        dispatchMessage(group);


    }

    private void dispatchMessage(Map<Long, List<MaocheRobotCrawlerMessageDO>> group) {

        int max = 10;
        int i = 0;
        for (Map.Entry<Long, List<MaocheRobotCrawlerMessageDO>> entry : group.entrySet()) {

            if (i >= 10) {
                return;
            }
            List<MaocheRobotCrawlerMessageDO> value = entry.getValue();
            try {
                // 关联关系处理
                boolean res = doDispatchMessage(value);
                // todo 先处理一次
                if (res) {
                    Thread.sleep(2000);
                    i++;
                }
            } catch (Exception e) {
                log.error("AIOceanXxlJob 推送AI消息异常 value:{}", JsonUtils.toJSONString(value), e);
            }

        }

    }

    private boolean doDispatchMessage(List<MaocheRobotCrawlerMessageDO> messages) {

        // 查询关联关系下的公海消息，找到已经处理完成的（状态=NORMAL）

        // 按照时间从小到大排序
        messages = messages.stream().sorted(Comparator.comparing(i -> i.getTime().getTime())).collect(Collectors.toList());

//        List<String> statusList = new ArrayList<>();
//        statusList.add(OceanStatusEnum.SIMILAR.name());
//        statusList.add(OceanStatusEnum.NORMAL.name());
        // 查询公海是否已经全部转换完成
        List<Long> msgIds = messages.stream().map(MaocheRobotCrawlerMessageDO::getIid).toList();
        List<MaocheRobotCrawlerMessageSyncDO> syncMsgs = maocheRobotCrawlerMessageSyncDao.listRobotMsgIds(msgIds, null);
        if (CollectionUtils.isEmpty(syncMsgs)) {
            // 无数据，并且messages只有一个，而且是图片
            if (messages.size() == 1 && messages.get(0).getMsgtype().equals("2")) {
                // 直接失败
                maocheRobotCrawlerMessageDao.updateStatus(msgIds, "FAILED_TEXT_MISSED");
                return false;
            }

            // 多次不存在，直接废弃
            String key = StringUtils.join(msgIds, "_");
            String val = cacheService.get(key);
            Long incr = NumberUtils.toLong(val);
            if (incr <= 10) {
                incr = cacheService.incr(key);
                cacheService.expire(key, (int) TimeUnit.DAYS.toSeconds(1));
                log.error("查询关联关系下的公海消息获取到的数据为空 机器人消息id：{}", JsonUtils.toJSONString(msgIds));
                return false;
            }

            cacheService.delete(key);
            // 多次不存在，直接废弃
            maocheRobotCrawlerMessageDao.updateStatus(msgIds, "FAILED_SYNC_MISSED");
            return false;

        }

        Map<Long, MaocheRobotCrawlerMessageSyncDO> syncMap = syncMsgs.stream().collect(Collectors.toMap(MaocheRobotCrawlerMessageSyncDO::getRobotMsgId, Function.identity(), (o1, o2) -> o1));

        boolean match = true;
        Boolean imgMatch = null;
        int picNum = 0;
        boolean similar = false;
        boolean failed = false;
        long current = System.currentTimeMillis();
        boolean timeout = false;
        List<AIMessage> aiMessages = new ArrayList<>();

        long minutes = TimeUnit.MINUTES.toMillis(30);

//        long before30Min = System.currentTimeMillis() - 30 * 60 * 1000L;

        // 判断是否满足组合发送到AI
        long retryMsgId = 0;
        for (MaocheRobotCrawlerMessageDO message : messages) {
            AIMessage msg = new AIMessage();
            msg.setMsgId(message.getIid());
            msg.setTime(message.getTime().getTime());

            if (message.getMsgtype().equals("2")) {
                picNum++;
                msg.setContentType(2);
                String img = getImg(message.getMsg());
                // 如果没有md5直接忽略图片
                String md5 = getImgMd5(message.getMsg());
                if (StringUtils.isBlank(md5)) {
                    continue;
                }
                // todo 查询原图是否下载完成
                JSONObject messageDetail = wechatRobotAdapter.getMessageDetail(message.getToid(), message.getMsgsvrid());
                if (messageDetail == null || StringUtils.isBlank(messageDetail.getString("ext"))) {
                    // 次数判断
                    String imgKey = "imgMatch_" + message.getMsgsvrid();
                    String s = cacheService.get(imgKey);
                    long c = NumberUtils.toLong(s);
                    // 1次机会
                    if (c < 1) {
                        cacheService.incr(imgKey);
                        cacheService.expire(imgKey, 86400);
                        log.info("大图获取为空 getMsgsvrid {}", message.getMsgsvrid());
                        imgMatch = false;
                        // 调用一次接口，获取大图
                        wechatRobotAdapter.getImageDetail(message.getToid(), message.getFromgid(), message.getMsgsvrid());
                        break;
                    } else {
                        continue;
                    }

                } else {
                    img = messageDetail.getString("ext");
                }
                // {"Thumb":"http://b1.wcr222.top/ff386c874d9d22eb/2025/03/30/418af5996afb493984a851c7bd96cf77.jpg","Md5":"","length":118815, "hdlen":0}
                msg.setContent(img);
            } else {
                msg.setContentType(1);
                // 从公海获取数据
                MaocheRobotCrawlerMessageSyncDO syncDO = syncMap.get(message.getIid());
                if (syncDO == null || StringUtils.isBlank(syncDO.getMsg())) {
                    // 公海中有转换异常的数据，（todo 后续可以考虑加一次重试）
                    retryMsgId = message.getIid();
                    match = false;
                    break;
                }
                // 单子超过30分钟
                long time = syncDO.getWxTime().getTime();
                if (current - time > minutes) {
                    timeout = true;
                    break;
                }

                if (OceanStatusEnum.FAIL.name().equals(syncDO.getStatus())) {
                    // 相似的消息，直接跳过
                    failed = true;
                    break;
                }
                if (OceanStatusEnum.SIMILAR.name().equals(syncDO.getStatus())) {
                    // 相似的消息，直接跳过
                    similar = true;
                    break;
                }
                if (!OceanStatusEnum.NORMAL.name().equals(syncDO.getStatus())) {
                    // 相似的消息，直接跳过
                    failed = true;
                    break;
                }

                String syncMsg = syncDO.getMsg();

                // 移除公海固定头 尾
                //resContent = "✨有好价✨\n" + resContent;
                // resContent = resContent + "---------------------\n" + "自助查车 dwz.cn/qveM26UV";
                String replaceAll = syncMsg.replaceAll("✨有好价✨\\n", "");
                replaceAll = replaceAll.replaceAll("---------------------\\n", "");
                replaceAll = replaceAll.replaceAll("自助查车 dwz.cn/qveM26UV", "");
                msg.setContent(replaceAll);
            }
            aiMessages.add(msg);
        }
        if (imgMatch != null && !imgMatch) {
            return false;
        }
        if (timeout) {
            // 更新索引
            oceanEsService.updateRobotState(msgIds, AI_FAILED_TIMEOUT.name(), null, syncMap);

            maocheRobotCrawlerMessageDao.updateStatus(msgIds, AI_FAILED_TIMEOUT.name());
            return false;
        }
        if (failed) {
            // 更新索引
            oceanEsService.updateRobotState(msgIds, "FAILED_ANALYSIS", null, syncMap);

            maocheRobotCrawlerMessageDao.updateStatus(msgIds, "FAILED_ANALYSIS");
            return false;
        }
        if (similar) {
            oceanEsService.updateRobotState(msgIds, "FAILED_SIMILAR", null, syncMap);

            maocheRobotCrawlerMessageDao.updateStatus(msgIds, "FAILED_SIMILAR");
            return false;
        }
        if (!match) {
            if (retryMsgId > 0) {
                String key = "retry_ocean_" + retryMsgId;
                String val = cacheService.get(key);
                if (StringUtils.isBlank(val)) {
                    cacheService.incr(key);
                    cacheService.expire(key, 86400);
                } else {
                    long times = NumberUtils.toLong(val);
                    if (times > 10) {
                        // 修改状态为需要再次加工
                        maocheRobotCrawlerMessageDao.updateStatus(msgIds, "RETRY");
                        log.error("关联车单从公海查询聚合失败，存在漏单 robotMsgIds：{}，picNum:{}, sync:{}", msgIds, picNum, JSONObject.toJSONString(syncMap.keySet()));
                    } else {
                        cacheService.incr(key);
                        cacheService.expire(key, 86400);
                    }
                }
            }

            return false;
        }

        AIMessageInfo info = new AIMessageInfo();
        info.setTaskId(UUID.randomUUID().toString());
        info.setContents(aiMessages);

        // todo 发送给Ai，并且标记状态为新的状态
        String s = FlameHttpService.doPost("https://wx.mtxtool.com/maoche/enqueue_task.html", JsonUtils.toJSONString(info));

        // 更新索引
        oceanEsService.updateRobotState(msgIds, "OCEAN", null, syncMap);
        maocheRobotCrawlerMessageDao.updateStatus(msgIds, "OCEAN");

        return true;
    }


    private static String getImg(String msg) {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        return jsonObject.getString("Thumb");
    }

    private static String getImgMd5(String msg) {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        return jsonObject.getString("Md5");
    }


    private void dispatchGroup(Map<Long, List<MaocheRobotCrawlerMessageDO>> group, List<MaocheRobotCrawlerMessageDO> dos) {
        for (MaocheRobotCrawlerMessageDO messageDO : dos) {
            List<MaocheRobotCrawlerMessageDO> relations = group.get(messageDO.getRelationId());
            if (CollectionUtils.isEmpty(relations)) {
                relations = new ArrayList<>();
            }

            relations.add(messageDO);
            group.put(messageDO.getRelationId(), relations);
        }
    }
}
