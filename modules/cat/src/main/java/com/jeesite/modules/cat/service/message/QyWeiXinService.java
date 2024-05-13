package com.jeesite.modules.cat.service.message;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.service.FlameHttpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信消息服务
 * url https://developer.work.weixin.qq.com/document/path/99110
 */
@Slf4j
@Component
public class QyWeiXinService {

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
        String doPost = FlameHttpService.doPost(url, JsonUtils.toJSONString(params));

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
            String doPost = FlameHttpService.doPost(url, JsonUtils.toJSONString(params));
            // 解析错误信息
            try {
                JSONObject res = JSONObject.parseObject(doPost);
                if (res != null) {
                    Object o = res.get("errcode");
                    if (o instanceof Integer) {
                        int code = (Integer) o;
                        if (code > 0) {
                            String errmsg = (String) res.get("errmsg");
                            return Result.ERROR(code, errmsg);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("发送图片失败 img:{}, webHook:{}", img, webHook, e);
                return Result.ERROR(500, "解析异常");
            }
            return Result.OK(doPost);
        } catch (Exception e) {
            log.error("发送图片失败 img:{}, webHook:{}", img, webHook, e);
        }
        return Result.ERROR(500, "发送失败");
    }

    public static void main(String[] args) {
        String img = "https://img.alicdn.com/bao/uploaded/i1/1879335580/O1CN01bxRJzY1r5guygOori_!!1879335580.jpg_500x500.jpg";
        String img2 = "https://img.alicdn.com/bao/uploaded/i1/1879335580/O1CN01bxRJzY1r5guygOori_!!1879335580.jpg_300x300.jpg";
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
