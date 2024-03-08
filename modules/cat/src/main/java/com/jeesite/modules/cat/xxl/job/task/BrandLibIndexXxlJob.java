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
 * 品牌库索引全量索引
 */
@Slf4j
@Component
public class BrandLibIndexXxlJob extends IJobHandler {

    @Resource
    private MaochePushTaskRuleService maochePushTaskRuleService;

    @Resource
    private BrandLibEsService brandLibEsService;

    @Resource
    private BrandLibService brandLibService;

    @Resource
    private TaskEsService taskEsService;

    @Override
    @XxlJob("brandLibIndexXxlJob")
    public void execute() throws Exception {
        // 获取所有的品牌库
        List<MaochePushTaskRuleDO> rules = maochePushTaskRuleService.getAllBrandLib();
        log.info("BrandLibIndexXxlJob rules size: {}", rules.size());
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }

        for (MaochePushTaskRuleDO rule : rules) {
            brandLibEsService.doIndexEs(rule);
        }
    }
}
