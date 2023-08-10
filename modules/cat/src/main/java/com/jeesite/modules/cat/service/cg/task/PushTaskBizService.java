package com.jeesite.modules.cat.service.cg.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.entity.QwChatroomInfoDO;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.enums.task.TaskSwitchEnum;
import com.jeesite.modules.cat.model.task.content.PushTaskContentDetail;
import com.jeesite.modules.cat.service.MaochePushTaskService;
import com.jeesite.modules.cat.service.MaocheTaskService;
import com.jeesite.modules.cat.service.QwChatroomInfoService;
import com.jeesite.modules.cat.service.message.QwService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jeesite.modules.cat.service.cg.CgUnionProductStatisticsService.UNIQUE_ID;

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

    public void push() {
        log.info("【推送】推送数据入队列成功");

        // 开启的，并且正常推送的任务
//        MaochePushTaskDO query = new MaochePushTaskDO();
//        query.setStatus(TaskStatusEnum.NORMAL.name());
//        query.setPublishDate_lte(new Date());


        // 1. 查询所有的任务
//        List<MaochePushTaskDO> list = maochePushTaskService.findList(query);
        List<MaochePushTaskDO> list = maochePushTaskService.listValidTask();
        log.info("【推送】查询到的任务列表数量：{}", list.size());

        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        List<String> taskIds = list.stream().map(MaochePushTaskDO::getTaskId).distinct().toList();
        MaocheTaskDO query = new MaocheTaskDO();
        query.setId_in(taskIds.toArray(new String[0]));
        List<MaocheTaskDO> tasks = maocheTaskService.findList(query);
        Map<String, MaocheTaskDO> taskMap = tasks.stream().collect(Collectors.toMap(MaocheTaskDO::getId, Function.identity(), (a, b) -> b));

        // 2. 遍历任务，推送
        for (MaochePushTaskDO taskDO : list) {
            MaocheTaskDO task = taskMap.get(taskDO.getTaskId());
            if (task == null) {
                continue;
            }
            if (!TaskStatusEnum.NORMAL.name().equals(task.getStatus()) || !TaskSwitchEnum.OPEN.name().equals(task.getTaskSwitch())) {
                continue;
            }

            // 获取需要推送的内容
            String content = taskDO.getContent();
            PushTaskContentDetail detail = JsonUtils.toReferenceType(content, new TypeReference<PushTaskContentDetail>() {
            });
            if (detail == null) {
                // 内容异常
                taskDO.setStatus(TaskStatusEnum.EXCEPTION.name());
                taskDO.setRemarks("推送内容异常，解析content失败");
                maochePushTaskService.update(taskDO);
                continue;
            }

            // 推送的文案
            String text = detail.getDetail();
            String img = detail.getImg();
            if (StringUtils.isBlank(text)) {
                log.error("推送内容异常，为空， push task id：{}", taskDO.getId());
                continue;
            }
            img = "https://www.baidu.com/img/flexible/logo/pc/result@2.png";
            // 先更新状态
            maochePushTaskService.updateStatus(new ArrayList<>(Collections.singleton(taskDO.getId())), TaskStatusEnum.PUSHING.name());
            taskDO.setStatus(TaskStatusEnum.PUSHING.name());

            // 推送
            send(text, img);

            // 推送完成，再更新状态
            maochePushTaskService.finishPushTask(taskDO);

            // 查询是否还有待发送的任务
            MaochePushTaskDO pushTaskQuery = new MaochePushTaskDO();
            pushTaskQuery.setTaskId(taskDO.getTaskId());
            pushTaskQuery.setStatus(TaskStatusEnum.NORMAL.name());
            List<MaochePushTaskDO> otherTasks = maochePushTaskService.findList(pushTaskQuery);
            if (CollectionUtils.isEmpty(otherTasks)) {
                // 没有了，更新任务状态
                maocheTaskService.finishTask(taskDO.getTaskId());
            }
        }

    }

    private void send(String content, String img) {
        if (StringUtils.isBlank(content)) {
            return;
        }

        // 获取所有的群
        List<QwChatroomInfoDO> qwChatroomInfoDOS = qwChatroomInfoService.listByOwnerId("1688856684429282");

        try {
            for (QwChatroomInfoDO qwChatroomInfoDO : qwChatroomInfoDOS) {
                if (!qwChatroomInfoDO.getId().equals("68")) {
                    continue;
                }
                String roomChatId = qwChatroomInfoDO.getRoomChatId();
                qwService.send(UNIQUE_ID, textMap(content, roomChatId));
                if (StringUtils.isNotBlank(img)) {
                    qwService.send(UNIQUE_ID, picMap(img, roomChatId));
                }
            }

        } catch (Exception e) {

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
    private Map<String, Object> picMap(String path, String rid) {
        Map<String, Object> msg = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("conversation_id", rid);
        data.put("path", path);
        data.put("request_key", "AAAAAA");

        msg.put("data", data);
        msg.put("type", 5003);

        return msg;
    }
}
