package com.jeesite.modules.robot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.helper.CatEsHelper;
import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
import com.jeesite.modules.cat.model.condition.CatRobotMessageCondition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class CatSyncRobotMessageController {

    @Resource
    private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @RequestMapping(value = "cat/robot/message/sync", method = RequestMethod.POST)
    public Result<String> syncMessage(@RequestBody CatRobotMessageCondition request) {

        if (request == null || request.getId() == null || request.getId() <= 0) {
            return Result.ERROR(500, "参数错误");
        }
        MaocheRobotCrawlerMessageDO query = new MaocheRobotCrawlerMessageDO();
        query.setId(String.valueOf(request.getId()));
        MaocheRobotCrawlerMessageDO message = maocheRobotCrawlerMessageDao.getByEntity(query);

        if (message == null) {
            return Result.ERROR(404, "资源不存在");
        }

        CarRobotCrawlerMessageIndex catIndex = CatEsHelper.buildCatIndex(message);
        Map<String, Object> data = JSONObject.parseObject(JSON.toJSONString(catIndex), Map.class);
        // 特殊字段(避免为int，写不进索引)
        data.put("processed", Optional.ofNullable(catIndex.getProcessed()).orElse(0L));
        elasticSearch7Service.index(data, ElasticSearchIndexEnum.CAT_ROBOT_CRAWLER_MESSAGE_INDEX, String.valueOf(catIndex.getId()));

        return Result.OK("同步完成");
    }

    @RequestMapping(value = "cat/robot/message/all/sync", method = RequestMethod.POST)
    public Result<String> syncAllMessage(String token) {

        if (StringUtils.isBlank(token)) {
            return Result.ERROR(500, "参数错误");
        }
        if (!token.equals("catcar")) {
            return Result.ERROR(500, "参数错误");
        }

        long id = 0L;
        int limit = 1000;
        while (true) {
            List<MaocheRobotCrawlerMessageDO> list = maocheRobotCrawlerMessageDao.findAll(id, limit);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            for (MaocheRobotCrawlerMessageDO item : list) {
                try {
                    CarRobotCrawlerMessageIndex catIndex = CatEsHelper.buildCatIndex(item);
                    Map<String, Object> data = JsonUtils.toReferenceType(JSON.toJSONString(catIndex), new TypeReference<Map<String, Object>>() {
                    });
                    if (data == null) {
                        continue;
                    }
                    // 特殊字段(避免为int，写不进索引)
                    data.put("processed", Optional.ofNullable(catIndex.getProcessed()).orElse(0L));

                    elasticSearch7Service.index(data, ElasticSearchIndexEnum.CAT_ROBOT_CRAWLER_MESSAGE_INDEX, String.valueOf(catIndex.getId()));
                } catch (Exception e) {
                    log.error("index error item:{} ", JSON.toJSONString(item), e);
                }
            }

            id = list.get(list.size() - 1).getIid();
            if (list.size() < limit) {
                break;
            }
        }
//        for (MaocheRobotCrawlerMessageDO item : all) {
//            try {
//                CarRobotCrawlerMessageIndex catIndex = CatEsHelper.buildCatIndex(item);
//                Map<String, Object> data = JSONObject.parseObject(JSON.toJSONString(catIndex), Map.class);
//                elasticSearch7Service.index(data, ElasticSearchIndexEnum.CAT_ROBOT_CRAWLER_MESSAGE_INDEX, String.valueOf(catIndex.getId()));
//            } catch (Exception e) {
//                log.error("index error item:{} ", JSON.toJSONString(item), e);
//            }
//        }
        return Result.OK("完成");
    }
}
