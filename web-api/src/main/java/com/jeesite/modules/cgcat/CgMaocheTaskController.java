package com.jeesite.modules.cgcat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheTaskDao;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.enums.task.TaskSwitchEnum;
import com.jeesite.modules.cat.enums.task.TimeTypeEnum;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.model.task.content.PushTaskContent;
import com.jeesite.modules.cat.model.task.content.PushTaskContentDetail;
import com.jeesite.modules.cat.model.task.dto.TaskRequest;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaochePushTaskService;
import com.jeesite.modules.cat.service.MaocheTaskService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.task.PushTaskBizService;
import com.jeesite.modules.cat.service.cg.task.PushTaskCreateService;
import com.jeesite.modules.cat.service.cg.task.TaskSearchBizService;
import com.jeesite.modules.cgcat.dto.PushTaskDetail;
import com.jeesite.modules.cgcat.dto.PushTaskEditRequest;
import com.jeesite.modules.cgcat.dto.PushTaskResponse;
import com.jeesite.modules.cgcat.dto.TaskDTO;
import com.jeesite.modules.cgcat.dto.TaskDetail;
import com.jeesite.modules.cgcat.dto.TaskEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
//@RequestMapping(value = "${adminPath}/backend/api/")
// todo
@RequestMapping(value = "${adminPath}/api")
public class CgMaocheTaskController {

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaochePushTaskService maochePushTaskService;

    @Resource
    private MaocheTaskService maocheTaskService;

    @Resource
    private PushTaskCreateService pushTaskCreateService;

    @Resource
    private TaskSearchBizService taskSearchBizService;

    @Resource
    private MaocheTaskDao maocheTaskDao;

