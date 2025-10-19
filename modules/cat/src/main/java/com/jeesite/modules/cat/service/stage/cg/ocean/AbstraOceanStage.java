package com.jeesite.modules.cat.service.stage.cg.ocean;

import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageProductCondition;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.es.OceanEsService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.jeesite.modules.cat.service.stage.cg.ocean.exception.QueryThirdApiException;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandContext;
import com.mchange.lang.ByteUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
        deletions.add("https://y-03.cn/4zbilN");

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
        replacements.put("荭包", "红包");
        replacements.put("帼", "国");
        replacements.put("忦值", "价值");
        replacements.put("忦", "价");

        replacements.put("拍1:", "加购一件\n");
        replacements.put("拍2:", "加购两件\n");
        replacements.put("拍3:", "加购三件\n");
        replacements.put("拍4:", "加购四件\n");
        replacements.put("拍5:", "加购五件\n");
        replacements.put("拍6:", "加购六件\n");
        replacements.put("拍7:", "加购七件\n");
        replacements.put("拍8:", "加购八件\n");
        replacements.put("拍9:", "加购九件\n");
        replacements.put("拍10:", "加购十件\n");

        replacements.put("凑1:", "凑单一件\n");
        replacements.put("凑2:", "凑单两件\n");
        replacements.put("凑3:", "凑单三件\n");
        replacements.put("凑4:", "凑单四件\n");
        replacements.put("凑5:", "凑单五件\n");
        replacements.put("凑6:", "凑单六件\n");
        replacements.put("凑7:", "凑单七件\n");
        replacements.put("凑8:", "凑单八件\n");
        replacements.put("凑9:", "凑单九件\n");
        replacements.put("凑10:", "凑单十件\n");

        replacements.put("加1:", "加购一件\n");
        replacements.put("加2:", "加购两件\n");
        replacements.put("加3:", "加购三件\n");
        replacements.put("加4:", "加购四件\n");
        replacements.put("加5:", "加购五件\n");
        replacements.put("加6:", "加购六件\n");
        replacements.put("加7:", "加购七件\n");
        replacements.put("加8:", "加购八件\n");
        replacements.put("加9:", "加购九件\n");
        replacements.put("加10:", "加购十件\n");
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

            // 计算相似内容code
            calSimilar(context);

            // 3. 保存商品数据到消息中
            buildBaseMessageProducts(context);

            // 判断是否为相似商品
            checkSimilar(context);

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
            DingDingService.sendParseDingDingMsg("公海流程处理异常 message :{}, e:{}", JsonUtils.toJSONString(context.getCrawlerMessage()), e.getMessage());
        }
    }

    @Override
    public void buildBaseMessageSync(OceanContext context) {

        MaocheRobotCrawlerMessageDO message = context.getCrawlerMessage();
        String msg = message.getMsg();
        // 消息内容干预
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
        MaocheRobotCrawlerMessageSyncDO update = new MaocheRobotCrawlerMessageSyncDO();

        update.setId(messageSync.getId());
        // 不修改msg的内容
        update.setStatus(OceanStatusEnum.FAIL.name());
        update.setRemarks(context.getFailRemarks());
        maocheRobotCrawlerMessageSyncService.updateById(messageSync);
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
        if (context.getSimilar() != null) {
            // 新逻辑
            return;
        }

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

        msg = msg.replaceAll("\u2028", "\n");
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
            if (StringUtils.isBlank(replace)) {
                continue;
            }
            String replaceAscii = ByteUtils.toHexAscii(replace.getBytes(StandardCharsets.UTF_8));

            if (replace.equals("\n") || replace.equals("\uFE0F\uFE0F") || hexAscii.equals(replace) || hexAscii.equals(replaceAscii)) {
                continue;
            }

            for (TextBO textBO : deletionTexts) {
                replace = replace.replaceAll(textBO.getText(), "");
            }

            if (StringUtils.isBlank(replace)) {
                continue;
            }

            replaceAscii = ByteUtils.toHexAscii(replace.getBytes(StandardCharsets.UTF_8));
            if (replace.equals("\n") || replace.equals("\uFE0F\uFE0F") || hexAscii.equals(replaceAscii)) {
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
//        boolean contains = msg.contains("y.q5url.cn") || msg.contains("y-03.cn");
        return affType;
//        return contains ? "tb" : affType;
    }

    @Data
    @AllArgsConstructor
    public static class TextBO {

        private String text;

        private Integer size;
    }

    @Override
    public void buildBaseMessageProducts(OceanContext context) {

        List<MaocheRobotCrawlerMessageProductDO> messageProducts = new ArrayList<>();
        // 获取淘宝的
        List<MaocheRobotCrawlerMessageProductDO> tbProducts = buildTbProducts(context);
        // 获取京东的
        List<MaocheRobotCrawlerMessageProductDO> jdProducts = buildJdProducts(context);

        if (CollectionUtils.isNotEmpty(tbProducts)) {
            messageProducts.addAll(tbProducts);
        }

        if (CollectionUtils.isNotEmpty(jdProducts)) {
            messageProducts.addAll(jdProducts);
        }

        context.setMessageProducts(messageProducts);
    }

    public List<MaocheRobotCrawlerMessageProductDO> buildTbProducts(OceanContext context) {

        Map<String, CommandResponseV2> productMap = context.getTbProductMap();
        if (MapUtils.isEmpty(productMap)) {
            return null;
        }

        List<MaocheRobotCrawlerMessageProductDO> messageProducts = new ArrayList<>();
        for (Map.Entry<String, CommandResponseV2> entry : productMap.entrySet()) {
            CommandResponseV2 tbProduct = entry.getValue();
            CommandResponseV2.ItemBasicInfo itemBasicInfo = tbProduct.getItemBasicInfo();
            CommandResponseV2.PricePromotionInfo pricePromotionInfo = tbProduct.getPricePromotionInfo();

            // XgBGorXFGtXxwmvX5BT0oYcAUg-yz3oeZi6a2bapxdcyb
            String[] idArr = StringUtils.split(tbProduct.getNumIid(), "-");
            String itemIdSuffix = idArr[1];

            MaocheRobotCrawlerMessageProductDO productDO = new MaocheRobotCrawlerMessageProductDO();
            productDO.setResourceId(itemIdSuffix);
            productDO.setInnerId("0");
            productDO.setItemId(tbProduct.getNumIid());
            productDO.setApiContent(JsonUtils.toJSONString(tbProduct));
            productDO.setCategory(itemBasicInfo.getCategoryName());
            productDO.setTitle(itemBasicInfo.getTitle());
            productDO.setShortTitle(itemBasicInfo.getShortTitle());
            // detail = 2之后，字段被移除了
            productDO.setShopDsr("0");
            productDO.setShopName(itemBasicInfo.getShopTitle());
            productDO.setSellerId(itemBasicInfo.getSellerId());
            productDO.setPictUrl(itemBasicInfo.getPictUrl());
            productDO.setCommissionRate(new BigDecimal(tbProduct.getCommissionRate()).multiply(new BigDecimal(100)).longValue());
            productDO.setPrice(new BigDecimal(pricePromotionInfo.getZkFinalPrice()).multiply(new BigDecimal(100)).longValue());
            productDO.setVolume(NumberUtils.toLong(itemBasicInfo.getVolume()));
            productDO.setStatus("NORMAL");
            productDO.setCreateBy("admin");
            productDO.setUpdateBy("admin");
            productDO.setRemarks("{}");

            messageProducts.add(productDO);
        }

        return messageProducts;
    }

    public List<MaocheRobotCrawlerMessageProductDO> buildJdProducts(OceanContext context) {
        List<JdUnionIdPromotion> promotions = context.getJdProducts();
        if (context.isOnlySpecialUri()) {
            return buildSpecialUriProducts(context);
        }

        if (CollectionUtils.isEmpty(promotions)) {
            return null;
        }

        List<MaocheRobotCrawlerMessageProductDO> productDOs = new ArrayList<>();
        for (JdUnionIdPromotion promotion : promotions) {
            String skuId = promotion.getSkuId();
            if (StringUtils.isBlank(skuId)) {
                continue;
            }

            MaocheRobotCrawlerMessageProductDO productDO = new MaocheRobotCrawlerMessageProductDO();

            long reservePrice = 0L;
            long originalPrice = 0L;
            if (promotion.getPriceInfo() != null) {
                originalPrice = BigDecimal.valueOf(promotion.getPriceInfo().getPrice()).multiply(new BigDecimal(100)).longValue();
                reservePrice = BigDecimal.valueOf(promotion.getPriceInfo().getLowestPrice()).multiply(new BigDecimal(100)).longValue();
            }

            long commissionRate = 0L;
            long commission = 0L;
            if (promotion.getCommissionInfo() != null) {
                commissionRate = BigDecimal.valueOf(promotion.getCommissionInfo().getCommissionShare()).multiply(new BigDecimal(100)).longValue();
                commission = BigDecimal.valueOf(promotion.getCommissionInfo().getCommission()).multiply(new BigDecimal(100)).longValue();
            }

            String imgUrl = "";
            if (promotion.getImageInfo() != null && CollectionUtils.isNotEmpty(promotion.getImageInfo().getImageList())) {
                imgUrl = promotion.getImageInfo().getImageList().get(0).getUrl();
            }
            // 获取不到的话 取视频的封面图
            if (StringUtils.isBlank(imgUrl) && promotion.getVideoInfo() != null && promotion.getVideoInfo().get(0) != null) {
                JdUnionIdPromotion.VideoInfo videoInfo = promotion.getVideoInfo().get(0);
                List<JdUnionIdPromotion.Video> videoList = videoInfo.getVideoList();
                if (CollectionUtils.isNotEmpty(videoList)) {
                    JdUnionIdPromotion.Video video = videoList.get(0);
                    imgUrl = video.getImageUrl();
                }
            }

            String sellerId = "";
            String shopTitle = "";
            if (promotion.getShopInfo() != null) {
                shopTitle = promotion.getShopInfo().getShopName();
                sellerId = String.valueOf(promotion.getShopInfo().getShopId());
            }

            // 商品标题
            productDO.setItemUrl(promotion.getShortURL());
//            productDO.setRobotMsgId(message.getRobotMsgId());
//            productDO.setMsgId(message.getUiid());
            productDO.setAffType(getAffType());
            productDO.setResourceId(String.valueOf(promotion.getSkuId()));
            productDO.setInnerId("0");

            promotion.setImageInfo(null);
            productDO.setApiContent(JsonUtils.toJSONString(promotion));

            productDO.setCategory("京东");
            productDO.setTitle(promotion.getSkuName());

            productDO.setShortTitle("");
            productDO.setShopDsr("0");
            productDO.setCommissionRate(commissionRate);
            productDO.setShopName(shopTitle);
            productDO.setSellerId(sellerId);
            productDO.setPrice(reservePrice);
            productDO.setPictUrl(imgUrl);
            productDO.setVolume(0L);
            productDO.setStatus("NORMAL");
            productDO.setCreateBy("admin");
            productDO.setUpdateBy("admin");
//            productDO.setCreateDate(message.getCreateDate());
//            productDO.setUpdateDate(message.getUpdateDate());
            productDO.setRemarks("{}");

            productDOs.add(productDO);
        }

        if (CollectionUtils.isEmpty(productDOs)) {
            return null;
        }

        return productDOs;
    }

    public List<MaocheRobotCrawlerMessageProductDO> buildSpecialUriProducts(OceanContext context) {
        List<MaocheRobotCrawlerMessageProductDO> productDOs = new ArrayList<>();
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        String resourceId = messageSync.getUniqueHash();

        MaocheRobotCrawlerMessageProductDO productDO = new MaocheRobotCrawlerMessageProductDO();

        long reservePrice = 0L;
        long commissionRate = 0L;
        String imgUrl = "https://cat.zhizher.com/assets/userfiles/fileupload/202404/1784101138316550144.png";
        // 获取不到的话 取视频的封面图
        String sellerId = "";
        String shopTitle = "";

        // 商品标题
        productDO.setAffType(getAffType());
        productDO.setResourceId(resourceId);
        productDO.setInnerId("0");

        productDO.setCategory("京东");
        productDO.setTitle("外部链接");

        productDO.setShortTitle("");
        productDO.setShopDsr("0");
        productDO.setCommissionRate(commissionRate);
        productDO.setShopName(shopTitle);
        productDO.setSellerId(sellerId);
        productDO.setApiContent("");
        productDO.setPrice(reservePrice);
        productDO.setPictUrl(imgUrl);
        productDO.setVolume(0L);
        productDO.setStatus("NORMAL");
        productDO.setCreateBy("admin");
        productDO.setUpdateBy("admin");
        productDO.setRemarks("{}");

        productDOs.add(productDO);


        return productDOs;
    }

    @Override
    public void calSimilar(OceanContext context) {
        // 获取转链详情
        CommandContext command = context.getCommandContext();
        Map<String, CommandResponseV2> tbProductMap = context.getTbProductMap();
        SimilarContext similar = new SimilarContext();
        List<SimilarDetail> products = new ArrayList<>();
        // 淘宝为空
        List<String> failUrls = new ArrayList<>();

        if (command != null && CollectionUtils.isNotEmpty(command.listShortDetails())) {
            List<ShortUrlDetail> shortUrlDetails = command.listShortDetails();
            for (ShortUrlDetail url : shortUrlDetails) {
                String checkUrl = Optional.ofNullable(url.getReplaceUrl()).orElse(url.getContentUrl());
                if (BooleanUtils.isNotTrue(url.getApiRes())) {
                    failUrls.add(checkUrl);
                    continue;
                }
                // 京东商品
                JdUnionIdPromotion promotion = url.getPromotion();
                if (promotion != null) {
                    SimilarDetail detail = SimilarDetail.convertProduct(promotion);
                    if (detail == null) {
                        continue;
                    }
                    products.add(detail);
                }
            }

            similar.setNum(shortUrlDetails.size());
            similar.setProducts(products);
            similar.setFailUrls(failUrls);

            context.setSimilar(similar);
        } else if (MapUtils.isNotEmpty(tbProductMap)) {

            for (Map.Entry<String, CommandResponseV2> entry : tbProductMap.entrySet()) {
                SimilarDetail detail = SimilarDetail.convertProduct(entry.getValue());
                if (detail == null) {
                    continue;
                }
                products.add(detail);
            }

            similar.setNum(tbProductMap.size());
            context.setSimilar(similar);
        }
    }

    public void checkSimilar(OceanContext context) {
        // 获取转链详情
        SimilarContext similar = context.getSimilar();
        if (similar == null) {
            return;
        }

        String calCode = similar.calCode();

        // 判断3天前内是否存在
        OceanMessageCondition condition = new OceanMessageCondition();
        condition.setUniqueHash(calCode);
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

        // 修改状态为相似内容
        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        if (messageSync != null) {
            messageSync.setStatus(OceanStatusEnum.SIMILAR.name());
            messageSync.setUniqueHash(calCode);
        }
    }
}
