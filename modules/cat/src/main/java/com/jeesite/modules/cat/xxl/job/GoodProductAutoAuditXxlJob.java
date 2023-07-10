package com.jeesite.modules.cat.xxl.job;

import com.jeesite.modules.cat.service.cg.AutoProductService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 有好价商品库自动入库
 */
@Slf4j
@Component
public class GoodProductAutoAuditXxlJob extends IJobHandler {

    @Resource
    private AutoProductService autoProductService;

    @Override
    @XxlJob("goodProductAutoAuditXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("goodProductAutoAuditXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            autoProductService.autoGoodProductAudit();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        stopWatch.stop();

        XxlJobHelper.log("goodProductAutoAuditXxlJob xxl job end 耗时：" + stopWatch.toString());
    }
}
