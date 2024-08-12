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
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.mchange.lang.ByteUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import com.google.common.collect.Lists;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstraOceanStage implements OceanStage {

    public static List<String> deletions = new ArrayList<>();
    public static List<TextBO> deletionTexts = new ArrayList<>();
    static {
        deletions.add("豪车");
        deletions.add("❗");
        deletions.add("速度❗❗✋慢无~");
        deletions.add("✋慢无~");
        deletions.add("✋慢无");
        deletions.add("漏栋价");
        deletions.add("漏栋");
        deletions.add("随时无~");
        deletions.add("超超豪车‼手慢无无无");
        deletions.add("☞复制去桃宝弹出：");
        deletions.add("☞复制去桃宝弹出:");
        deletions.add("🦆");
        deletions.add("\uD83E\uDD86"); // 🦆
        deletions.add("🐱");
        deletions.add("\uD83D\uDC31"); // 🐱
        deletions.add("🐔");
        deletions.add("\uD83D\uDC14");
        deletions.add("⚠");
        deletions.add("🐦");
        deletions.add("\uD83D\uDC26");
        deletions.add("👇");
        deletions.add("\uD83D\uDC47");
        deletions.add("✅");
        deletions.add("👉复制去🍑宝");
        deletions.add("\uD83D\uDC49复制去\uD83C\uDF51宝");
        deletions.add("--");
        deletions.add("復zhi打开𝙏𝙖𝙤𝘽𝙖𝙤 𝘼𝙋𝙋");
        deletions.add("復zhi打开\uD835\uDE4F\uD835\uDE56\uD835\uDE64\uD835\uDE3D\uD835\uDE56\uD835\uDE64 \uD835\uDE3C\uD835\uDE4B\uD835\uDE4B");
        deletions.add("可可独家");
        deletions.add("可可首发");
        deletions.add("可可");

        deletions.add("好价");
        deletions.add("简单车");
        deletions.add("速度");
        deletions.add("速度手慢无");
        deletions.add("活动稀少");
        deletions.add("手慢无~");
        deletions.add("手慢无");
        deletions.add("\uD83D\uDC36"); // 🐶
        deletions.add("☞复制去淘宝弹出：");
        deletions.add("快冲‼");
        deletions.add("‼");
        deletions.add("！");
        deletions.add("!");
        deletions.add("进猫车群#COCO猫舍");
        deletions.add("_________________");

        for (String item : deletions) {
            TextBO textBO = new TextBO(item, item.length());
            deletionTexts.add(textBO);
        }

        // 排序，按大到小
        // 使用Collections.sort方法和自定义Comparator进行排序
        deletionTexts.sort(new Comparator<TextBO>() {
            @Override
            public int compare(TextBO o1, TextBO o2) {
                // 按照size字段从大到小排序
                return Integer.compare(o2.getSize(), o1.getSize());
            }
        });
    }

    public static List<String> deletionUrls = new ArrayList<>();
    static {
        deletionUrls.add("车:s.q5url.cn/yA7U");
        deletionUrls.add("\uD83D\uDC31车:s.q5url.cn/yA7U");
    }

    /**
     * 命中关键词，直接不进公海
     */
    public static List<String> failTexts = new ArrayList<>();
    static {
        failTexts.add("冠军标");
        failTexts.add("元佑双标");
    }

    public static Map<String, String> replacements = new LinkedHashMap<>();
    static {
        replacements.put("卷", "券");
        replacements.put("锩", "券");
        replacements.put("蕞低", "最低");
        replacements.put("加车1件", "加购一件");
        replacements.put("plus\\+首单", "Plus叠首单");
        replacements.put("plus", "Plus");
        replacements.put("亓", "元");
        replacements.put("旗见店", "旗舰店");
        replacements.put("好萍仮", "好返");
        replacements.put("到✋", "到手价");
        replacements.put("拼\\.团\\.", "拼团");
        replacements.put("帼际", "国际");
        replacements.put("桃宝", "淘宝");
        replacements.put("✖", "*");
        replacements.put("坤", "鸡");
        replacements.put("➕", "&");
        replacements.put("普素羊肉", "经典鲜羊肉");
        replacements.put("好萍", "好评");
        replacements.put("原本", "日常");
        replacements.put("不吃包tui", "不吃包退");
        replacements.put("好反", "好返");
    }

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

        try {
            // 1. 构建基础的消息结构
            buildBaseMessageSync(context);

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
        String msg = message.getMsg();
        String s = interposeMsg(msg);
        message.setMsg(s);

        // 1. 构建基础的消息结构
        MatchContent matchContent = calMatchContent(getPattern(), message.getMsg());

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
        query.setUiid_in(ids);
        query.setStatus("NORMAL");
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
            // 为空的时候 做一次db的查询，es刷磁盘需要时间，短时间内可能会查询不出来
//            MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
//            query.setUniqueHash(uniqueHash);
//            query.setStatus("NORMAL");
//            List<MaocheRobotCrawlerMessageSyncDO> similarMsgs = maocheRobotCrawlerMessageSyncService.findList(query);
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

    public String interposeMsg(String msg) {
        if (StringUtils.isBlank(msg)) {
            return msg;
        }

        // 规则
        // 先做replace
        // 遍历替换规则并进行替换
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            msg = msg.replaceAll(entry.getKey(), entry.getValue());
        }

        // 构建排序，长的文本需要先匹配做删除
        String[] split = msg.split("\n");
        StringBuilder builder = new StringBuilder();

        // 特殊空格
        byte[] bytes = new byte[]{-17, -72, -113};
        String hexAscii = ByteUtils.toHexAscii(bytes);

        for (String failText : failTexts) {
            if (msg.contains(failText)) {
                throw new IllegalArgumentException("messageSync contains fail . msg: " + msg + ", text " + failText);
            }
        }

        for (String line : split) {
            String replace = line;
            // 是否包含 url
            for (String url : deletionUrls) {
                if (replace.contains(url)) {
                    replace = null;
                    break;
                }
            }
            String replaceAscii = ByteUtils.toHexAscii(replace.getBytes(StandardCharsets.UTF_8));

            if (StringUtils.isBlank(replace) || replace.equals("\n") || replace.equals("\uFE0F\uFE0F") || hexAscii.equals(replace) || hexAscii.equals(replaceAscii)) {
                continue;
            }

            for (TextBO textBO : deletionTexts) {
                replace = replace.replaceAll(textBO.getText(), "");
            }
            replaceAscii = ByteUtils.toHexAscii(replace.getBytes(StandardCharsets.UTF_8));
            if (StringUtils.isBlank(replace) || replace.equals("\n") || replace.equals("\uFE0F\uFE0F") || hexAscii.equals(replaceAscii)) {
                continue;
            }

            builder.append(replace).append("\n");
        }

        return builder.toString();
    }

    public static String fixAffType(String msg, String affType) {
        if (StringUtils.isBlank(msg)) {
            return affType;
        }
        boolean contains = msg.contains("y.q5url.cn");

        return contains ? "tb" : affType;
    }

    @Data
    @AllArgsConstructor
    public static class TextBO {

        private String text;

        private Integer size;
    }
}