    @RequestMapping(value = "/push/task/list")
    @ResponseBody
    public Page<TaskDTO> taskList(HttpServletRequest request, HttpServletResponse response) {

        Page<TaskDTO> page = new Page<>(request, response);

//        int from = (page.getPageNo() - 1) * page.getPageSize();

        int total = maocheTaskService.getTotal();
        int size = page.getPageSize();

        List<MaocheTaskDO> servicePage = maocheTaskService.getPage(page.getPageNo(), size);

        if (CollectionUtils.isEmpty(servicePage)) {
            return page;
        }

        List<String> taskIds = servicePage.stream().map(MaocheTaskDO::getId).toList();
        // 查询推送任务
        List<MaochePushTaskDO> pushTaskDOS = Optional.ofNullable(maochePushTaskService.getByTaskIds(taskIds)).orElse(new ArrayList<>());
        // 分桶
        Map<String, List<MaochePushTaskDO>> taskMap = pushTaskDOS.stream().collect(Collectors.groupingBy(MaochePushTaskDO::getTaskId));

        List<TaskDTO> taskDTOs = convert(servicePage);
        for (TaskDTO dto : taskDTOs) {
            int finishedNum = 0;    // 完成数量
            int pushNum = 0;    // 推送数量
            String status = dto.getStatus();
            List<MaochePushTaskDO> pushTasks = taskMap.get(dto.getId());
            if (CollectionUtils.isNotEmpty(pushTasks)) {
                finishedNum = (int) pushTasks.stream().filter(pushTaskDO -> TaskStatusEnum.FINISHED.name().equals(pushTaskDO.getStatus())).count();
                pushNum = (int) pushTasks.stream().filter(pushTaskDO -> !TaskStatusEnum.DELETE.name().equals(pushTaskDO.getStatus())).count();
                if (finishedNum == 0) {
                    status = TaskStatusEnum.WAITING.name();
                } else if (finishedNum < pushNum) {
                    status = TaskStatusEnum.PUSHING.name();
                } else if (finishedNum == pushNum) {
                    status = TaskStatusEnum.FINISHED.name();
                }
                if (pushTasks.size() == 1) {
                    MaochePushTaskDO pushTaskDO = pushTasks.get(0);
                    if (pushTaskDO.getStatus().equals(TaskStatusEnum.NORMAL.name())) {
                        dto.setTitle(dto.getTitle() + "\n" + DateTimeUtils.getStringDate(pushTaskDO.getPublishDate()));
                    }
                }
            }
            if (!TaskSwitchEnum.OPEN.name().equals(dto.getTaskSwitch())) {
                status = TaskStatusEnum.INIT.name();
            }
            dto.setStatus(status);
            dto.setFinishedNum(finishedNum);
            dto.setPushNum(pushNum);
        }

        Page<TaskDTO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), total, taskDTOs);

        return toPage;
    }

    public static List<TaskDTO> convert(List<MaocheTaskDO> taskDOS) {
        if (CollectionUtils.isEmpty(taskDOS)) {
            return Collections.emptyList();
        }

        List<TaskDTO> taskDTOS = new ArrayList<>(taskDOS.size());
        for (MaocheTaskDO taskDO : taskDOS) {
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setId(taskDO.getId());
            taskDTO.setTitle(taskDO.getTitle());
            taskDTO.setSubTitle(taskDO.getSubTitle());
            taskDTO.setTaskType(taskDO.getTaskType());
            taskDTO.setStatus(taskDO.getStatus());
            taskDTO.setTaskSwitch(taskDO.getTaskSwitch());
            taskDTO.setTimeType(taskDO.getTimeType());
            taskDTO.setPublishDate(taskDO.getPublishDate());
            taskDTOS.add(taskDTO);
        }
        return taskDTOS;
    }

    // 推送任务创建
    @RequestMapping(value = "/push/task/create")
    @ResponseBody
    public Result<?> createTask(TaskRequest request) {

        if (request == null) {
            return Result.ERROR(500, "参数错误。");
        }

        return pushTaskCreateService.createTask(request);
    }

    // 获取任务详情
    @RequestMapping(value = "/push/task/detail/get")
    @ResponseBody
    public Result<?> taskDetail(TaskRequest request) {
        if (request == null || StringUtils.isBlank(request.getTaskId())) {
            return Result.ERROR(404, "参数错误");
        }

        // 获取详情
        MaochePushTaskDO query = new MaochePushTaskDO();
        query.setTaskId(request.getTaskId());
        List<MaochePushTaskDO> pushTasks = maochePushTaskService.queryList(query);
        if (CollectionUtils.isEmpty(pushTasks)) {
            return Result.ERROR(404, "推送任务不存在");
        }

        // 任务
        MaocheTaskDO task = maocheTaskService.get(request.getTaskId());
        if (task == null) {
            return Result.ERROR(404, "任务不存在");
        }

        List<UnionProductTO> productTOs = Optional.ofNullable(taskSearchBizService.getPushTaskProducts(pushTasks)).orElse(new ArrayList<>());
        Map<Long, UnionProductTO> productTOMap = productTOs.stream().collect(Collectors.toMap(UnionProductTO::getId, Function.identity(), (k1, k2) -> k1));

        String content = task.getContent();
        PushTaskContent taskContent = JsonUtils.toReferenceType(content, new TypeReference<PushTaskContent>() {
        });
        List<PushTaskDetail> pushTaskDetails = new ArrayList<>();
        if (taskContent != null && CollectionUtils.isNotEmpty(taskContent.getIds())) {
            Map<String, List<MaochePushTaskDO>> listMap = pushTasks.stream().collect(Collectors.groupingBy(MaochePushTaskDO::getResourceId));
            for (Long id : taskContent.getIds()) {
                List<MaochePushTaskDO> maochePushTaskDOS = listMap.get(String.valueOf(id));
                if (CollectionUtils.isEmpty(maochePushTaskDOS)) {
                    continue;
                }
                for (MaochePushTaskDO push : maochePushTaskDOS) {
                    long productId = NumberUtils.toLong(push.getResourceId());
                    UnionProductTO productTO = productTOMap.get(productId);

                    if (TaskStatusEnum.DELETE.name().equals(push.getStatus())) {
                        continue;
                    }
                    PushTaskDetail detail = PushTaskDetail.convert(push);
                    detail.setProduct(productTO);
                    pushTaskDetails.add(detail);
                }
            }
        } else {
            for (MaochePushTaskDO push : pushTasks) {
                if (TaskStatusEnum.DELETE.name().equals(push.getStatus())) {
                    continue;
                }
                PushTaskDetail detail = PushTaskDetail.convert(push);
                pushTaskDetails.add(detail);
            }
        }

        TaskDetail taskDetail = new TaskDetail();
        taskDetail.setTitle(task.getTitle());
        taskDetail.setSubTitle(task.getSubTitle());
        taskDetail.setTimeType(task.getTimeType());
        taskDetail.setPublishDate(task.getPublishDate());

        PushTaskResponse response = new PushTaskResponse();
        response.setTaskId(request.getTaskId());
        response.setTask(taskDetail);
        response.setDetails(pushTaskDetails);

        return Result.OK(response);
    }


    // 获取任务详情
    @RequestMapping(value = "/task/edit")
    @ResponseBody
    public Result<?> editTask(TaskEditRequest request) {
        if (request == null || StringUtils.isBlank(request.getId())) {
            return Result.ERROR(404, "参数错误");
        }

        // 获取详情
        MaocheTaskDO query = new MaocheTaskDO();
        query.setId(request.getId());
        MaocheTaskDO task = maocheTaskService.get(query);
        if (task == null) {
            return Result.ERROR(404, "任务不存在");
        }
        // 获取时间
        if (TaskSwitchEnum.OPEN.name().equals(task.getTaskSwitch())) {
            return Result.ERROR(500, "已经启用的任务不允许编辑");
        }

        // 修改推送类型的时候，需要校验是否是改变为每日重复任务
        if (!task.getTimeType().equals(request.getTimeType())) {
            // 如果任务本身是每日重复任务，不允许修改
            if (TimeTypeEnum.REPEAT_DAILY_SCHEDULE.name().equals(task.getTimeType())) {
                return Result.ERROR(500, "【每日重复任务】不允许修改任务类型");
            }
            if (request.getTimeType().equals(TimeTypeEnum.REPEAT_DAILY_SCHEDULE.name())) {
                return Result.ERROR(500, "不允许修改任务类型为【每日重复任务】类型");
            }
        }

        task.setTitle(request.getTitle());
        task.setTimeType(request.getTimeType());
        task.setSubTitle(request.getSubTitle());
        task.setPublishDate(request.getPublishDate());

        maocheTaskService.update(task);

        return Result.OK("更新成功");
    }


    // 获取任务详情
    @RequestMapping(value = "/push/task/edit")
    @ResponseBody
    public Result<?> editDetail(PushTaskEditRequest request) {
        if (request == null || StringUtils.isBlank(request.getId())) {
            return Result.ERROR(404, "参数错误");
        }

        // 获取详情
        MaochePushTaskDO query = new MaochePushTaskDO();
        query.setId(request.getId());
        MaochePushTaskDO pushTask = maochePushTaskService.get(query);
        if (pushTask == null) {
            return Result.ERROR(404, "推送任务不存在，查询不到具体的推送任务");
        }

        String content = pushTask.getContent();
        PushTaskContentDetail taskContent = null;
        if (StringUtils.isBlank(content)) {
            taskContent = new PushTaskContentDetail();
        } else {
            taskContent = JsonUtils.toReferenceType(content, new TypeReference<PushTaskContentDetail>() {
            });
        }

        taskContent.setDetail(request.getDetail());
        taskContent.setImg(request.getImg());

        pushTask.setPushType(request.getPushType());
        pushTask.setContent(JsonUtils.toJSONString(taskContent));

        maochePushTaskService.update(pushTask);

        return Result.OK("更新成功");
    }



    // 获取任务详情
    @RequestMapping(value = "/push/task/delete")
    @ResponseBody
    public Result<?> deleteDetail(PushTaskEditRequest request) {
        if (request == null || StringUtils.isBlank(request.getId())) {
            return Result.ERROR(404, "参数错误");
        }

        // 获取详情
        MaochePushTaskDO query = new MaochePushTaskDO();
        query.setId(request.getId());
        MaochePushTaskDO pushTask = maochePushTaskService.get(query);
        if (pushTask == null) {
            return Result.ERROR(404, "推送任务不存在");
        }

        maochePushTaskService.updateStatus(pushTask.getId(), TaskStatusEnum.DELETE);

        return Result.OK("更新成功");
    }

    // 任务启用
    @RequestMapping(value = "/task/switch")
    @ResponseBody
    public Result<?> enableTask(TaskEditRequest request) {
        if (request == null || StringUtils.isBlank(request.getId())) {
            return Result.ERROR(404, "参数错误");
        }

        TaskSwitchEnum switchEnum = TaskSwitchEnum.getEnum(request.getTaskSwitch());
        if (switchEnum == null) {
            return Result.ERROR(404, "参数错误");
        }

        // 获取详情
        MaocheTaskDO query = new MaocheTaskDO();
        query.setId(request.getId());
        MaocheTaskDO task = maocheTaskService.get(query);
        if (task == null) {
            return Result.ERROR(404, "任务不存在");
        }

        // 获取推送任务
        MaochePushTaskDO pushTaskQuery = new MaochePushTaskDO();
        pushTaskQuery.setTaskId(request.getId());

        List<MaochePushTaskDO> pushtaskList = maochePushTaskService.queryList(pushTaskQuery);
        if (CollectionUtils.isEmpty(pushtaskList)) {
            return Result.ERROR(404, "推送任务不存在");
        }

        // 过滤已经删除的任务和已经完成的任务
        List<MaochePushTaskDO> taskDOS = pushtaskList.stream()
                .filter(i -> !TaskStatusEnum.DELETE.name().equals(i.getStatus()) && !TaskStatusEnum.FINISHED.name().equals(i.getStatus()))
                .toList();

        if (CollectionUtils.isEmpty(taskDOS)) {
            return Result.ERROR(404, "有效推送任务不存在");
        }

        // 如果是关闭的话，直接关闭
        if (TaskSwitchEnum.CLOSE == switchEnum) {
            maocheTaskService.updateStatusSwitch(task.getId(), TaskStatusEnum.STOP, TaskSwitchEnum.CLOSE);
            // 把所有的都改成停止
            List<String> pushIds = taskDOS.stream().map(MaochePushTaskDO::getId).toList();
            maochePushTaskService.updateStatus(pushIds, TaskStatusEnum.STOP.name());

            return Result.OK("更新成功");
        }

        String content = task.getContent();
        PushTaskContent taskContent = JsonUtils.toReferenceType(content, new TypeReference<PushTaskContent>() {
        });
        // 如果是开启的话，把所有初始状态的都改成正常，并且设置发布时间
        // 根据不同的推送类型，设置不同的发布时间
        if (task.getTimeType().equals(TimeTypeEnum.DAILY_SCHEDULE.name())) {
            // 获取发布时间
            Date publishDate = task.getPublishDate();
            // 获取到时 分
            Date time = DateTimeUtils.getTodyDate(publishDate);
            // 从第二天开始
            time = new Date(time.getTime() + 86400000L);

            if (taskContent != null && CollectionUtils.isNotEmpty(taskContent.getIds())) {
                Map<String, List<MaochePushTaskDO>> listMap = taskDOS.stream().collect(Collectors.groupingBy(MaochePushTaskDO::getResourceId));
                for (Long id : taskContent.getIds()) {
                    List<MaochePushTaskDO> maochePushTaskDOS = listMap.get(String.valueOf(id));
                    if (CollectionUtils.isEmpty(maochePushTaskDOS)) {
                        continue;
                    }
                    for (MaochePushTaskDO push : maochePushTaskDOS) {
                        // 更新
                        maochePushTaskService.updateStatus(new ArrayList<>(Collections.singletonList(push.getId())), TaskStatusEnum.NORMAL.name(), time);

                        time = new Date(time.getTime() + 86400000L);
                    }
                }
            } else {
                for (MaochePushTaskDO push : taskDOS) {
                    // 更新
                    maochePushTaskService.updateStatus(new ArrayList<>(Collections.singletonList(push.getId())), TaskStatusEnum.NORMAL.name(), time);

                    time = new Date(time.getTime() + 86400000L);
                }
            }
        } else if (task.getTimeType().equals(TimeTypeEnum.REPEAT_DAILY_SCHEDULE.name())) {
            // 获取发布时间
            Date publishDate = task.getPublishDate();
            // 获取到时 分
            Date time = DateTimeUtils.getTodyDate(publishDate);
            // 从第二天开始
            time = new Date(time.getTime() + 86400000L);
            if (CollectionUtils.isNotEmpty(taskDOS)) {
                List<String> ids = taskDOS.stream().map(MaochePushTaskDO::getId).collect(Collectors.toList());
                maochePushTaskService.updateStatus(ids, TaskStatusEnum.NORMAL.name(), time);
            }

        } else {

            Date time = new Date();
            if (task.getTimeType().equals(TimeTypeEnum.SCHEDULE.name())) {
                time = task.getPublishDate();
            }
            if (taskContent != null && taskContent.getDelayTime() != null) {
                // 如果是延迟发送的，需要查询最后一条任务的发布时间，再加上延迟时间
                long timeTime = time.getTime();
                try {
                    MaocheTaskDO latestTask = maocheTaskDao.getLatestTask();
                    if (latestTask != null && latestTask.getPublishDate() != null && latestTask.getPublishDate().getTime() > timeTime) {
                        timeTime = latestTask.getPublishDate().getTime();
                    }
                } catch (Exception e) {
                    log.error("查询最新任务异常", e);
                }
                time = new Date(timeTime + taskContent.getDelayTime());
            }

            List<String> ids = taskDOS.stream().map(MaochePushTaskDO::getId).collect(Collectors.toList());
            maochePushTaskService.updateStatus(ids, TaskStatusEnum.NORMAL.name(), time);
        }

        maocheTaskService.openTask(task.getId());
        return Result.OK("更新成功");
    }


    @Resource
    private PushTaskBizService pushTaskBizService;

    // 获取任务详情
    @RequestMapping(value = "/task/test/push")
    @ResponseBody
    public Result<?> testPush() {
        pushTaskBizService.push();
        return Result.OK("更新成功");
    }

}
