package com.jeesite.modules.cat.service.message;

import com.ctc.wstx.shaded.msv_core.util.Uri;
import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.service.FlameHttpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信消息服务
 */
@Slf4j
@Component
public class QyWeiXinService {

    @Resource
    private FlameHttpService flameHttpService;

    private static final String URL_FORMAT = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=%s";

    public Result<String> sendText(String text, String webHook) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(webHook)) {
            return Result.ERROR(500, "参数不能为空");
        }
        // {
        //    "msgtype": "text",
        //    "text": {
        //        "content": "广州今日天气：29度，大部分多云，降雨概率：60%",
        //		"mentioned_list":["wangqing","@all"],
        //		"mentioned_mobile_list":["13800001111","@all"]
        //    }
        //}

        Map<String, Object> textMap = new HashMap<>();
        textMap.put("content", text);


        String url = String.format(URL_FORMAT, webHook);

        Map<String, Object> params = new HashMap<>();
        params.put("msgtype", "text");
        params.put("text", textMap);
        String doPost = flameHttpService.doPost(url, JsonUtils.toJSONString(params));

        return Result.OK(doPost);
    }

    public Result<String> sendImage(String img, String webHook) {
        if (StringUtils.isBlank(img) || StringUtils.isBlank(webHook)) {
            return Result.ERROR(500, "参数不能为空");
        }
        // {
        //    "msgtype": "image",
        //    "image": {
        //        "base64": "DATA",
        //		"md5": "MD5"
        //    }
        //}
        try {
            URL imgUrl = new URL(img);
            InputStream inputStream = imgUrl.openStream();
            byte[] imageBytes = inputStream.readAllBytes();
            Map<String, Object> imageMap = new HashMap<>();
            String base64String = Base64.getEncoder().encodeToString(imageBytes);

            imageMap.put("base64", base64String);
            imageMap.put("md5", Md5Utils.md5Image(img));

            String url = String.format(URL_FORMAT, webHook);

            Map<String, Object> params = new HashMap<>();
            params.put("msgtype", "image");
            params.put("image", imageMap);
            String doPost = flameHttpService.doPost(url, JsonUtils.toJSONString(params));
            return Result.OK(doPost);
        } catch (Exception e) {
            log.error("发送图片失败 img:{}, webHook:{}", img, webHook, e);
        }
        return Result.ERROR(500, "发送失败");
    }

    public static void main(String[] args) {
        String img = "https://img.alicdn.com/bao/uploaded/i3/2128031955/O1CN01j46Cx81QJRAr5qh8Y_!!0-item_pic.jpg";
        try {
            URL imgUrl = new URL(img);
            InputStream inputStream = imgUrl.openStream();
            byte[] imageBytes = inputStream.readAllBytes();

            byte[] bytes = Md5Utils.md5(imageBytes);
            String result = "";
            for (int i = 0; i < bytes.length; i++) {
                String tmp = Integer.toHexString(bytes[i] & 0xFF);
                if (tmp.length() == 1) {
                    result = "0" + tmp;
                } else {
                    result += tmp;
                }
            }

            System.out.println(result);

            File file = null;
            try {
                String key = "1.png";
                String tempPart = "/tmp/" + key;
                URL url = new URL(img);
                file = new File(tempPart);
                FileUtils.copyInputStreamToFile(url.openStream(), file);
                System.out.println(Md5Utils.md5File(file));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (file != null) {
                    file.delete();
                }
            }

        } catch (Exception e) {
        }
    }
}
