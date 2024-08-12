package com.jeesite.modules.robot;


import com.jeesite.common.lang.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class SseTestController {

    @RequestMapping(value = "sse/test/api")
    public SseEmitter test(String id, HttpServletResponse response) {
        // 添加订阅，建立sse连接
        SseEmitter emitter = SSEUtils.addSub(id);

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    SSEUtils.pushMsg(id, "msg" + i + "\n");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.info("push msg error", e);
                }
            }
        }).start();



        return emitter;
    }

    @RequestMapping(value = "sse/test/question")
    public SseEmitter question(String id, String question, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        SseEmitter emitter = SSEUtils.addSub(id);

        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    SSEUtils.pushMsg(id, "question: answer：" + i + "\n");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.info("push msg error", e);
                }
            }
        }).start();

        return emitter;
    }


    @RequestMapping(value = "sse/test/api2")
    public SseEmitter test2(String id, String question, HttpServletResponse response) {
        // 添加订阅，建立sse连接
        SseEmitter emitter = SSEUtils.addSub(id);

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        if (StringUtils.isNotBlank(question)) {
            for (int i = 0; i < 10; i++) {
                try {
                    SSEUtils.pushMsg(id, "msg" + i + "\n");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.info("push msg error", e);
                }
            }
        }


        return emitter;
    }
}
