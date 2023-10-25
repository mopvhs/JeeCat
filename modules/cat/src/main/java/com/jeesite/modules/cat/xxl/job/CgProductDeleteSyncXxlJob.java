package com.jeesite.modules.cat.xxl.job;

import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheDataokeProductDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheSenderTaskService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.DaTaoKeApiService;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 全量构建商品索引
 */
@Slf4j
@Component
public class CgProductDeleteSyncXxlJob extends IJobHandler {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheSenderTaskService maocheSenderTaskService;

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private MaocheDataokeProductDao maocheDataokeProductDao;

    @Resource
    private DaTaoKeApiService daTaoKeApiService;

    @Resource
    private CacheService cacheService;

    @Resource
    private DingDingService dingDingService;

    @Override
    @XxlJob("cgProductDeleteSyncXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("cgProductDeleteSyncXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String status = "DELETE";
        int total = 0;

        long id = 0L;
        int limit = 20;
        while (true) {
            try {
                long start = System.currentTimeMillis();
                List<MaocheAlimamaUnionProductDO> list = maocheAlimamaUnionProductDao.findAll(id, status, limit);
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }
                total += list.size();
                id = list.get(list.size() - 1).getUiid();

                List<Long> ids = list.stream().map(MaocheAlimamaUnionProductDO::getUiid).toList();
                list = maocheAlimamaUnionProductDao.listSimpleByIds(ids);

                cgUnionProductService.indexEs(list, limit);
                long end = System.currentTimeMillis();
                long seconds = TimeUnit.MILLISECONDS.toSeconds(end - start);
                log.info("del sync xxl job current total :{}, single time:{}", total, seconds);

                if (list.size() < limit) {
                    break;
                }
            } catch (Exception e) {
                dingDingService.sendParseDingDingMsg("删除商品的全量同步异常 起始id:{}, 异常{}", null, id, e.getMessage());
                log.error(e.getMessage(), e);
            }
        }
        stopWatch.stop();

        dingDingService.sendParseDingDingMsg("xxl job 删除商品的全量同步索引数据完成，total:{}, 耗时：{}", null, total, stopWatch.toString());

        XxlJobHelper.log("cgProductDeleteSyncXxlJob xxl job end 耗时：" + stopWatch.toString());
    }
}
