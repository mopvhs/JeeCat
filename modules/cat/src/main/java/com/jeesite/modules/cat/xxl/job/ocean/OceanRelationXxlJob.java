package com.jeesite.modules.cat.xxl.job.ocean;

import com.jeesite.common.lang.NumberUtils;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.service.es.OceanEsService;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OceanRelationXxlJob extends IJobHandler {

    @Resource
    private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;

    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

    @Resource
    private OceanEsService oceanEsService;

    @Override
    @XxlJob("oceanRelationXxlJob")
    public void execute() throws Exception {

        // 拉取最近的50条 status = special 按照 time倒序
        List<MaocheRobotCrawlerMessageDO> dos = maocheRobotCrawlerMessageDao.listRelationMessage();
        if (CollectionUtils.isEmpty(dos)) {
            return;
        }

        // 分桶 <群, <人，List<消息>>>
        Map<String, Map<String, List<MaocheRobotCrawlerMessageDO>>> group = new HashMap<>();
        dispatchGroup(group, dos);

        // 判断消息的关联关系
        relationship(group);


    }

    private void relationship(Map<String, Map<String, List<MaocheRobotCrawlerMessageDO>>> group) {

        for (Map.Entry<String, Map<String, List<MaocheRobotCrawlerMessageDO>>> entry : group.entrySet()) {

            String key = entry.getKey();
            Map<String, List<MaocheRobotCrawlerMessageDO>> value = entry.getValue();
            for (Map.Entry<String, List<MaocheRobotCrawlerMessageDO>> msgEntry : value.entrySet()) {
                // 关联关系处理
                doRelationship(msgEntry.getValue());
            }

        }

    }

    private void doRelationship(List<MaocheRobotCrawlerMessageDO> messages) {

        long relationId = 0L;
        long lastMsgTime = 0L;

        long ms = TimeUnit.MINUTES.toMillis(1);
        List<MaocheRobotCrawlerMessageDO> relations = new ArrayList<>();
        for (MaocheRobotCrawlerMessageDO msg: messages) {
            int msgType = NumberUtils.toInt(msg.getMsgtype());

            if (relationId == 0) {
                relationId = msg.getIid();
            }

            // 如果是图片。默认是新的一条消息的开始
            boolean start = msgType == 2;
            long left = msg.getTime().getTime() - lastMsgTime;

            // 如果是图片，默认是新的开始！ 或者和上一条的时间，超过1min
            if (start || (lastMsgTime > 0 && left > ms)) {
                // 如果暂存队列里面有消息需要把他们写入同一个关系
                if (CollectionUtils.isNotEmpty(relations)) {
                    // 写入关联关系
                    List<Long> ids = relations.stream().map(MaocheRobotCrawlerMessageDO::getIid).toList();
                    maocheRobotCrawlerMessageDao.relationMessage(ids, relationId);
                }

                relations = new ArrayList<>();
                // 重置位点状态
                relationId = msg.getIid();
                relations.add(msg);
            } else {
                // 不确定是否是新的开始，先放到暂存列表
                relations.add(msg);
            }

            lastMsgTime = msg.getTime().getTime();
        }

        // 如果还有relations，需要判断是否已经距离现在很久了
        if (CollectionUtils.isNotEmpty(relations)) {
            MaocheRobotCrawlerMessageDO messageDO = relations.get(relations.size() - 1);
            long left = System.currentTimeMillis() - messageDO.getTime().getTime() - (2 * ms);
            // 最好一条距离现在已经超过2分钟，可以直接认定为一条新的关系
            if (left >= 0) {
                List<Long> ids = relations.stream().map(MaocheRobotCrawlerMessageDO::getIid).toList();
                relationId = relations.get(0).getIid();
                maocheRobotCrawlerMessageDao.relationMessage(ids, relationId);
                // 更新索引
                // 查询sync
                List<MaocheRobotCrawlerMessageSyncDO> syncMsgs = maocheRobotCrawlerMessageSyncDao.listRobotMsgIds(ids, null);
                Map<Long, MaocheRobotCrawlerMessageSyncDO> syncMap = syncMsgs.stream().collect(Collectors.toMap(MaocheRobotCrawlerMessageSyncDO::getRobotMsgId, Function.identity(), (o1, o2) -> o1));

                oceanEsService.updateRobotState(ids, null, relationId, syncMap);

            }
        }

    }

    private void dispatchGroup(Map<String, Map<String, List<MaocheRobotCrawlerMessageDO>>> group, List<MaocheRobotCrawlerMessageDO> dos) {
        for (MaocheRobotCrawlerMessageDO messageDO : dos) {
            Map<String, List<MaocheRobotCrawlerMessageDO>> userMessageMap = group.get(messageDO.getFromgid());
            if (MapUtils.isEmpty(userMessageMap)) {
                userMessageMap = new HashMap<>();
            }
            List<MaocheRobotCrawlerMessageDO> list = userMessageMap.get(messageDO.getFromid());
            if (CollectionUtils.isEmpty(list)) {
                list = new ArrayList<>();
            }
            list.add(messageDO);

            userMessageMap.put(messageDO.getFromid(), list);
            group.put(messageDO.getFromgid(), userMessageMap);
        }
    }
}
