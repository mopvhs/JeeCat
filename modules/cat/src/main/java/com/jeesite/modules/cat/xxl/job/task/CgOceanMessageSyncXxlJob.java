package com.jeesite.modules.cat.xxl.job.task;

import com.jeesite.modules.cat.service.cg.CgUnionProductStatisticsService;
import com.jeesite.modules.cat.service.cg.OceanSyncService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 每日推荐
 */
@Slf4j
@Component
public class CgOceanMessageSyncXxlJob extends IJobHandler {

    @Resource
    private OceanSyncService oceanSyncService;

    @Override
    @XxlJob("cgOceanMessageSyncXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("cgOceanMessageSyncXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        oceanSyncService.sync();

        stopWatch.stop();

        XxlJobHelper.log("cgOceanMessageSyncXxlJob xxl job end 耗时：" + stopWatch.toString());
    }
}
