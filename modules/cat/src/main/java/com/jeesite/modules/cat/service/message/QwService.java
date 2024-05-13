package com.jeesite.modules.cat.service.message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.dao.MaocheChatroomInfoDao;
import com.jeesite.modules.cat.entity.QwConfigInfoDO;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.QwConfigInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class QwService {

    @Resource
    private QwConfigInfoService qwConfigInfoService;

    @Resource
    private MaocheChatroomInfoDao maocheChatroomInfoDao;

//    @Resource
//    private FlameHttpService flameHttpService;

    public boolean send(String uniqueId, Map<String, Object> msg) {
        if (StringUtils.isBlank(uniqueId) || MapUtils.isEmpty(msg)) {
            return false;
        }
        QwConfigInfoDO infoDO = qwConfigInfoService.getByUniqueId(uniqueId);
        if (infoDO == null) {
            return false;
        }
        String clientAccountInfo = infoDO.getClientAccountInfo();
        JSONArray array = JSONObject.parseArray(clientAccountInfo);
        JSONObject jsonObject = array.getJSONObject(0);
        Long clientId = jsonObject.getLong("client_id");

        msg.put("client_id", clientId);

        String url = "http://106.52.152.159:9999/send_cmd";

        Map<String, String> data = new HashMap<>();
        data.put("unique_id", uniqueId);
        data.put("cmd_json", JsonUtils.toJSONString(msg));

        try {
            String doPost = FlameHttpService.doFormPost(url, data);
            log.info("QwService send data: {}, result: {}", JsonUtils.toJSONString(data), doPost);
        } catch (Exception e) {
            log.error("send_cmd exception ", e);
        }

        return true;
    }
}
