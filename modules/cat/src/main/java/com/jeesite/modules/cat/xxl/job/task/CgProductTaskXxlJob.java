package com.jeesite.modules.cat.xxl.job.task;

import com.jeesite.modules.cat.service.cg.CgUnionProductStatisticsService;
import com.jeesite.modules.cat.service.cg.task.PushTaskBizService;
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
public class CgProductTaskXxlJob extends IJobHandler {

    @Resource
    private PushTaskBizService pushTaskBizService;

    @Override
    @XxlJob("cgProductTaskXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("CgProductTaskXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        pushTaskBizService.push();

        stopWatch.stop();

        XxlJobHelper.log("CgProductTaskXxlJob xxl job end 耗时：" + stopWatch.toString());
    }
}
