package com.jeesite.modules.cat.xxl.job;

import com.dtk.fetch.client.DtkFetchClient;
import com.jeesite.modules.cat.service.cg.DtkConsumer;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DaTaoKeConsumerXxlJob extends IJobHandler {

    @Resource
    private DtkFetchClient dtkFetchClient;

    @Override
    @XxlJob("daTaoKeConsumerXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("daTaoKeConsumerXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            dtkFetchClient.fetch();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        stopWatch.stop();

        XxlJobHelper.log("roomTypeStatusSecondPartXxlJob xxl job end 耗时：" + stopWatch.toString());
    }
}
