package com.jeesite.modules.cat.service.cg.third;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.service.FlameHttpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component
public class RpaWechatMessageRuleAdapter {

    @Value(value = "${rpa.domain}")
    private String domain;

    private static String wxId = "wxid_olwsi657cd2m22";

    public JSONObject getMsgRule(String wechatId) {
        // 判断token是否带固定前缀
        if (StringUtils.isBlank(wechatId)) {
            wechatId = wxId;
        }

//        String postUrl = "http://127.0.0.1:48080/app-api/api/member/auth/user/get";
        String postUrl = domain + "/app-api/tbl/api/message/rule";

        Map<String, String> data = new HashMap<>();
        data.put("wechatid", wechatId);
        String response = FlameHttpService.doPostWithHeaders(postUrl, JsonUtils.toJSONString(data), null);
        if (StringUtils.isBlank(response)) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject.getJSONObject("data") == null) {
            return null;
        }
        JSONObject res = jsonObject.getJSONObject("data");

        return res;
    }
}
