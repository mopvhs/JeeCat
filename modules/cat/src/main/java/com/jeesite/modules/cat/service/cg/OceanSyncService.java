package com.jeesite.modules.cat.service.cg;

import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheSyncDataInfoDO;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.MaocheSyncDataInfoService;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanStage;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OceanSyncService {

    @Resource
    private MaocheSyncDataInfoService maocheSyncDataInfoService;

    @Resource
    private MaocheRobotCrawlerMessageService maocheRobotCrawlerMessageService;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private OceanStage tbOceanStage;

    @Resource
    private OceanStage jdOceanStage;

    public Result<String> sync() {
        MaocheSyncDataInfoDO dataInfo = maocheSyncDataInfoService.getLatestSyncDataInfo("maoche_robot_crawler_message");
        long syncDataId = 0;
        long maxId = 0;
        int step = 50;
        if (dataInfo != null) {
            syncDataId = dataInfo.getIid();
            maxId = NumberUtils.toLong(dataInfo.getSyncMaxId());
            step = Optional.ofNullable(dataInfo.getStep()).orElse(step);
            if (maxId <= 0) {
                return Result.ERROR(400, "同步数据异常，最大同步id为0");
            }
        }

        step = 50;

        List<String> affTypes = new ArrayList<>();
        affTypes.add("tb");
        affTypes.add("jd");
        // 查询数据
        List<MaocheRobotCrawlerMessageDO> messages = maocheRobotCrawlerMessageService.startById(maxId, step, affTypes);
        if (CollectionUtils.isEmpty(messages)) {
            return Result.OK("暂无数据");
        }

        String offset = String.valueOf(messages.get(messages.size() - 1).getId());
        // todo yhq 处理数据
        // 一个一个的解析
        for (MaocheRobotCrawlerMessageDO message : messages) {
            offset = String.valueOf(message.getId());
            try {
                OceanContext context = new OceanContext(message);
                if (message.getAffType().equals("tb")) {
                    tbOceanStage.process(context);
                } else if (message.getAffType().equals("jd")) {
                    jdOceanStage.process(context);
                }
            } catch (Exception e) {
                break;
            }

//            else if (message.getAffType().equals("jd")) {
//
//                Map<String, String> urlMap = new HashMap<>();
//                List<String> urls = new ArrayList<>();
//                String[] split = StringUtils.split(content, "\n");
//                for (String item : split) {
//                    Matcher matcher = CommandService.jd.matcher(item);
//                    if (matcher.find()) {
//                        String group = matcher.group();
//                        urlMap.put(group, "");
//                        urls.add(group);
//                    }
//                }
//
//                if (MapUtils.isEmpty(urlMap)) {
//                    Map<String, Object> remarks = new HashMap<>();
//                    remarks.put("api_error", "正则匹配链接未找到");
//                    messageSyncDO.setStatus("FAIL");
//                    messageSyncDO.setRemarks(JsonUtils.toJSONString(remarks));
//                    // 写入
//                    maocheRobotCrawlerMessageSyncService.save(messageSyncDO);
//                    return Result.ERROR(500, "需要替换的链接分析失败");
//                }
//
//                List<JdUnionIdPromotion> promotions = new ArrayList<>();
//                for (String url : urls) {
//                    Result<JdUnionIdPromotion> result = dingDanXiaApiService.jdByUnionidPromotion("FHPOsYO7zki7tcrxp0amyGMP7wxVkbU3", url, 1002248572L, 3100684498L);
//                    if (Result.isOK(result)) {
//                        JdUnionIdPromotion promotion = result.getResult();
//                        if (promotion.getSkuId() == null || promotion.getSkuId() <= 0) {
//                            continue;
//                        }
//                        promotions.add(promotion);
//                    }
//                }
//
//                if (CollectionUtils.isEmpty(promotions)) {
//                    continue;
//                }
//
//                List<Long> skuIds = promotions.stream().map(JdUnionIdPromotion::getSkuId).toList();
//
//                messageSyncDO.setProcessed(1L);
//                messageSyncDO.setResourceIds(StringUtils.join(skuIds, ","));
//                messageSyncDO.setStatus("NORMAL");
//                maocheRobotCrawlerMessageSyncService.save(messageSyncDO);
//
//                for (JdUnionIdPromotion promotion : promotions) {
//                    MaocheRobotCrawlerMessageProductDO productDO = buildMessageProduct(messageSyncDO, promotion);
//                    maocheRobotCrawlerMessageProductService.save(productDO);
//                }
//            }
        }

        // 更新位点
        maocheSyncDataInfoService.addOrUpdateOffset(syncDataId, "maoche_robot_crawler_message", offset);

        return Result.OK("操作完成");
    }
}
