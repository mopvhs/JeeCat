package com.jeesite.modules.cat.service.message;

import com.google.gson.JsonObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.service.FlameHttpService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DingDingService {

    // 群名：猫车索引
    private static final String WEB_HOOK = "https://oapi.dingtalk.com/robot/send?access_token=5ea41e511ba998b6c2035209df2431193abf48c1003e6635a0aaea4e5256b3d6";

    // 猫车告警
    private static final String WEB_HOOK_CAT = "https://oapi.dingtalk.com/robot/send?access_token=faabc57115e2b8b26a1472ce9e428b0dbd41ed1fb8efc9f1beae8cc143579ab6";

    public static void sendDingDingMsg(String msg) {
        String hook = getHook(null);
        doSend(msg, hook);
    }

    public static void sendParseDingDingMsg(String msg, Object... varArr) {
        // 占位符
        msg = parse1(msg, varArr);
        String hook = getHook(null);
        doSend(msg, hook);
    }

    public static void sendParseDingDingMsg(String msg, Integer sceneType, Object... varArr) {
        // 占位符
        msg = parse1(msg, varArr);
        String hook = getHook(sceneType);
        doSend(msg, hook);
    }


    public static void sendDingDingMsg(String msg, String webHook) {
        if (StringUtils.isBlank(webHook)) {
            sendDingDingMsg(msg);
        } else {
            doSend(msg, webHook);
        }
    }

    public static void sendDingDingMsg(String msg, Integer sceneType) {
        String hook = getHook(sceneType);
        doSend(msg, hook);
    }

    private static void doSend(String msg, String hook) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("msgtype", "text");

        JsonObject msgJson = new JsonObject();
        msgJson.addProperty("content", msg + ".");
        jsonObject.addProperty("text", msgJson.toString());

        try {
//            String post = HttpClientUtils.post(hook, JsonUtils.toReferenceType(jsonObject.toString(), new TypeReference<Map<String, String>>() {
//            }));
            String post = FlameHttpService.doPost(hook, jsonObject.toString());

        } catch (Exception e) {
            log.error("发送钉钉消息失败 ", e);
        }
    }

    private static String getHook(Integer sceneType) {
        if (sceneType == null) {
            return WEB_HOOK;
        }
        if (sceneType == 1) {
            return WEB_HOOK_CAT;
        }

        return WEB_HOOK;
    }

    /**
     * 将字符串text中由openToken和closeToken组成的占位符依次替换为args数组中的值
     * @param openToken
     * @param closeToken
     * @param text
     * @param args
     * @return
     */
    public static String parse(String openToken, String closeToken, String text, Object... args) {
        if (args == null || args.length <= 0) {
            return text;
        }
        int argsIndex = 0;
        if (text == null || text.isEmpty()) {
            return "";
        }
        char[] src = text.toCharArray();
        int offset = 0;
        // search open token
        int start = text.indexOf(openToken, offset);
        if (start == -1) {
            return text;
        }
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        while (start > -1) {
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // found open token. let's search close token.
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        offset = end + closeToken.length();
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found.
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {

                    String value = (argsIndex <= args.length - 1) ?
                            (args[argsIndex] == null ? "" : args[argsIndex].toString()) : expression.toString();
                    builder.append(value);
                    offset = end + closeToken.length();
                    argsIndex++;

                }
            }
            start = text.indexOf(openToken, offset);
        }
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }
    public static String parse0(String text, Object... args) {
        return DingDingService.parse("${", "}", text, args);
    }
    public static String parse1(String text, Object... args) {
        return DingDingService.parse("{", "}", text, args);
    }
}
