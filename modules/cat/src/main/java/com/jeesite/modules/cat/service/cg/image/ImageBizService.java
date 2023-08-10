package com.jeesite.modules.cat.service.cg.image;

import com.google.gson.JsonObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.service.FlameHttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ImageBizService {

    @Resource
    private FlameHttpService flameHttpService;

    public String getToken(String email, String password) {

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("email", email);
        jsonMap.put("password", password);


        // {\"code\":200,\"msg\":\"success\",\"data\":{\"token\":\"f985a48d5f828254ce2ff7225d0479cd\"},\"time\":1691507022}
        String doPost = flameHttpService.doPost("http://static.zhizher.com/api/token", JsonUtils.toJSONString(jsonMap));
        if (StringUtils.isBlank(doPost)) {
            return null;
        }

        return doPost;
    }

//    public String uploadImage(String token, File file) {
//
//        Map<String, Object> jsonMap = new HashMap<>();
//        jsonMap.put("token", token);
//        jsonMap.put("fileName", fileName);
//        jsonMap.put("fileContent", fileContent);
//
//        // {\"code\":200,\"msg\":\"success\",\"data\":{\"token\":\"f985a48d5f828254ce2ff7225d0479cd\"},\"time\":1691507022}
//        String doPost = flameHttpService.doUploadFilePost("http://static.zhizher.com/api/upload", JsonUtils.toJSONString(jsonMap));
//        if (StringUtils.isBlank(doPost)) {
//            return null;
//        }
//
//        return doPost;
//    }
}
