package com.jeesite.modules.cat.service.cg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.lang.TimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CgUserRcmdService {

    @Resource
    private CacheService cacheService;

    private static final String KEY_SUFFIX = "history_keyword_";

    public void setHistoryKeyword(String openId, String keyword) {
        if (StringUtils.isBlank(openId)) {
            return;
        }
        keyword = StringUtils.trim(keyword);
        if (StringUtils.isBlank(keyword)) {
            return;
        }
        String key = KEY_SUFFIX + openId;

        List<String> keywords = getHistoryKeywords(openId);
        if (keywords.size() >= 10) {
            keywords.set(0, keyword);
        } else {
            if (keywords.contains(keyword)) {
                return;
            }
            keywords.add(0, keyword);
        }

        cacheService.set(key, JsonUtils.toJSONString(keywords));
        cacheService.expire(key, (int) TimeUnit.DAYS.toSeconds(30));
    }

    public List<String> getHistoryKeywords(String openId) {
        if (StringUtils.isBlank(openId)) {
            return new ArrayList<>();
        }

        String key = KEY_SUFFIX + openId;

        String value = cacheService.get(key);

        if (StringUtils.isBlank(value)) {
            return new ArrayList<>();
        }

        List<String> list = Optional.ofNullable(JsonUtils.toReferenceType(value, new TypeReference<List<String>>() {
        })).orElse(new ArrayList<>());

        return list;
    }
}
