package com.jeesite.modules.cat.service.cg.third;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.service.FlameHttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WechatRobotAdapter {

    @Value(value = "${jubo.domain}")
    private String domain;

    public JSONObject getMessageDetail(String wechatId, String msgSvrId) {
        if (StringUtils.isBlank(wechatId) || StringUtils.isBlank(msgSvrId)) {
            return null;
        }

        String url = domain + "/api/app/message/detail/get";

        Map<String, Object> data = new HashMap<>();
        data.put("wechatId", wechatId);
        data.put("msgSvrId", msgSvrId);

        Map<String, String> headers = new HashMap<>();
        headers.put("token", "33DD94BBF49356583E460D1FA2907EDB");
        try {

            String response = FlameHttpService.doPostWithHeaders(url, JsonUtils.toJSONString(data), headers);
            log.info("robot msg request {}, response {}", JsonUtils.toJSONString(data), response);
            if (StringUtils.isBlank(response)) {
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(response);
            if (jsonObject == null || jsonObject.getInteger("code") == null) {
                return null;
            }
            Integer code = jsonObject.getInteger("code");
            Object detail = jsonObject.get("data");
            if (code != 0 || detail == null) {
                return null;
            }

            return jsonObject.getJSONObject("data");
        } catch (Exception e) {
            log.error("robot msg exception request {}", JsonUtils.toJSONString(data), e);
        }
        return null;
    }

    /**
     * 获取图片原图（大图）指令提交
     * @param wechatId
     * @param msgSvrId
     */
    public void getImageDetail(String wechatId, String convId, String msgSvrId) {
        if (StringUtils.isBlank(wechatId) || StringUtils.isBlank(convId) || StringUtils.isBlank(msgSvrId)) {
            return;
        }

        String url = domain + "/api/app/image/detail";

        Map<String, Object> data = new HashMap<>();
        data.put("wechatId", wechatId);
        data.put("msgSrvId", msgSvrId);
        data.put("friendId", convId);
        Map<String, String> headers = new HashMap<>();
        headers.put("token", "33DD94BBF49356583E460D1FA2907EDB");
        try {
            String response = FlameHttpService.doPostWithHeaders(url, JsonUtils.toJSONString(data), headers);
            log.info("getImageDetail msg request {}, response {}", JsonUtils.toJSONString(data), response);
        } catch (Exception e) {
            log.error("getImageDetail msg exception request {}", JsonUtils.toJSONString(data), e);
        }
    }
}
