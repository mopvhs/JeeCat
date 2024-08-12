package com.jeesite.modules.cgcat;


import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.service.cg.third.DwzApiService;
import com.jeesite.modules.cat.service.cg.third.dto.DwzShortUrlDetail;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import com.jeesite.modules.cgcat.dto.CommandRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}")
public class ShortUrlController {


    @Resource
    private CacheService cacheService;

    @Resource
    private DwzApiService dwzApiService;

    /**
     * 口令信息替换
     * @param command
     * @return
     */
    @RequestMapping(value = "/api/dwz/short/url/get")
    public Result<DwzShortUrlDetail> exchangeCommand(@RequestBody CommandRequest command) {

        if (command == null || StringUtils.isBlank(command.getContent())) {
            return Result.ERROR(500, "参数不能为空");
        }

        // 获取短地址md5
        String key = Md5Utils.md5(command.getContent());
        String value = cacheService.get(key);
        if (StringUtils.isNotBlank(value)) {
            return JsonUtils.toReferenceType(value, new TypeReference<Result<DwzShortUrlDetail>>() {
            });
        }

        boolean longTerm = false;
        String type = command.getType();
        if (StringUtils.isNotBlank(type) && type.equals("longTerm")) {
            longTerm = true;
        }

        Result<DwzShortUrlDetail> result = dwzApiService.shortUrl(command.getContent(), longTerm);
        if (result.isSuccess() && result.getResult() != null) {
            cacheService.setWithExpireTime(key, JsonUtils.toJSONString(result), (int) TimeUnit.DAYS.toSeconds(7));
        }

        return result;
    }


}
