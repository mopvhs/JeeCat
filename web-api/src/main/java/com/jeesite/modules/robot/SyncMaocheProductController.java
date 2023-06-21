package com.jeesite.modules.robot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheProductDao;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.helper.CatEsHelper;
import com.jeesite.modules.cat.helper.MaocheProductHelper;
import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
import com.jeesite.modules.cat.model.CatRobotMessageCondition;
import com.jeesite.modules.cat.model.MaocheProductIndex;
import com.jeesite.modules.cat.model.MaocheProductSyncRequest;
import com.jeesite.modules.cat.model.UnionProductSyncRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class SyncMaocheProductController {

    @Resource
    private MaocheProductDao maocheProductDao;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

//    public Result<String> syncMaochePorduct(String ids) {
//
//        if (StringUtils.isBlank(ids)) {
//            return Result.ERROR(404, "资源不存在");
//        }
//
//        String[] split = ids.split(",");
//
//        MaocheProductDO query = new MaocheProductDO();
//        query.setId_in(split);
//        List<MaocheProductDO> productDOs = maocheProductDao.findList(query);
//
//        if (CollectionUtils.isEmpty(productDOs)) {
//            return Result.ERROR(404, "资源不存在");
//        }
//        for (MaocheProductDO productDO : productDOs) {
//            MaocheProductIndex catIndex = MaocheProductHelper.buildIndex(productDO);
//            if (catIndex == null) {
//                return Result.OK("数据异常");
//            }
//            Map<String, Object> data = JsonUtils.toReferenceType(JsonUtils.toJSONString(catIndex), new TypeReference<Map<String, Object>>() {
//            });
//            // 特殊字段(避免为int，写不进索引)
//            data.put("processed", catIndex.getProcessed());
//            elasticSearch7Service.index(data, ElasticSearchIndexEnum.CAT_ROBOT_CRAWLER_MESSAGE_INDEX, String.valueOf(catIndex.getId()));
//        }
//
//        return Result.OK("同步完成");
//    }

    @RequestMapping(value = "maoche/product/batch/sync", method = RequestMethod.POST)
    public Result<String> batchSyncProduct(@RequestBody MaocheProductSyncRequest request) {

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
                    MaocheProductDO query = new MaocheProductDO();
                    query.setIid_in(p.toArray(new Long[0]));
                    List<MaocheProductDO> messages = maocheProductDao.findList(query);

                    if (CollectionUtils.isEmpty(messages)) {
                        continue;
                    }
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (MaocheProductDO productDO : messages) {

                        MaocheProductIndex catIndex = MaocheProductHelper.buildIndex(productDO);
                        if (catIndex == null) {
                            continue;
                        }
                        Map<String, Object> data = JsonUtils.toReferenceType(JsonUtils.toJSONString(catIndex), new TypeReference<Map<String, Object>>() {
                        });
                        // 特殊字段(避免为int，写不进索引)
                        data.put("processed", catIndex.getProcessed());
                    }

                    elasticSearch7Service.index(list, ElasticSearchIndexEnum.MAOCHE_PRODUCT_INDEX, "id", corePoolSize);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        return Result.OK("同步完成");
    }

    @RequestMapping(value = "maoche/product/sync", method = RequestMethod.POST)
    public Result<String> syncMaochePorduct(Long id) {

        if (id == null || id <= 0) {
            return Result.ERROR(500, "参数错误");
        }
        MaocheProductDO query = new MaocheProductDO();
        query.setId(String.valueOf(id));
        MaocheProductDO message = maocheProductDao.getByEntity(query);

        if (message == null) {
            return Result.ERROR(404, "资源不存在");
        }

        MaocheProductIndex catIndex = MaocheProductHelper.buildIndex(message);
        if (catIndex == null) {
            return Result.OK("数据异常");
        }
        Map<String, Object> data = JsonUtils.toReferenceType(JsonUtils.toJSONString(catIndex), new TypeReference<Map<String, Object>>() {
        });
        // 特殊字段(避免为int，写不进索引)
        data.put("processed", catIndex.getProcessed());
        elasticSearch7Service.index(data, ElasticSearchIndexEnum.CAT_ROBOT_CRAWLER_MESSAGE_INDEX, String.valueOf(catIndex.getId()));

        return Result.OK("同步完成");
    }

    @RequestMapping(value = "maoche/product/all/sync", method = RequestMethod.POST)
    public Result<String> syncAllMaocheProduct(String token) {

        if (StringUtils.isBlank(token)) {
            return Result.ERROR(500, "参数错误");
        }
        if (!token.equals("catcar")) {
            return Result.ERROR(500, "参数错误");
        }

        long id = 0L;
        int limit = 1000;
        while (true) {
            List<MaocheProductDO> list = maocheProductDao.findAll(id, limit);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            for (MaocheProductDO item : list) {
                try {
                    MaocheProductIndex catIndex = MaocheProductHelper.buildIndex(item);
                    if (catIndex == null) {
                        continue;
                    }
                    Map<String, Object> data = JsonUtils.toReferenceType(JSON.toJSONString(catIndex), new TypeReference<Map<String, Object>>() {
                    });
                    if (data == null) {
                        continue;
                    }
                    // 特殊字段(避免为int，写不进索引)
                    data.put("processed", catIndex.getProcessed());

                    elasticSearch7Service.index(data, ElasticSearchIndexEnum.MAOCHE_PRODUCT_INDEX, String.valueOf(catIndex.getId()));
                } catch (Exception e) {
                    log.error("index error item:{} ", JSON.toJSONString(item), e);
                }
            }

            id = list.get(list.size() - 1).getIid();
            if (list.size() < limit) {
                break;
            }
        }

        return Result.OK("完成");
    }
}
