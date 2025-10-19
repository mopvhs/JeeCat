package com.jeesite.modules.cat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheRuleDao;
import com.jeesite.modules.cat.entity.MaocheRuleDO;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.AbstraUpOceanStage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OceanRuleService {

    @Resource
    private MaocheRuleDao maocheRuleDao;

    @Resource
    private CacheService cacheService;

    public Map<String, String> getKeywordReplacement() {
        MaocheRuleDO rule = getRule("keyword_replacement");
        if (rule == null || StringUtils.isBlank(rule.getContent())) {
            return new HashMap<>();
        }

        try {
            return JsonUtils.toReferenceType(rule.getContent(), new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            log.error("替换规则解析异常", e);
        }
        return new HashMap<>();
    }

    public List<String> getKeywordFailText() {
        MaocheRuleDO rule = getRule("keyword_fail_text");
        if (rule == null || StringUtils.isBlank(rule.getContent())) {
            return new ArrayList<>();
        }

        try {
            Map<String, String> map = JsonUtils.toReferenceType(rule.getContent(), new TypeReference<Map<String, String>>() {
            });

            if (MapUtils.isNotEmpty(map)) {
                return new ArrayList<>(map.keySet());
            }

        } catch (Exception e) {
            log.error("替换规则解析异常", e);
        }
        return new ArrayList<>();
    }

    public List<AbstraUpOceanStage.TextBO> getKeywordDelete() {
        String key = "keyword_delete";
        String val = cacheService.get(key);
        if (StringUtils.isNotBlank(val)) {
            return JsonUtils.toReferenceType(val, new TypeReference<List<AbstraUpOceanStage.TextBO>>() {
            });
        }

        MaocheRuleDO rule = getRuleDb("keyword_delete");
        if (rule == null || StringUtils.isBlank(rule.getContent())) {
            return new ArrayList<>();
        }

        try {
            String content = rule.getContent();

            Map<String, String> deletions = JsonUtils.toReferenceType(content, new TypeReference<Map<String, String>>() {
            });
            if (MapUtils.isEmpty(deletions)) {
                return new ArrayList<>();
            }

            List<AbstraUpOceanStage.TextBO> deletionTexts = new ArrayList<>();

            for (String item : deletions.keySet()) {
                AbstraUpOceanStage.TextBO textBO = new AbstraUpOceanStage.TextBO(item, item.length());
                deletionTexts.add(textBO);
            }

            // 排序，按大到小
            // 使用Collections.sort方法和自定义Comparator进行排序
            deletionTexts.sort(new Comparator<AbstraUpOceanStage.TextBO>() {
                @Override
                public int compare(AbstraUpOceanStage.TextBO o1, AbstraUpOceanStage.TextBO o2) {
                    // 按照size字段从大到小排序
                    return Integer.compare(o2.getSize(), o1.getSize());
                }
            });

            cacheService.setWithExpireTime(key, JsonUtils.toJSONString(deletionTexts), 600);

            return deletionTexts;
        } catch (Exception e) {
            log.error("替换规则解析异常", e);
        }
        return new ArrayList<>();
    }

    public MaocheRuleDO getRule(String key) {

        String val = cacheService.get(key);
        if (StringUtils.isNotBlank(val)) {
            return JsonUtils.toReferenceType(val, new TypeReference<MaocheRuleDO>() {
            });
        }

        MaocheRuleDO query = new MaocheRuleDO();
        query.setKeyName(key);
        MaocheRuleDO byEntity = maocheRuleDao.getByEntity(query);

        if (byEntity == null) {
            return null;
        }

        cacheService.setWithExpireTime(key, JsonUtils.toJSONString(byEntity), 600);

        return byEntity;
    }

    public MaocheRuleDO getRuleDb(String key) {

        MaocheRuleDO query = new MaocheRuleDO();
        query.setKeyName(key);
        MaocheRuleDO byEntity = maocheRuleDao.getByEntity(query);

        if (byEntity == null) {
            return null;
        }
        return byEntity;
    }
}
