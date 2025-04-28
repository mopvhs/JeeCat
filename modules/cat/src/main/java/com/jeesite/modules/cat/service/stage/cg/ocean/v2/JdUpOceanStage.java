package com.jeesite.modules.cat.service.stage.cg.ocean.v2;

import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.inner.InnerApiService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.cg.third.tb.dto.GeneralConvertResp;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.stage.cg.ocean.exception.QueryThirdApiException;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandContext;
import com.jeesite.modules.cat.service.toolbox.dto.CommandDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JdUpOceanStage extends AbstraUpOceanStage {

    @Resource
    private DingDanXiaApiService dingDanXiaApiService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductService;

    @Resource
    private InnerApiService innerApiService;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private OceanSearchService oceanSearchService;

    @Resource
    private CommandService commandService;

    @Resource
    private TbUpOceanStage tbUpOceanStage;

    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

    @Override
    public String getAffType() {
        return "jd";
    }

    @Override
    public Pattern getPattern() {
        return CommandService.jd;
    }

    @Override
    public void queryProductFromThirdApi(OceanUpContext context) {
        // 1. 查询淘宝api获取商品数据
        // 2. 保存商品数据
        // 3. 保存商品数据到消息中

        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        String content = messageSync.getMsg();

        // 京东转链
        CommandContext command = new CommandContext();
        command.setContent(content);
        command.setRelationId(messageSync.getId());
        Result<CommandDTO> doDwz = commandService.doDwz(command);
        if (doDwz == null || !doDwz.isSuccess()) {
            throw new QueryThirdApiException(QueryThirdApiException.QUERY_FAIL, "转换异常");
        }

        List<ShortUrlDetail> urlDetails = command.listShortDetails();
        if (CollectionUtils.isEmpty(urlDetails)) {
            Map<String, Object> apiError = new HashMap<>();
            apiError.put("url", "正则匹配链接未找到");
            messageSync.addApiError(apiError);
            throw new QueryThirdApiException(QueryThirdApiException.QUERY_FAIL, "正则匹配链接未找到");
        }
        Map<String, Object> apiError = new HashMap<>();

        boolean isSpecialUri = false;
        List<JdUnionIdPromotion> promotions = new ArrayList<>();
        Map<String, GeneralConvertResp> tbProductMap = new HashMap<>();

        for (ShortUrlDetail item : urlDetails) {
            if (isSpecialUri(item.getContentUrl()) && !isSpecialUri) {
                isSpecialUri = true;
            }

            JdUnionIdPromotion promotion = item.getPromotion();
            if (promotion != null) {
                promotions.add(promotion);
            }

            if (BooleanUtils.isNotTrue(item.getApiRes()) && StringUtils.isNotBlank(item.getContentUrl()) && StringUtils.isNotBlank(item.getErrorMsg())) {
                apiError.put(item.getContentUrl(), item.getErrorMsg());
            }

            GeneralConvertResp tbProduct = item.getTbProduct();
            if (tbProduct != null) {
                tbProductMap.put(item.getReplaceUrl(), tbProduct);
            }
        }

        context.setCommandContext(command);
        context.setJdProducts(promotions);
        context.setTbProductMap(tbProductMap);

        // 如果不存在商品，并且只存在特殊uri
        if (CollectionUtils.isEmpty(promotions) && isSpecialUri) {
            context.setOnlySpecialUri(true);
            return;
        }

        if (CollectionUtils.isEmpty(promotions) && MapUtils.isEmpty(tbProductMap)) {
            apiError.put("url", "订单侠jd接口信息查询未找到数据");
            messageSync.addApiError(apiError);
            throw new QueryThirdApiException(QueryThirdApiException.QUERY_FAIL, "订单侠jd接口信息查询未找到数据");
        }
    }

    @Override
    public void saveMessageAndProduct(OceanUpContext context) {

        MaocheRobotCrawlerMessageSyncDO messageSync = context.getMessageSync();
        List<JdUnionIdPromotion> jdProducts = context.getJdProducts();
        List<MaocheRobotCrawlerMessageProductDO> messageProducts = context.getMessageProducts();
        if (messageSync == null || CollectionUtils.isEmpty(messageProducts)) {
            throw new IllegalArgumentException("messageSync or data or messageProducts is null");
        }
        List<String> resourceIds = new ArrayList<>();

        List<String> hashProducts = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(jdProducts)) {
            for (JdUnionIdPromotion jd : jdProducts) {
                if (jd == null) {
                    continue;
                }
                if (StringUtils.isNotBlank(jd.getSkuId())) {
                    resourceIds.add(jd.getSkuId());

                    String hashProduct = "";
                    JdUnionIdPromotion.ShopInfo shopInfo = jd.getShopInfo();
                    if (shopInfo != null) {
                        hashProduct += shopInfo.getShopId();
                    }
                    hashProduct += "_" + jd.getSkuName();

                    hashProducts.add(hashProduct);
                }
            }
        } else if (context.isOnlySpecialUri()) {
            resourceIds.add(messageSync.getUniqueHash());
        } else {
            if (MapUtils.isNotEmpty(context.getTbProductMap())) {
                tbUpOceanStage.saveMessageAndProduct(context);
                return;
            } else {
                throw new IllegalArgumentException("messageSync or data or jdProducts is null");
            }
        }
        String hash = null;
        if (!context.isIgnoreSimHash() && CollectionUtils.isNotEmpty(hashProducts)) {
            // 排序
            hashProducts = hashProducts.stream().sorted().collect(Collectors.toList());
            // toJsonString -> md5
            hash = Md5Utils.md5(JsonUtils.toJSONString(hashProducts));
        }

        String status = OceanStatusEnum.NORMAL.name();
        // todo 有部分是忽略相似度判断的，需要忽略
        if (StringUtils.isNotBlank(hash)) {
            // 获取今天凌晨4点的时间戳
            Date startTime = DateUtils.getOfDayFirst(new Date(), 2);
            // 如果当前时间小于4点，则获取前一天的数据
            if (startTime.getTime() > System.currentTimeMillis()) {
                startTime = DateUtils.getOfDayFirst(DateUtils.addDays(new Date(), -1), 4);
            }

            String date = DateTimeUtils.getStringDate(startTime);
            List<MaocheRobotCrawlerMessageSyncDO> simProductSyncMsgList = maocheRobotCrawlerMessageSyncDao.listByProductUniqueHash(hash, "NORMAL", date);
            if (CollectionUtils.isNotEmpty(simProductSyncMsgList)) {
                simProductSyncMsgList = simProductSyncMsgList.stream().filter(i -> !i.getId().equals(messageSync.getId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(simProductSyncMsgList)) {
                    status = OceanStatusEnum.SIMILAR.name();
                }
            }
        }

        // 获取商品额时间
        Date createDate = messageSync.getCreateDate();
        int newProduct = 0;
        // 获取3天前的开始时间
        long startTime = DateUtils.getOfDayFirst(DateUtils.addDays(createDate, -3)).getTime();
        // 获取今天开始时间
        long endTime = DateUtils.getOfDayFirst(createDate).getTime() - 1;
        // 判断3天前内是否存在
        OceanMessageCondition condition = new OceanMessageCondition();
        condition.setResourceIds(resourceIds);
        condition.setAffType(getAffType());
        condition.setGteCreateDate(startTime);
        condition.setLteCreateDate(endTime);
        ElasticSearchData<MaocheMessageSyncIndex, CatProductBucketTO> searchMsg = oceanSearchService.searchMsg(
                condition,
                null,
                null,
                null,
                0, 1);
        if (searchMsg != null && CollectionUtils.isEmpty(searchMsg.getDocuments())) {
            newProduct = 1;
        }

        CommandContext commandContext = context.getCommandContext();
        boolean allSuccess = true;
        if (commandContext != null && StringUtils.isNotBlank(commandContext.getResContent())) {

            List<ShortUrlDetail> shortUrlDetails = commandContext.listShortDetails();
            // 判断是否全部成功
            for (ShortUrlDetail detail : shortUrlDetails) {
                if (StringUtils.isBlank(detail.getReplaceUrl())) {
                    allSuccess = false;
                    break;
                }
            }
            String resContent = commandContext.getResContent();
            // 添加头尾
            if (allSuccess) {
                resContent = "✨有好价✨\n" + resContent;
                resContent = resContent + "---------------------\n" + "自助查车 dwz.cn/qveM26UV";
            }

            messageSync.setMsg(resContent);
        }

        Collections.sort(resourceIds);
        messageSync.setResourceIds(StringUtils.join(resourceIds, ","));

        messageSync.addNewProduct(newProduct);
        messageSync.addCommandContext(context.getCommandContext());
        messageSync.setProcessed(1L);
        messageSync.setResourceIds(StringUtils.join(resourceIds, ","));
        messageSync.setStatus(status);
        messageSync.setProductHash(hash);

        boolean res = maocheRobotCrawlerMessageSyncService.updateById(messageSync);
        if (!res) {
            String message = JsonUtils.toJSONString(messageSync);
            log.error("messageSync is update fail message:{}", message);
            throw new IllegalArgumentException("messageSync is update fail message:" + message);
        }
        for (MaocheRobotCrawlerMessageProductDO productDO : messageProducts) {
            try {
                fillMessageInfo2Product(messageSync, productDO);
                maocheRobotCrawlerMessageProductService.save(productDO);
            } catch (Exception e) {
                log.error("save messageProduct error message:{}", JsonUtils.toJSONString(productDO), e);
            }
        }

    }

    @Override
    public void fillMessageInfo2Product(MaocheRobotCrawlerMessageSyncDO message, MaocheRobotCrawlerMessageProductDO productDO) {
        productDO.setRobotMsgId(message.getRobotMsgId());
        productDO.setMsgId(message.getUiid());
        productDO.setAffType(message.getAffType());
        productDO.setCreateDate(message.getCreateDate());
        productDO.setUpdateDate(message.getUpdateDate());
    }

    public void buildUriMessageProducts(OceanUpContext context) {
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
        context.setMessageProducts(productDOs);
    }

    public boolean isSpecialUri(String uri) {
        if (StringUtils.isBlank(uri)) {
            return false;
        }
        return
//                uri.contains("y-03.cn") ||
//                uri.contains("3.cn") ||
                uri.contains("jd.cn") ||
                uri.contains("t.cn") ||
                uri.contains("s.q5url.cn/yA7U") ||
                uri.contains("985.so") ||
                uri.contains("kzurl11.cn") ||
                uri.contains("kurl06.cn");
    }

    /**
     * 是否包含特殊uri
     * @param content
     * @return
     */
    public boolean hadSpecialUri(String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }
        String[] split = StringUtils.split(content, "\n");

        for (String item : split) {
            Matcher matcher = CommandService.jd.matcher(item);
            if (matcher.find()) {
                String group = matcher.group();
                if (isSpecialUri(group)) {
                    return true;
                }
            } else {
                if (isSpecialUri(item)) {
                    return true;
                }
            }
        }

        return false;
    }
}
