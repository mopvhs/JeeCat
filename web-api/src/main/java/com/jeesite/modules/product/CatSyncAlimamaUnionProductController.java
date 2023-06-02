package com.jeesite.modules.product;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.helper.CatEsHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
import com.jeesite.modules.cat.model.CatRobotMessageCondition;
import com.jeesite.modules.cat.model.UnionProductSyncRequest;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.tasks.Task;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        MaocheAlimamaUnionProductDO message = maocheAlimamaUnionProductDao.getByEntity(query);

        if (message == null) {
            return Result.ERROR(404, "资源不存在");
        }

        cgUnionProductService.indexEs(List.of(message), 1);

        return Result.OK("同步完成");
    }

    @RequestMapping(value = "cat/alimama/union/product/batch/sync", method = RequestMethod.POST)
    public Result<String> batchSyncProduct(@RequestBody UnionProductSyncRequest request) {

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
                    query.setId_in(p.toArray(new Long[0]));
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
                    query.setId_in(p.toArray(new Long[0]));
                    List<MaocheAlimamaUnionProductDO> products = maocheAlimamaUnionProductDao.findList(query);

                    if (CollectionUtils.isEmpty(products)) {
                        continue;
                    }
                    maocheAlimamaUnionProductService.updateProductStatus(p, "DELETE");

                    List<Long> ids = products.stream().map(MaocheAlimamaUnionProductDO::getIid).distinct().collect(Collectors.toList());
                    cgUnionProductService.delIndex(ids);
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        return Result.OK("同步完成");
    }

    @RequestMapping(value = "cat/alimama/union/product/all/sync", method = RequestMethod.POST)
    public Result<String> syncAllProduct(String token) {

        if (StringUtils.isBlank(token)) {
            return Result.ERROR(500, "参数错误");
        }
        if (!token.equals("catcar")) {
            return Result.ERROR(500, "参数错误");
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long id = 0L;
                int limit = 20;
                while (true) {
                    List<MaocheAlimamaUnionProductDO> list = maocheAlimamaUnionProductDao.findAll(id, limit);
                    if (CollectionUtils.isEmpty(list)) {
                        break;
                    }
                    cgUnionProductService.indexEs(list, 1);

                    id = list.get(list.size() - 1).getIid();
                    if (list.size() < limit) {
                        break;
                    }
                }
            }
        };

        executor.submit(runnable);

        return Result.OK("完成");
    }
}
