package com.jeesite.modules.cat.xxl.job.task;

import com.google.common.collect.Lists;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.TimeUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步最近100条相似消息到公海索引
 */
@Component
public class SyncOceanSimilarXxlJob extends IJobHandler {

    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Override
    @XxlJob("syncOceanSimilarXxlJob")
    public void execute() throws Exception {

        Date startTime = new Date();
        // 今天0点
        String date = DateTimeUtils.getStringDate(new Date(DateTimeUtils.earliestByDay(new Date())));

        // 获取最新的100条相似消息
        List<MaocheRobotCrawlerMessageSyncDO> messageSyncDOS = maocheRobotCrawlerMessageSyncDao.listSimilar(date, 100);
        if (CollectionUtils.isEmpty(messageSyncDOS)) {
            return;
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (MaocheRobotCrawlerMessageSyncDO item : messageSyncDOS) {
            Map<String, Object> messageSyncIndex = new HashMap<>();
            messageSyncIndex.put("id", item.getUiid());
            messageSyncIndex.put("status", OceanStatusEnum.SIMILAR.name());
            data.add(messageSyncIndex);
        }

        elasticSearch7Service.update(data, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);

        Date endTime = new Date();

        long left = endTime.getTime() - startTime.getTime();

        String msg = "同步最近100条相似消息到公海索引完成。\n整体耗时：{}分钟\n总数据量为：{}\n开始时间为：{}\n结束时间为：{}";
        DingDingService.sendParseDingDingMsg(msg, TimeUtils.formatTime(left), messageSyncDOS.size(), DateTimeUtils.getStringDate(startTime), DateTimeUtils.getStringDate(endTime));
    }
}
