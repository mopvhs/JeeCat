package com.jeesite.modules.cat.service.es;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OceanEsService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductService;

    public void indexEs(List<Long> msgIds, int corePoolSize) {
        if (CollectionUtils.isEmpty(msgIds)) {
            return;
        }
        msgIds = msgIds.stream().distinct().collect(Collectors.toList());

        MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
        query.setUiid_in(msgIds);
        query.setStatus("NORMAL");
        // 查询消息
        List<MaocheRobotCrawlerMessageSyncDO> crawlerMessages = maocheRobotCrawlerMessageSyncService.findList(query);
        if (CollectionUtils.isEmpty(crawlerMessages)) {
            return;
        }

        MaocheRobotCrawlerMessageProductDO productQuery = new MaocheRobotCrawlerMessageProductDO();
        productQuery.setMsgId_in(msgIds);
        productQuery.setStatus("NORMAL");
        // 查询消息
        List<MaocheRobotCrawlerMessageProductDO> productList = maocheRobotCrawlerMessageProductService.findList(productQuery);

        List<Map<String, Object>> messageSyncIndex = getMessageSyncIndex(crawlerMessages);
        List<Map<String, Object>> productIndex = getMessageProductIndex(productList);

        elasticSearch7Service.index(messageSyncIndex, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", corePoolSize);
        elasticSearch7Service.index(productIndex, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_PRODUCT_INDEX, "id", corePoolSize);

    }

    private List<Map<String, Object>> getMessageSyncIndex(List<MaocheRobotCrawlerMessageSyncDO> crawlerMessages) {
        if (CollectionUtils.isEmpty(crawlerMessages)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (MaocheRobotCrawlerMessageSyncDO item : crawlerMessages) {
            MaocheMessageSyncIndex index = MaocheMessageSyncIndex.toIndex(item);
            if (index == null) {
                continue;
            }
            Map<String, Object> map = JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
            });

            list.add(map);
        }

        return list;
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
