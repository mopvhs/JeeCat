package com.jeesite.modules.cat.service.cg.third;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.cg.third.dto.DwzShortUrlDetail;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 维易接口
 */
@Slf4j
@Component
public class DwzApiService {

    private static final String TOKEN = "95d7c4470b423823be88c5897c147463";



    /**
     * https://dwz.cn/console/apidoc/v3
     * @param url
     * @return
     */
    public Result<DwzShortUrlDetail> shortUrl(String url, boolean longTerm) {
        // 判空
        if (StringUtils.isBlank(url)) {
            return Result.ERROR(400, "veKey不能为空");
        }

        String termOfValidity = "1-year";
        if (longTerm) {
            termOfValidity = "long-term";
        }

        String postUrl = "https://dwz.cn/api/v3/short-urls";
        List<Map<String, Object>> params = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        data.put("LongUrl", url);
        data.put("TermOfValidity", termOfValidity);
        params.add(data);

        Map<String, String> headers = new HashMap<>();
        headers.put("Dwz-Token", TOKEN);
        headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put("Content-Language", "zh");


        try {
            String response = FlameHttpService.doPostWithHeaders(postUrl, JsonUtils.toJSONString(params), headers);
            if (StringUtils.isBlank(response)) {
                return Result.ERROR(500, "请求失败，数据为空");
            }
            JSONObject jsonObject = JSONObject.parseObject(response);
            if (jsonObject == null) {
                return Result.ERROR(500, "请求解析失败，数据为空");
            }

            Integer code = Optional.ofNullable(jsonObject.getInteger("Code")).orElse(-1);
            String msg = jsonObject.getString("ErrMsg");
            if (code.equals(0)) {

                JSONArray jsonArray = jsonObject.getJSONArray("ShortUrls");
                DwzShortUrlDetail urlDetail = null;
                if (jsonArray != null) {
                    urlDetail = JSONObject.parseObject(jsonArray.getString(0), DwzShortUrlDetail.class);
                }

                return Result.OK(urlDetail);
            }

            return Result.ERROR(code, msg);
        } catch (Exception e) {
            log.error("请求短链接口异常", e);
        }

        return Result.OK(null);
    }
}
