package com.jeesite.modules.cat.service.stage.cg.ocean;

import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageProductCondition;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.es.OceanEsService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.stage.cg.ocean.exception.QueryThirdApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import com.google.common.collect.Lists;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstraOceanStage implements OceanStage {

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private OceanEsService oceanEsService;

    @Resource
    private OceanSearchService oceanSearchService;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Override
    public void process(OceanContext context) {

        // 1. 构建基础的消息结构
        buildBaseMessageSync(context);

        try {
            // 2. 查询第三方接口获取商品数据
            queryProductFromThirdApi(context);

            // 3. 保存商品数据到消息中
            buildBaseMessageProducts(context);

            // 4. 保存商品数据
            saveMessageAndProduct(context);

            // 相似文案判断
            similarMsgCheck(context);

            // 6. 构建索引
            indexEx(context);

            // 相似消息更新索引
            similarMsgUpdate(context);

        } catch (QueryThirdApiException e) {
            String action = e.getAction();
            // 查询失败后，是否需要保存消息
            if (QueryThirdApiException.QUERY_FAIL.equals(action)) {
                saveFailQueryProduct(context);
                return;
            }
            log.error("查询第三方接口获取商品数据失败 message :{}", JsonUtils.toJSONString(context.getCrawlerMessage()), e);
        } catch (Exception e) {
            log.error("公海流程处理异常 message :{}", JsonUtils.toJSONString(context.getCrawlerMessage()), e);
        }
    }

    @Override
    public void buildBaseMessageSync(OceanContext context) {

        MaocheRobotCrawlerMessageDO message = context.getCrawlerMessage();

        // 1. 构建基础的消息结构

        MaocheRobotCrawlerMessageSyncDO sync = new MaocheRobotCrawlerMessageSyncDO();

        sync.setMsg(message.getMsg());
        sync.setProcessed(0L);
        sync.setResourceIds("");
        sync.setUniqueHash(message.getUniqueHash());
        sync.setWxTime(message.getTime());
        sync.setAffType(message.getAffType());
        sync.setRobotMsgId(message.getIid());
        sync.setCreateBy("admin");
        sync.setCreateDate(message.getCreateTime());
        sync.setUpdateBy("admin");
        sync.setUpdateDate(new Date());
        sync.setRemarks("");
        sync.setStatus("INIT");

        MatchContent matchContent = calMatchContent(getPattern(), message.getMsg());
        sync.setUniqueHash(matchContent.getCalMd5());

        // 写入到context中
        context.setMessageSync(sync);
        customBuildMessage(context);
    }

    @Override
    public void customBuildMessage(OceanContext context) {
        return;
    }

    public void similarMsgUpdate(OceanContext context) {
        List<MaocheMessageSyncIndex> similarMessages = context.getSimilarMessages();
        if (CollectionUtils.isEmpty(similarMessages) || !context.isIndexResult()) {
            return;
        }
        // 更新msg的状态，并且记录因为谁导致的相似
        List<Long> ids = similarMessages.stream().map(MaocheMessageSyncIndex::getId).toList();
        // 获取相似的消息
        MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
        query.setUiid_in(ids);query.setStatus("NORMAL");
        List<MaocheRobotCrawlerMessageSyncDO> similarMsgs = maocheRobotCrawlerMessageSyncService.findList(query);

        // 更新相似消息的状态
        // 10条一次
        List<List<MaocheRobotCrawlerMessageSyncDO>> partition = Lists.partition(similarMsgs, 10);

        Long similarIid = context.getCrawlerMessage().getIid();
        List<Map<String, Object>> data = new ArrayList<>();
        for (List<MaocheRobotCrawlerMessageSyncDO> p : partition) {
            Map<String, Object> messageSyncIndex = new HashMap<>();

            try {
                for (MaocheRobotCrawlerMessageSyncDO item : p) {
                    item.addRemarks("similar", similarIid);
                    item.setStatus("SIMILAR");

                    messageSyncIndex.put("id", item.getUiid());
                    messageSyncIndex.put("status", "SIMILAR");
                    data.add(messageSyncIndex);
                }

                // 批量更新
                maocheRobotCrawlerMessageSyncService.updateBatch(p);
            } catch (Exception e) {
                log.error("更新相似消息状态失败", e);
                try {
                    maocheRobotCrawlerMessageSyncService.updateBatch(p);
                } catch (Exception ee) {
                    log.error("第二次更新相似消息状态失败", ee);
                }
            }
        }

        elasticSearch7Service.update(data, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);

    }

    public void saveFailQueryProduct(OceanContext context) {
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        if (messageSync == null) {
            log.error("保存失败的查询商品数据失败, messageSync is null");
            return;
        }
        messageSync.setStatus("FAIL");
        messageSync.setRemarks(context.getFailRemarks());
        maocheRobotCrawlerMessageSyncService.save(messageSync);
    }

    @Override
    public void indexEx(OceanContext context) {
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        if (messageSync == null || messageSync.getUiid() == null || messageSync.getUiid() == 0L) {
            log.error("索引构建失败, messageSync is null");
            return;
        }

        oceanEsService.indexEs(Collections.singletonList(messageSync.getUiid()), 10);

        // 默认执行的话就认为是成功
        context.setIndexResult(true);
    }


    @Override
    public void similarMsgCheck(OceanContext context) {
//        if (context.isOnlySpecialUri()) {
//            return;
//        }
        // 获取文案的md5
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        String uniqueHash = messageSync.getUniqueHash();

        // 判断3天前内是否存在
        OceanMessageCondition condition = new OceanMessageCondition();
        condition.setUniqueHash(uniqueHash);
        condition.setAffType(getAffType());
        // 查询存在一样的uniqueHash的数据，通过es查询
        ElasticSearchData<MaocheMessageSyncIndex, CatProductBucketTO> searchData = oceanSearchService.searchMsg(condition, null, null, null, 0, 1000);
        // 异常的时候，再查询一次
        if (searchData == null) {
            searchData = oceanSearchService.searchMsg(condition, null, null, null, 0, 1000);
        }
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return;
        }
        List<MaocheMessageSyncIndex> documents = searchData.getDocuments();

        List<String> resourceIds = Arrays.asList(StringUtils.split(messageSync.getResourceIds(), ","));

        List<MaocheMessageSyncIndex> similarMessages = new ArrayList<>();
        // 需要二次对比的数据
        List<MaocheMessageSyncIndex> secondChecks = new ArrayList<>();


        // 先对比资源id是否一样，数量，以及集合的差集是否为0
        for (MaocheMessageSyncIndex doc : documents) {
            List<String> tempIds = new ArrayList<>(resourceIds);
            List<String> itemResourceIds = Optional.ofNullable(doc.getResourceIds()).orElse(new ArrayList<>());

            if (tempIds.size() != itemResourceIds.size()) {
                continue;
            }
            // 差集为0，说明资源id一样
            tempIds.removeAll(itemResourceIds);

            if (CollectionUtils.isEmpty(tempIds)) {
                // 说明资源id一样，直接返回
                similarMessages.add(doc);
                continue;
            }
            secondChecks.add(doc);
        }

        if (CollectionUtils.isNotEmpty(secondChecks)) {
            // 通过secondChecks获取msgId的map
            Map<Long, MaocheMessageSyncIndex> messageSyncIndexMap = secondChecks.stream().collect(Collectors.toMap(MaocheMessageSyncIndex::getId, Function.identity(), (k1, k2) -> k1));

            List<Long> msgIds = secondChecks.stream().map(MaocheMessageSyncIndex::getId).distinct().toList();
            OceanMessageProductCondition productSearch = new OceanMessageProductCondition();
            productSearch.setMsgIds(msgIds);

            ElasticSearchData<MaocheMessageProductIndex, CatProductBucketTO> productSearchData = oceanSearchService.searchProduct(productSearch, null, null, 0, 1000);
            // 获取失败的话，再获取一次
            if (productSearchData == null) {
                productSearchData = oceanSearchService.searchProduct(productSearch, null, null, 0, 1000);
            }
            if (productSearchData == null || CollectionUtils.isEmpty(productSearchData.getDocuments())) {
                // 多次失败的话，就认为是一样的
                similarMessages.addAll(secondChecks);
                return;
            }

            List<String> sellerIds = context.getMessageProducts().stream().map(MaocheRobotCrawlerMessageProductDO::getSellerId).distinct().toList();

            List<MaocheMessageProductIndex> productIndices = productSearchData.getDocuments();
            // 按照msgId分组
            Map<Long, List<MaocheMessageProductIndex>> msgIdMap = productIndices.stream().collect(Collectors.groupingBy(MaocheMessageProductIndex::getMsgId));
            // 对比seller是否一样
            for (Map.Entry<Long, List<MaocheMessageProductIndex>> entry : msgIdMap.entrySet()) {
                MaocheMessageSyncIndex messageSyncIndex = messageSyncIndexMap.get(entry.getKey());
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    similarMessages.add(messageSyncIndex);
                    continue;
                }
                List<String> productSellerIds = entry.getValue().stream().map(MaocheMessageProductIndex::getSellerId).distinct().toList();
                List<String> temp = new ArrayList<>(sellerIds);
                temp.removeAll(productSellerIds);
                if (CollectionUtils.isEmpty(temp)) {
                    similarMessages.add(messageSyncIndex);
                }
            }
        }

        context.setSimilarMessages(similarMessages);
    }

    public static MatchContent calMatchContent(Pattern pattern, String content) {
        StringBuilder calContent = new StringBuilder();
        String[] split = content.split("\n");
        String regex = "[\\w\\d*.()/]+";

        List<Matcher> matchers = new ArrayList<>();

        List<String> contents = new ArrayList<>();
        for (String item : split) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                matchers.add(matcher);
                // 去除所有的数字和符号还有英文
                calContent.append(item.replaceAll(regex, ""));
            } else {
                contents.add(item);
                calContent.append(item);
            }
        }

        MatchContent matchContent = new MatchContent();

        matchContent.setCalContent(calContent.toString());
        matchContent.setContents(contents);
        matchContent.setMatchers(matchers);
        matchContent.setCalMd5(Md5Utils.md5(calContent.toString()));
        return matchContent;
    }

    @Data
    public static class MatchContent {

        private List<Matcher> matchers;

        private String calContent;

        private List<String> contents;

        private String calMd5;
    }
}
