package com.jeesite.modules.robot;

import com.jeesite.common.lang.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SSEUtils {

    // 设置超时
    private static final long SSE_TIMEOUT = 60 * 5 * 1000L;

    private static final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();


    public static SseEmitter addSub(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        SseEmitter sseEmitter = sseEmitters.get(id);

        if (sseEmitter == null) {
            sseEmitter = new SseEmitter(SSE_TIMEOUT);

            sseEmitter.onTimeout(() -> {
                log.info("SseEmitter timeout, id:{}", id);
                sseEmitters.remove(id);
            });

            sseEmitter.onCompletion(() -> {
                log.info("SseEmitter complete, id:{}", id);
                sseEmitters.remove(id);
            });

            sseEmitters.put(id, sseEmitter);
        }

        return sseEmitter;
    }

    public static void pushMsg(String id, String msg) throws Exception {

        try {
            SseEmitter.SseEventBuilder builder = SseEmitter.event().data(msg);
            sseEmitters.get(id).send(builder.build());
        }  catch (Exception e) {
            log.error("push msg error", e);
        }
    }
}
