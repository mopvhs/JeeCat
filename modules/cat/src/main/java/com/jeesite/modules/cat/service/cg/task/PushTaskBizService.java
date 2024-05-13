package com.jeesite.modules.cat.service.cg.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.enums.task.PushTypeEnum;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.enums.task.TaskSwitchEnum;
import com.jeesite.modules.cat.enums.task.TimeTypeEnum;
import com.jeesite.modules.cat.model.task.content.PushTaskContentDetail;
import com.jeesite.modules.cat.service.MaochePushTaskService;
import com.jeesite.modules.cat.service.MaocheTaskService;
import com.jeesite.modules.cat.service.QwChatroomInfoService;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.jeesite.modules.cat.service.message.QwService;
import com.jeesite.modules.cat.service.message.QyWeiXinService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PushTaskBizService {


    @Resource
    private MaocheTaskService maocheTaskService;

    @Resource
    private MaochePushTaskService maochePushTaskService;

    @Resource
    private QwChatroomInfoService qwChatroomInfoService;

    @Resource
    private QwService qwService;

    @Resource
    private QyWeiXinService qyWeiXinService;

    public void push() {
        log.info("【推送】推送数据入队列成功");

        // 1. 查询所有的任务
        List<MaochePushTaskDO> list = maochePushTaskService.listValidTask(50);
        log.info("【推送】查询到的任务列表数量：{}", list.size());

        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        List<String> taskIds = list.stream().map(MaochePushTaskDO::getTaskId).filter(Objects::nonNull).distinct().toList();
        List<MaocheTaskDO> tasks = maocheTaskService.listByIds(taskIds);
        Map<String, MaocheTaskDO> taskMap = tasks.stream().collect(Collectors.toMap(MaocheTaskDO::getId, Function.identity(), (a, b) -> b));

        // 2. 遍历任务，推送
        for (MaochePushTaskDO push : list) {
            MaocheTaskDO task = taskMap.get(push.getTaskId());
            if (task == null) {
                continue;
            }
            if (!TaskStatusEnum.NORMAL.name().equals(task.getStatus()) || !TaskSwitchEnum.OPEN.name().equals(task.getTaskSwitch())) {
                continue;
            }

            // 获取需要推送的内容
            String content = push.getContent();
            PushTaskContentDetail detail = JsonUtils.toReferenceType(content, new TypeReference<PushTaskContentDetail>() {
            });
            if (detail == null) {
                // 内容异常
                push.setStatus(TaskStatusEnum.EXCEPTION.name());
                push.setRemarks("推送内容异常，解析content失败");
                maochePushTaskService.update(push);
                continue;
            }

            // 推送的文案
            String text = detail.getDetail();
            String img = detail.getImg();
            if (StringUtils.isBlank(text) && StringUtils.isBlank(img)) {
                log.error("推送内容异常，为空， push task id：{}", push.getId());
                continue;
            }

            boolean oldPush = true;
            String subTitle = task.getSubTitle();
            if ("NEW-PUSH".equals(subTitle)) {
                oldPush = false;
            }

            // 加上类型
            String pushType = push.getPushType();
            PushTypeEnum pushTypeEnum = PushTypeEnum.getByName(pushType);
            if (pushTypeEnum != null && oldPush) {
                text = pushTypeEnum.getDesc() + "\n" + text;
            }

            // 先更新状态
            maochePushTaskService.updateStatus(new ArrayList<>(Collections.singleton(push.getId())), TaskStatusEnum.PUSHING.name());
            push.setStatus(TaskStatusEnum.PUSHING.name());

            // 推送
            send(text, img);

            // 推送完成，再更新状态
            maochePushTaskService.finishPushTask(push);

            // 查询是否还有待发送的任务
            MaochePushTaskDO pushTaskQuery = new MaochePushTaskDO();
            pushTaskQuery.setTaskId(push.getTaskId());
            pushTaskQuery.setStatus(TaskStatusEnum.NORMAL.name());
            List<MaochePushTaskDO> otherTasks = maochePushTaskService.findList(pushTaskQuery);
            if (CollectionUtils.isEmpty(otherTasks)) {
                // 如果是每日重复的任务，则复制生成一条新的任务，每日重复任务只能一个子任务
                if (TimeTypeEnum.REPEAT_DAILY_SCHEDULE.name().equals(task.getTimeType())) {
                    maochePushTaskService.updateStatus(Collections.singletonList(push.getId()), TaskStatusEnum.NORMAL.name(), DateUtils.addDays(push.getPublishDate(), 1));
                } else {
                    // 没有了，更新任务状态
                    maocheTaskService.finishTask(push.getTaskId());
                }
            }
        }

    }

    private void send(String content, String img) {
        if (StringUtils.isNotBlank(img)) {
            Result<String> imgRes = qyWeiXinService.sendImage(img, "d64fcde3-8555-4e05-8ce4-529b7c50a966");
            log.info("首次发送图片，img:{}, imgRes: {}",img, imgRes);
            if (!Result.isOK(imgRes)) {
                // 判断错误码，40009，表示图片过大，需要压缩
                if (imgRes.getCode() != null && imgRes.getCode() == 40009) {
                    img += "_500x500.jpg";
                    // 重新发送
                    imgRes = qyWeiXinService.sendImage(img, "d64fcde3-8555-4e05-8ce4-529b7c50a966");
                    log.info("二次发送图片，img:{}, imgRes: {}",img, imgRes);
                }
            }
            if (!Result.isOK(imgRes)) {
                DingDingService.sendDingDingMsg("图片发送失败，img: " + img + ", imgRes: " + imgRes);
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {

            }
        }
        if (StringUtils.isNotBlank(content)) {
            log.info("text: {}", content);
            Result<String> textRes = qyWeiXinService.sendText(content, "d64fcde3-8555-4e05-8ce4-529b7c50a966");
        }

    }

    private Map<String, Object> textMap(String content, String rid) {
        Map<String, Object> msg = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("conversation_id", rid);
        data.put("content", content);
        data.put("request_key", "AAAAAA");

        msg.put("data", data);
        msg.put("type", 5000);

        return msg;
    }
    private Map<String, Object> picMap(String img, String rid) {
        Map<String, Object> msg = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("conversation_id", rid);
        data.put("request_key", "AAAAAA");

        msg.put("data", data);
        msg.put("type", 5003);
        msg.put("url", img);

        return msg;
    }
}
