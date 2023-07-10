package com.jeesite.modules.cat.xxl.job;

import com.jeesite.modules.cat.service.cg.CgUnionProductStatisticsService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 9.9元推荐
 */
@Slf4j
@Component
public class CgProductNineRcmdXxlJob extends IJobHandler {

    @Resource
    private CgUnionProductStatisticsService cgUnionProductStatisticsService;

    @Override
    @XxlJob("cgProductNineRcmdXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("cgProductNineRcmdXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        cgUnionProductStatisticsService.nineRcmd();

        stopWatch.stop();

        XxlJobHelper.log("cgProductNineRcmdXxlJob xxl job end 耗时：" + stopWatch.toString());
    }
}
