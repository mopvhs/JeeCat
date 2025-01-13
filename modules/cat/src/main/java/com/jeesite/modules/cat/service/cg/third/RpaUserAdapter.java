package com.jeesite.modules.cat.service.cg.third;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.service.FlameHttpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component
public class RpaUserAdapter {

    @Value(value = "${rpa.domain}")
    private String domain;

    public String getToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        return request.getHeader("Authorization");
    }

    public JSONObject getUser(String token) {
        // 判断token是否带固定前缀
        if (StringUtils.isBlank(token) || !token.contains("Bearer ")) {
            return null;
        }

//        String postUrl = "http://127.0.0.1:48080/app-api/api/member/auth/user/get";
        String postUrl = domain + "/app-api/api/member/auth/user/get";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", token);
        String response = FlameHttpService.doPostWithHeaders(postUrl, "{}", headers);
        if (StringUtils.isBlank(response)) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject.getJSONObject("result") == null) {
            return null;
        }
        JSONObject res = jsonObject.getJSONObject("result");
        JSONObject user = res.getJSONObject("result");
        if (user == null) {
            return null;
        }

        return user;
    }
}
