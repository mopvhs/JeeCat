package com.jeesite.modules.cat.xxl.job.ocean;

import com.jeesite.modules.cat.service.cg.OceanSyncService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 采集数据同步进公海
 */
@Slf4j
@Component
public class CgRobotMessageSyncXxlJob extends IJobHandler {

    @Resource
    private OceanSyncService oceanSyncService;

    @Override
    @XxlJob("cgRobotMessageSyncXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("CgRobotMessageSyncXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        oceanSyncService.robotMsg();

        stopWatch.stop();

        XxlJobHelper.log("cgRobotMessageSyncXxlJob xxl job end 耗时：" + stopWatch.toString());
    }
}
