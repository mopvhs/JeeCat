package com.jeesite.modules.cgcat;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.FlameProxyHttpService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.MaocheSyncDataInfoService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.OceanSyncService;
import com.jeesite.modules.cat.service.cg.inner.InnerApiService;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
import com.jeesite.modules.cat.service.cg.third.VeApiService;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.stage.cg.ocean.AbstraOceanStage;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanStage;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanContentHelper;
import com.jeesite.modules.cat.service.stage.cg.ocean.helper.OceanMonitorHelper;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.OceanUpContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.OceanUpStage;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandDTO;
import com.jeesite.modules.cat.xxl.job.CgProductDeleteSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.CgProductSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.ProductAutoAuditXxlJob;
import com.jeesite.modules.cat.xxl.job.ocean.CgOceanAnalysisXxlJob;
import com.jeesite.modules.cgcat.dto.CommandRequest;
import com.jeesite.modules.cgcat.dto.TbProductRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Resource
    private OceanStage jdOceanStage;

    @Resource
    private OceanUpStage tbUpOceanStage;

    @Resource
    private OceanUpStage jdUpOceanStage;

    @Resource
    private VeApiService veApiService;

    @Resource
    private CgOceanAnalysisXxlJob cgOceanAnalysisXxlJob;


    private RateLimiter limiter = RateLimiter.create(1);

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

    @RequestMapping(value = "toolbox/msg/sync/analysisXxlJob")
    public Result<?> analysisXxlJob() {

        try {
            cgOceanAnalysisXxlJob.execute();
        } catch (Exception e) {
            log.error("AnalysisXxlJob", e);
            return Result.ERROR(500, "AnalysisXxlJob");
        }
        return Result.OK("AnalysisXxlJob OK");
    }

    @Resource
    private ProductAutoAuditXxlJob productAutoAuditXxlJob;

    @RequestMapping(value = "toolbox/product/online/productAutoAuditXxlJob")
    public Result<?> productAutoAuditXxlJob() {

        try {
            productAutoAuditXxlJob.execute();
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
                String message = result.getMessage();

                cacheService.setWithExpireTime(key, message, (int) TimeUnit.SECONDS.toSeconds(10));

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

    /**
     * 补数据
     * @param ids
     * @return
     */
    @RequestMapping(value = "/maoche/robot/message/sync")
    @ResponseBody
    public Result<String> sync(String ids) {
        String[] split = StringUtils.split(ids, ",");

        MaocheRobotCrawlerMessageDO query = new MaocheRobotCrawlerMessageDO();
        query.setId_in(split);
        List<MaocheRobotCrawlerMessageDO> messages = maocheRobotCrawlerMessageService.findList(query);

        for (MaocheRobotCrawlerMessageDO message : messages) {
            OceanContext context = new OceanContext(message);

            // afftype干预订正
            String affType = message.getAffType();
            String msg = message.getMsg();
            affType = AbstraOceanStage.fixAffType(msg, affType);
            message.setAffType(affType);
            if (affType.equals("tb")) {

                tbOceanStage.process(context);
            } else if (affType.equals("jd")) {

                jdOceanStage.process(context);
            }
        }

        return Result.OK("操作完成");
    }

    /**
     * 补数据
     * @param ids
     * @return
     */
    @RequestMapping(value = "/maoche/robot/message/sync/v2")
    @ResponseBody
    public Result<String> syncV2(String ids) {
        String[] split = StringUtils.split(ids, ",");

        List<Long> collect = Arrays.stream(split).map(NumberUtils::toLong).collect(Collectors.toList());
        List<MaocheRobotCrawlerMessageSyncDO> messages = maocheRobotCrawlerMessageSyncDao.listByIds(collect);

        for (MaocheRobotCrawlerMessageSyncDO message : messages) {
            OceanUpContext context = new OceanUpContext(message);

            // afftype干预订正
            String affType = message.getAffType();
            if (affType.equals("tb")) {
                tbUpOceanStage.process(context);
            } else if (affType.equals("jd")) {
                jdUpOceanStage.process(context);
            }
        }

        return Result.OK("操作完成");
    }

    @Resource
    private OceanSyncService oceanSyncService;

    /**
     * 消息同步测试
     * @return
     */
    @RequestMapping(value = "/maoche/test/robot/message/sync")
    @ResponseBody
    public Result<String> syncTest() {

        Result<String> sync = oceanSyncService.sync();

        return sync;
    }

    /**
     * 初始池消息同步测试
     * @return
     */
    @RequestMapping(value = "/maoche/test/robot/message/robotMsg")
    @ResponseBody
    public Result<String> robotMsg() {

        Result<String> sync = oceanSyncService.robotMsg();

        return sync;
    }

    /**
     * 初始池消息同步测试
     * @return
     */
    @RequestMapping(value = "/maoche/test/robot/message/stat/log")
    @ResponseBody
    public String statLog() {

        StringBuilder builder = new StringBuilder();
        List<String> affTypes = new ArrayList<>();
        affTypes.add("tb");
        affTypes.add("jd");
        String date = DateTimeUtils.getStringDateShort(new Date());

        for (String affType : affTypes) {
            String numKey = OceanMonitorHelper.getOceanNumKey(affType);
            String urlSizeKey = OceanMonitorHelper.getOceanUrlSizeKey(affType);
            String tsTotalKey = OceanMonitorHelper.getOceanTsTotalKey(affType);

            long milliseconds = NumberUtils.toLong(cacheService.get(tsTotalKey));
            // 将毫秒转换为秒
            long ms = milliseconds % 1000;
            long totalSeconds = milliseconds / 1000;
            // 计算小时、分钟和剩余秒数
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            // 格式化输出
            StringBuilder result = new StringBuilder();
            if (hours > 0) {
                result.append(hours).append("小时");
            }
            if (minutes > 0 || hours > 0) {
                result.append(minutes).append("分");
            }
            result.append(seconds).append("秒");
            result.append(ms).append("毫秒");


            builder.append(affType).append("单子解析次数：").append(cacheService.get(numKey)).append("\n");
            builder.append(affType).append("链接次数：").append(cacheService.get(urlSizeKey)).append("\n");
            builder.append(affType).append("总耗时：").append(result.toString()).append("\n");

            for (OceanMonitorHelper.OceanTimeRangeEnum obe : OceanMonitorHelper.OceanTimeRangeEnum.values()) {
                String redisKey = OceanMonitorHelper.OceanTimeRangeEnum.getRedisKey(affType, date, obe);
                builder.append(affType).append("区间[").append(obe.getKey()).append("]：").append(cacheService.get(redisKey)).append("\n");
            }

            // 相同消息数量
            String robotMsgSameNumKey = OceanMonitorHelper.getRobotMsgSameNumKey(affType);
            long sameMsg = NumberUtils.toLong(cacheService.get(robotMsgSameNumKey));

            String robotMsgNumKey = OceanMonitorHelper.getRobotMsgNumKey(affType);
            long msg = NumberUtils.toLong(cacheService.get(robotMsgNumKey));
            builder.append(affType).append("机器人采集【相同/总量】").append(sameMsg).append("/").append(msg).append("\n");

            builder.append("\n");
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        long milliseconds = 1321312L;

        // 将毫秒转换为秒
        long ms = milliseconds % 1000;
        long totalSeconds = milliseconds / 1000;

        // 计算小时、分钟和剩余秒数
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;


        // 格式化输出
        StringBuilder result = new StringBuilder();
        if (hours > 0) {
            result.append(hours).append("小时");
        }
        if (minutes > 0 || hours > 0) {
            result.append(minutes).append("分");
        }
        result.append(seconds).append("秒");
        result.append(ms).append("毫秒");

        System.out.println(result.toString());
    }




    @Resource
    private ElasticSearch7Service elasticSearch7Service;
    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

    /**
     * 消息同步测试
     * @return
     */
    @RequestMapping(value = "/maoche/test/robot/message/fixMsgNgram")
    @ResponseBody
    public Result<String> fixMsgNgram() {

        long id = 0;
        int limit = 10;
        while (true) {
            try {
                List<MaocheRobotCrawlerMessageSyncDO> all = maocheRobotCrawlerMessageSyncDao.findAll(id, limit);
                if (CollectionUtils.isEmpty(all)) {
                    break;
                }
                id = all.get(all.size() - 1).getUiid();
                for (MaocheRobotCrawlerMessageSyncDO item : all) {
                    Map<String, Object> messageSyncIndex = new HashMap<>();
                    messageSyncIndex.put("id", item.getUiid());
                    messageSyncIndex.put("msgNgram", item.getMsg());

                    elasticSearch7Service.update(Collections.singletonList(messageSyncIndex), ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);
                }

            } catch (Exception e) {

            }
        }

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


    @RequestMapping("/tb/search")
    public Result<?> tbSearch(String itemId) {
        String vekey = "V73687541H40026415";
        String pid = "mm_30153430_909250463_109464700418";

        Result<JSONArray> jsonArrayResult = veApiService.tbSearch(vekey, itemId, pid, null);

        Result<JSONObject> result = dingDanXiaApiService.idPrivilege("FHPOsYO7zki7tcrxp0amyGMP7wxVkbU3", itemId, "mm_30153430_909250463_109464700418");

        return result;
    }

    @Resource
    private FlameProxyHttpService flameProxyHttpService;

    @RequestMapping("/tb/jiexi/iid")
    public Result<Long> jiexiIid(String itemId) {
//        Map<String, Object> objectMap = new HashMap<>();
//        objectMap.put("detail", 2);
//        // 获取到口令详情
//        Result<CommandResponse> commonCommand = tbApiService.getCommonCommand(itemId, objectMap);
//        if (!Result.isOK(commonCommand)) {
//            return Result.ERROR(500, "获取口令详情失败");
//        }
//        CommandResponse result = commonCommand.getResult();
////        String itemUrl = result.getItemUrl();
//        String itemUrl = result.getSclickUrl();
//
//        Result<Long> analysisTbIid = innerApiService.getAnalysisTbIid(itemUrl);

//        return analysisTbIid;
        return Result.OK(0L);
    }

    @RequestMapping("/tb/jiexi/item/url/iid")
    public Result<Long> jiexiItemUrlIid(String itemUrl) {

        Result<Long> analysisTbIid = innerApiService.getAnalysisTbIid(itemUrl);

        return analysisTbIid;
    }

    @RequestMapping("/tb/jiexi/item/url/batch/update")
    public Result<String> batchUpdate(String ids) {

        String[] split = StringUtils.split(ids, ",");
        List<Long> longList = Arrays.stream(split).map(Long::parseLong).collect(Collectors.toList());

        MaocheRobotCrawlerMessageSyncDO messageSyncDO = new MaocheRobotCrawlerMessageSyncDO();
        messageSyncDO.setUiid_in(longList);
        messageSyncDO.setStatus("NORMAL");
        List<MaocheRobotCrawlerMessageSyncDO> list = maocheRobotCrawlerMessageSyncService.findList(messageSyncDO);

        for (MaocheRobotCrawlerMessageSyncDO item : list) {
            item.setStatus("DELETE");
            item.setRemarks("{}");
        }

        maocheRobotCrawlerMessageSyncService.updateBatch(list);

        return Result.OK("");
    }


    public static MatchContent calMatchContent(Pattern pattern, String content) {
        StringBuilder calContent = new StringBuilder();
        String[] split = content.split("\n");
        String regex = "[\\w\\d*.()/]+";

        List<Matcher> matchers = new ArrayList<>();

        for (String item : split) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                matchers.add(matcher);
                // 去除所有的数字和符号还有英文
                calContent.append(item.replaceAll(regex, ""));
            } else {
                calContent.append(item);
            }
        }

        MatchContent matchContent = new MatchContent();

        matchContent.setCalContent(calContent.toString());
        matchContent.setMatchers(matchers);
        matchContent.setCalMd5(Md5Utils.md5(calContent.toString()));
        return matchContent;
    }

    @Data
    private static class MatchContent {

        private List<Matcher> matchers;

        private String calContent;

        private String calMd5;
    }


}
