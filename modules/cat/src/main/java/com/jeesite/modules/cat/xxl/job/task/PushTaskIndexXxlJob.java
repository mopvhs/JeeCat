package com.jeesite.modules.cat.xxl.job.task;

import com.google.common.collect.Lists;
import com.jeesite.common.lang.TimeUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.dao.MaochePushTaskDao;
import com.jeesite.modules.cat.entity.MaochePushTaskRuleDO;
import com.jeesite.modules.cat.service.MaochePushTaskRuleService;
import com.jeesite.modules.cat.service.MaochePushTaskService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibService;
import com.jeesite.modules.cat.service.es.BrandLibEsService;
import com.jeesite.modules.cat.service.es.TaskEsService;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 推送任务全量索引
 */
@Slf4j
@Component
public class PushTaskIndexXxlJob extends IJobHandler {

    @Resource
    private MaochePushTaskService maochePushTaskService;

    @Resource
    private MaochePushTaskDao maochePushTaskDao;

    @Resource
    private TaskEsService taskEsService;

    @Override
    @XxlJob("pushTaskIndexXxlJob")
    public void execute() throws Exception {

        Date startTime = new Date();

        int num = 0;
        String id = "0";
        int limit = 100;
        boolean breakFlag = false;
        while (true) {
            List<String> ids = maochePushTaskDao.getIds(id, limit);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            id = ids.get(ids.size() - 1);
            num += ids.size();
            if (ids.size() < limit) {
                breakFlag = true;
            }
            List<List<String>> partition = Lists.partition(ids, 20);
            for (List<String> p : partition) {
                try {
                    // 操作
                    taskEsService.indexEs(p, 10);
                } catch (Exception e) {
                    log.error("推送任务全量索引失败 ids:{}", JsonUtils.toJSONString(p), e);
                }
            }

            if (breakFlag) {
                break;
            }
        }
        Date endTime = new Date();

        long left = endTime.getTime() - startTime.getTime();

        String msg = "推送任务全量索引成功。\n整体耗时：{}分钟\n总数据量为：{}\n开始时间为：{}\n结束时间为：{}";
        DingDingService.sendParseDingDingMsg(msg, TimeUtils.formatTime(left), num, DateTimeUtils.getStringDate(startTime), DateTimeUtils.getStringDate(endTime));
    }
}
