package com.jeesite.modules.cat.service.es;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanContentHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OceanEsService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductService;

    @Resource
    private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;

    public boolean indexEs(List<Long> msgIds, int corePoolSize) {
        if (CollectionUtils.isEmpty(msgIds)) {
            return false;
        }
        msgIds = msgIds.stream().distinct().collect(Collectors.toList());

//        MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
//        query.setUiid_in(msgIds);
//        query.setStatus("NORMAL");
        // 查询消息
        List<MaocheRobotCrawlerMessageSyncDO> crawlerMessages = maocheRobotCrawlerMessageSyncService.listByIds(msgIds);
        if (CollectionUtils.isEmpty(crawlerMessages)) {
            return false;
        }

        // 获取机器人消息
        List<Long> robotMsgIds = crawlerMessages.stream().map(MaocheRobotCrawlerMessageSyncDO::getRobotMsgId).toList();

        List<MaocheRobotCrawlerMessageDO> robotMsgs = maocheRobotCrawlerMessageDao.listByIds(robotMsgIds);

//        MaocheRobotCrawlerMessageProductDO productQuery = new MaocheRobotCrawlerMessageProductDO();
//        productQuery.setMsgId_in(msgIds);
//        productQuery.setStatus("NORMAL");

//        Map<Long, MaocheRobotCrawlerMessageDO> robotMap = robotMsgs.stream().collect(Collectors.toMap(MaocheRobotCrawlerMessageDO::getIid, Function.identity(), (o1, o2) -> o1));

        // 查询消息
        List<MaocheRobotCrawlerMessageProductDO> productList = maocheRobotCrawlerMessageProductService.listByMsgIds(msgIds);

        List<Map<String, Object>> messageSyncIndex = getMessageSyncIndex(crawlerMessages, productList, robotMsgs);
        List<Map<String, Object>> productIndex = getMessageProductIndex(productList);

        elasticSearch7Service.index(messageSyncIndex, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", corePoolSize);
        elasticSearch7Service.index(productIndex, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_PRODUCT_INDEX, "id", corePoolSize);

        return true;
    }

    private List<Map<String, Object>> getMessageSyncIndex(List<MaocheRobotCrawlerMessageSyncDO> crawlerMessages,
                                                          List<MaocheRobotCrawlerMessageProductDO> productList,
                                                          List<MaocheRobotCrawlerMessageDO> robotMessages) {
        if (CollectionUtils.isEmpty(crawlerMessages)) {
            return new ArrayList<>();
        }
        productList = Optional.ofNullable(productList).orElse(new ArrayList<>());
        // 获取类目
        List<String> categoryNames = productList.stream().map(MaocheRobotCrawlerMessageProductDO::getCategory).distinct().toList();
//        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> messageSyncIndex = OceanContentHelper.getMessageSyncIndex(crawlerMessages, robotMessages);

        for (Map<String, Object> sync : messageSyncIndex) {
            sync.put("categoryNames", categoryNames);
        }

//        for (MaocheRobotCrawlerMessageSyncDO item : crawlerMessages) {
//            MaocheMessageSyncIndex index = MaocheMessageSyncIndex.toIndex(item);
//
//            if (index == null) {
//                continue;
//            }
//            index.setCategoryNames(categoryNames);
//            Map<String, Object> map = JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
//            });
//
//            list.add(map);
//        }

        return messageSyncIndex;
    }

    public void updateRobotState(List<Long> msgIds, String status, Long relationId, Map<Long, MaocheRobotCrawlerMessageSyncDO> syncMap) {
        if (CollectionUtils.isEmpty(msgIds) || MapUtils.isEmpty(syncMap)) {
            return;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Long msgId : msgIds) {
            MaocheRobotCrawlerMessageSyncDO syncDO = syncMap.get(msgId);
            if (syncDO == null) {
                continue;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id", syncDO.getUiid());
            if (relationId != null && relationId > 0) {
                data.put("relationId", relationId);
            }
            if (StringUtils.isNotBlank(status)) {
                data.put("oceanStatus", status);
            }

            list.add(data);
        }

        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        elasticSearch7Service.update(list, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);
    }

    private List<Map<String, Object>> getMessageProductIndex(List<MaocheRobotCrawlerMessageProductDO> items) {
        if (CollectionUtils.isEmpty(items)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (MaocheRobotCrawlerMessageProductDO item : items) {
            MaocheMessageProductIndex index = MaocheMessageProductIndex.toIndex(item);
            if (index == null) {
                continue;
            }
            Map<String, Object> map = JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
            });

            list.add(map);
        }

        return list;
    }
}
