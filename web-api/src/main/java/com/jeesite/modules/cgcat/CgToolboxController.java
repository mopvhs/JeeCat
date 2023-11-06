package com.jeesite.modules.cgcat;


import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.entity.MaocheSyncDataInfoDO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.MaocheSyncDataInfoService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.inner.InnerApiService;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanStage;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandDTO;
import com.jeesite.modules.cat.xxl.job.CgProductDeleteSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.CgProductSyncXxlJob;
import com.jeesite.modules.cgcat.dto.CommandRequest;
import com.jeesite.modules.cgcat.dto.TbProductRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class CgToolboxController {

    @Resource
    private CommandService commandService;

    @Resource
    private CgProductDeleteSyncXxlJob cgProductDeleteSyncXxlJob;

    @Resource
    private CgProductSyncXxlJob cgProductSyncXxlJob;

    @Resource
    private CacheService cacheService;

    @Resource
    private InnerApiService innerApiService;

    @Resource
    private MaocheSyncDataInfoService maocheSyncDataInfoService;

    @Resource
    private MaocheRobotCrawlerMessageService maocheRobotCrawlerMessageService;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductService;

    @Resource
    private TbApiService tbApiService;

    @Resource
    private DingDanXiaApiService dingDanXiaApiService;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private OceanStage tbOceanStage;

    /**
     * 口令信息替换
     * @param command
     * @return
     */
    @RequestMapping(value = "toolbox/command/exchange")
    public Result<CommandDTO> exchangeCommand(@RequestBody CommandRequest command) {

        if (command == null || StringUtils.isBlank(command.getContent()) || StringUtils.isBlank(command.getType())) {
            return Result.ERROR(500, "参数不能为空");
        }

        Result<CommandDTO> result = commandService.exchangeCommand(command.getContent(), command.getType());

        return result;
    }

    @RequestMapping(value = "toolbox/product/del/sync")
    public Result<?> syncDelProduct() {

        try {
            cgProductDeleteSyncXxlJob.execute();
        } catch (Exception e) {
            log.error("同步删除商品失败", e);
            return Result.ERROR(500, "同步删除商品失败");
        }
        return Result.OK("OK");
    }

    @RequestMapping(value = "toolbox/product/online/sync")
    public Result<?> syncProduct() {

        try {
            cgProductSyncXxlJob.execute();
        } catch (Exception e) {
            log.error("同步商品失败", e);
            return Result.ERROR(500, "同步商品失败");
        }
        return Result.OK("OK");
    }

    @RequestMapping(value = "toolbox/test")
    public Result<?> test(Long robotMsgId) {

        MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
        query.setRobotMsgId(robotMsgId);
        MaocheRobotCrawlerMessageSyncDO syncDO = maocheRobotCrawlerMessageSyncService.get(query);

        return Result.OK("OK");
    }

    @RequestMapping(value = "toolbox/tb/product/add")
    public Result<?> addTbProduct(@RequestBody TbProductRequest request) {

        try {
            if (request == null || StringUtils.isBlank(request.getNumIid())) {
                return Result.ERROR(400, "参数不能为空");
            }

            String key = "toolbox_" + request.getNumIid();
            // redis幂等
            String s = cacheService.get(key);
            if (StringUtils.isNotBlank(s)) {
                return Result.ERROR(401, "请勿重复操作");
            }

            // 调用接口
            Result<String> result = innerApiService.syncTbProduct(request.getNumIid());
            if (!Result.isOK(result)) {
                Long ttl = cacheService.ttl(key);
                cacheService.setWithExpireTime(key, "1", (int) TimeUnit.SECONDS.toSeconds(10));

                String message = result.getMessage();
                if (ttl != null && ttl > 0) {
                    message = message + "，\n请" + ttl + "秒后重试" + "\n请" + ttl + "秒后重试" + "\n请" + ttl + "秒后重试" + "\n请" + ttl + "秒后重试" + "\n请" + ttl + "秒后重试";
                }
                result.setMessage(message);
                return result;
            }

            cacheService.setWithExpireTime(key, "1", (int) TimeUnit.SECONDS.toSeconds(10));
        } catch (Exception e) {
            log.error("同步商品失败", e);
            return Result.ERROR(500, "同步商品失败");
        }
        return Result.OK("入库中");
    }

    @RequestMapping(value = "/maoche/robot/message/sync")
    @ResponseBody
    public Result<String> sync(@RequestParam(value = "times", required = false, defaultValue = "1") int times) {

        for (int i = 0; i < times; i++) {
            sync2();
        }

        return Result.OK("OK");
    }
    // 获取规则模板 - specification
    public Result<String> sync2() {
        MaocheSyncDataInfoDO dataInfo = maocheSyncDataInfoService.getLatestSyncDataInfo("maoche_robot_crawler_message");
        long syncDataId = 0;
        long maxId = 0;
        int step = 100;
        if (dataInfo != null) {
            syncDataId = dataInfo.getIid();
            maxId = NumberUtils.toLong(dataInfo.getSyncMaxId());
            step = Optional.ofNullable(dataInfo.getStep()).orElse(step);
            if (maxId <= 0) {
                return Result.ERROR(400, "同步数据异常，最大同步id为0");
            }
        }

        step = 10;

        List<String> affTypes = new ArrayList<>();
        affTypes.add("tb");
//        affTypes.add("jd");
        // 查询数据
        List<MaocheRobotCrawlerMessageDO> messages = maocheRobotCrawlerMessageService.startById(maxId, step, affTypes);
        if (CollectionUtils.isEmpty(messages)) {
            return Result.OK("暂无数据");
        }

        // todo yhq 处理数据
        // 一个一个的解析
        for (MaocheRobotCrawlerMessageDO message : messages) {
            OceanContext context = new OceanContext(message);

            String content = message.getMsg();
            MaocheRobotCrawlerMessageSyncDO messageSyncDO = buildMessageSync(message);

            if (message.getAffType().equals("tb")) {

                tbOceanStage.process(context);


            } else if (message.getAffType().equals("jd")) {

                Map<String, String> urlMap = new HashMap<>();
                List<String> urls = new ArrayList<>();
                String[] split = StringUtils.split(content, "\n");
                for (String item : split) {
                    Matcher matcher = CommandService.jd.matcher(item);
                    if (matcher.find()) {
                        String group = matcher.group();
                        urlMap.put(group, "");
                        urls.add(group);
                    }
                }

                if (MapUtils.isEmpty(urlMap)) {
                    Map<String, Object> remarks = new HashMap<>();
                    remarks.put("api_error", "正则匹配链接未找到");
                    messageSyncDO.setStatus("FAIL");
                    messageSyncDO.setRemarks(JsonUtils.toJSONString(remarks));
                    // 写入
                    maocheRobotCrawlerMessageSyncService.save(messageSyncDO);
                    return Result.ERROR(500, "需要替换的链接分析失败");
                }

                List<JdUnionIdPromotion> promotions = new ArrayList<>();
                for (String url : urls) {
                    Result<JdUnionIdPromotion> result = dingDanXiaApiService.jdByUnionidPromotion("FHPOsYO7zki7tcrxp0amyGMP7wxVkbU3", url, 1002248572L, 3100684498L);
                    if (Result.isOK(result)) {
                        JdUnionIdPromotion promotion = result.getResult();
                        if (promotion.getSkuId() == null || promotion.getSkuId() <= 0) {
                            continue;
                        }
                        promotions.add(promotion);
                    }
                }

                if (CollectionUtils.isEmpty(promotions)) {
                    continue;
                }

                List<Long> skuIds = promotions.stream().map(JdUnionIdPromotion::getSkuId).toList();

                messageSyncDO.setProcessed(1L);
                messageSyncDO.setResourceIds(StringUtils.join(skuIds, ","));
                messageSyncDO.setStatus("NORMAL");
                maocheRobotCrawlerMessageSyncService.save(messageSyncDO);

                for (JdUnionIdPromotion promotion : promotions) {
                    MaocheRobotCrawlerMessageProductDO productDO = buildMessageProduct(messageSyncDO, promotion);
                    maocheRobotCrawlerMessageProductService.save(productDO);
                }
            }
        }

        // 更新位点
        maocheSyncDataInfoService.addOrUpdateOffset(syncDataId, "maoche_robot_crawler_message", String.valueOf(messages.get(messages.size() - 1).getId()));

        return Result.OK("操作完成");
    }


    private MaocheRobotCrawlerMessageSyncDO buildMessageSync(MaocheRobotCrawlerMessageDO message) {

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

        return sync;
    }


    private MaocheRobotCrawlerMessageProductDO buildMessageProduct(MaocheRobotCrawlerMessageSyncDO message, JdUnionIdPromotion promotion) {

        MaocheRobotCrawlerMessageProductDO productDO = new MaocheRobotCrawlerMessageProductDO();

        // 商品标题
        JdUnionIdPromotion.ShopInfo shopInfo = promotion.getShopInfo();
        JdUnionIdPromotion.PriceInfo priceInfo = promotion.getPriceInfo();

        promotion.setImageInfo(null);

        productDO.setRobotMsgId(message.getRobotMsgId());
        productDO.setMsgId(message.getUiid());
        productDO.setAffType(message.getAffType());
        productDO.setResourceId(String.valueOf(promotion.getSkuId()));
        productDO.setInnerId("0");
        productDO.setApiContent(JsonUtils.toJSONString(promotion));
        productDO.setCategory("");
        productDO.setTitle(promotion.getSkuName());

        productDO.setShortTitle("");
        productDO.setShopDsr("0");
        productDO.setShopName(shopInfo.getShopName());
        productDO.setSellerId(String.valueOf(shopInfo.getShopId()));
        productDO.setPrice(new BigDecimal(String.valueOf(priceInfo.getLowestPrice())).multiply(new BigDecimal(100)).longValue());
        productDO.setVolume(0L);
        productDO.setStatus("NORMAL");
        productDO.setCreateBy("admin");
        productDO.setUpdateBy("admin");
        productDO.setCreateDate(message.getCreateDate());
        productDO.setUpdateDate(message.getUpdateDate());
        productDO.setRemarks("");

        return productDO;
    }


}
