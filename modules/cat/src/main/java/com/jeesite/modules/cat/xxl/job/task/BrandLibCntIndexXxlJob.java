package com.jeesite.modules.cat.xxl.job.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaochePushTaskRuleDO;
import com.jeesite.modules.cat.service.MaochePushTaskRuleService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibService;
import com.jeesite.modules.cat.service.es.BrandLibEsService;
import com.jeesite.modules.cat.service.es.TaskEsService;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 品牌库索引计数更新
 */
@Slf4j
@Component
public class BrandLibCntIndexXxlJob extends IJobHandler {

    @Resource
    private MaochePushTaskRuleService maochePushTaskRuleService;

    @Resource
    private BrandLibEsService brandLibEsService;

    @Resource
    private BrandLibService brandLibService;

    @Resource
    private TaskEsService taskEsService;

    @Override
    @XxlJob("brandLibCntIndexXxlJob")
    public void execute() throws Exception {
        // 获取所有的品牌库
        List<MaochePushTaskRuleDO> rules = maochePushTaskRuleService.getAllBrandLib();
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }

        for (MaochePushTaskRuleDO rule : rules) {
            Map<String, Object> params = new HashMap<>();

            List<String> keywords = JsonUtils.toReferenceType(rule.getKeyword(), new TypeReference<List<String>>() {
            });
            // 群今日新增 - 外部发单数量
            params.put("groupDailyInc", brandLibService.getKeywordsOceanCnt(keywords));
            // 库今日新增 - 此关键词新抓取到的商品
            params.put("productDailyInc", brandLibService.getKeywordsProductCnt(keywords));
            // 历史任务推送次数
            params.put("historyPushCnt", brandLibService.getPushTaskCnt(rule.getUiid(), null));
            // 今日新增 - 任务推送次数
            params.put("pushDailyInc", brandLibService.getPushTaskCnt(rule.getUiid(), DateTimeUtils.getDay(new Date())));
            // 下次推送时间
            params.put("nextPushTime", brandLibService.getNextPushTime(rule.getUiid()));
            // 上次推送时间
            params.put("lastPushTime", brandLibService.getLastPushTime(rule.getUiid()));

            brandLibEsService.update(rule.getUiid(), params);
        }
    }
}
