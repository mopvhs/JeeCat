package com.jeesite.modules.product;

import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.model.condition.CatRobotMessageCondition;
import com.jeesite.modules.cat.model.UnionProductSyncRequest;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.message.DingDingService;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class CatSyncAlimamaUnionProductController {

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;
    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private DingDingService dingDingService;

    private static ExecutorService executor = null;

    @PostConstruct
    public void init() {
        executor = new ThreadPoolExecutor(5, 20,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("es-executor-controller"));
    }

    @RequestMapping(value = "cat/alimama/union/product/sync", method = RequestMethod.POST)
    public Result<String> syncProduct(@RequestBody CatRobotMessageCondition request) {

        if (request == null || request.getId() == null || request.getId() <= 0) {
            return Result.ERROR(500, "参数错误");
        }
        MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
        query.setId(String.valueOf(request.getId()));
//        MaocheAlimamaUnionProductDO message = maocheAlimamaUnionProductDao.getByEntity(query);

        MaocheAlimamaUnionProductDO unionProductDO = maocheAlimamaUnionProductService.get(query);

        if (unionProductDO == null) {
            return Result.ERROR(404, "资源不存在");
        }

        cgUnionProductService.indexEs(List.of(unionProductDO), 1);

        return Result.OK("同步完成");
    }

    @RequestMapping(value = "cat/alimama/union/product/batch/sync", method = RequestMethod.POST)
    public Result<String> batchSyncProduct(@RequestBody UnionProductSyncRequest request) {

        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            return Result.ERROR(500, "参数错误");
        }

        List<Long> ids = request.getIds().stream().distinct().toList();

        int corePoolSize = 20;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                List<List<Long>> partition = Lists.partition(ids, corePoolSize);
                for (List<Long> p : partition) {
                    MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
                    query.setIid_in(p.toArray(new Long[0]));
                    List<MaocheAlimamaUnionProductDO> messages = maocheAlimamaUnionProductDao.findList(query);

                    if (CollectionUtils.isEmpty(messages)) {
                        continue;
                    }

                    cgUnionProductService.indexEs(messages, corePoolSize);
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        return Result.OK("同步完成");
    }

    @RequestMapping(value = "/cat/alimama/union/product/batch/del", method = RequestMethod.POST)
    public Result<String> batchDelProduct(@RequestBody UnionProductSyncRequest request) {

        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            return Result.ERROR(500, "参数错误");
        }

        List<Long> ids = request.getIds().stream().distinct().toList();

        int corePoolSize = 10;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                List<List<Long>> partition = Lists.partition(ids, corePoolSize);
                for (List<Long> p : partition) {
                    MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
                    query.setIid_in(p.toArray(new Long[0]));
                    List<MaocheAlimamaUnionProductDO> products = maocheAlimamaUnionProductDao.findList(query);

                    if (CollectionUtils.isEmpty(products)) {
                        continue;
                    }
                    maocheAlimamaUnionProductService.updateProductStatus(p, "DELETE");

                    List<Long> ids = products.stream().map(MaocheAlimamaUnionProductDO::getUiid).distinct().collect(Collectors.toList());
                    cgUnionProductService.delIndex(ids);
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        return Result.OK("同步完成");
    }

    @RequestMapping(value = "cat/alimama/union/product/all/sync", method = RequestMethod.POST)
    public Result<String> syncAllProduct(String token, @RequestParam(value = "fast", required = false, defaultValue = "no") String fast) {

        if (StringUtils.isBlank(token)) {
            return Result.ERROR(500, "参数错误");
        }
        if (!token.equals("catcar")) {
            return Result.ERROR(500, "参数错误");
        }
        dingDingService.sendDingDingMsg("全量同步执行开始");


        long startTime = System.currentTimeMillis();
        int step = 5000;

        Inc inc = new Inc();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long id = 0L;
                int limit = 20;
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                while (true) {
                    String status = null;
                    if ("fast".equals(fast)) {
                        status = "NORMAL";
                    }

                    List<MaocheAlimamaUnionProductDO> list = maocheAlimamaUnionProductDao.findAll(id, status, limit);
                    if (CollectionUtils.isEmpty(list)) {
                        break;
                    }
                    inc.incNum(list.size());
                    cgUnionProductService.indexEs(list, 1);

                    // 每20000条打一次日志
                    if (inc.getNum() % step == 0) {
                        long t = System.currentTimeMillis() - startTime;
                        dingDingService.sendParseDingDingMsg("全量同步执行当前数量为:{}, 已耗时：{} 毫秒", inc.getNum(), t);
                    }

                    id = list.get(list.size() - 1).getUiid();
                    if (list.size() < limit) {
                        break;
                    }
                }
                stopWatch.stop();
                String msg = "全量猫车商品索引执行完成，总时间:{}";
                dingDingService.sendParseDingDingMsg(msg, stopWatch.toString());
            }
        };

        executor.submit(runnable);

        return Result.OK("完成");
    }

    @Data
    private class Inc {

        public int num;

        public void incNum(int count) {
            this.num += count;
        }
    }
}
