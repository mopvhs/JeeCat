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
 * 商品自动下架
 */
@Slf4j
@Component
public class ProductAutoOfflineXxlJob extends IJobHandler {

    @Resource
    private AutoProductService autoProductService;

    @Override
    @XxlJob("productAutoOfflineXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("productAutoOfflineXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            autoProductService.autoOfflineProduct();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        stopWatch.stop();

        XxlJobHelper.log("productAutoOfflineXxlJob xxl job end 耗时：" + stopWatch.toString());
    }
}
