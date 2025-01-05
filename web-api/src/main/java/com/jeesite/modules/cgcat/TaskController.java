package com.jeesite.modules.cgcat;


import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheBrandLibDao;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.enums.task.PushTypeEnum;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.enums.task.TaskTypeEnum;
import com.jeesite.modules.cat.model.task.content.PushTaskContent;
import com.jeesite.modules.cat.model.task.content.PushTaskContentDetail;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductBihaohuoService;
import com.jeesite.modules.cat.service.MaochePushTaskService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.MaocheTaskService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibTaskService;
import com.jeesite.modules.cat.service.cg.task.dto.BelongLibDTO;
import com.jeesite.modules.cat.service.cg.task.dto.NameDetail;
import com.jeesite.modules.cat.service.cg.task.dto.ProductDetail;
import com.jeesite.modules.cat.service.cg.task.dto.TaskDetail;
import com.jeesite.modules.cat.service.cg.task.dto.TaskInfo;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.es.TaskEsService;
import com.jeesite.modules.cat.xxl.job.task.PushTaskIndexXxlJob;
import com.jeesite.modules.cgcat.dto.task.BelongLibReq;
import com.jeesite.modules.cgcat.dto.task.SourceTaskCreateReq;
import com.jeesite.modules.cgcat.dto.task.TaskDetailGetReq;
import com.jeesite.modules.cgcat.dto.task.TaskDetailHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class TaskController {

    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductService;

    @Resource
    private MaocheAlimamaUnionProductBihaohuoService maocheAlimamaUnionProductBihaohuoService;

    @Resource
    private TbApiService tbApiService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private TaskDetailHelper taskDetailHelper;

    @Resource
    private MaochePushTaskService maochePushTaskService;

    @Resource
    private MaocheTaskService maocheTaskService;

    @Resource
    private TaskEsService taskEsService;

    @Resource
    private BrandLibTaskService brandLibTaskService;

    @Resource
    private MaocheBrandLibDao maocheBrandLibDao;

    // 获取任务详情
    @RequestMapping(value = "/source/task/info/get")
    public Result<TaskInfo> getTaskInfo(@RequestBody TaskDetailGetReq req) {
        NameDetail miniProgram = new NameDetail();
        miniProgram.setName("小程序");
        miniProgram.setContent("mp://rgI3nQxW1NT3gTz");
        TaskInfo taskInfo = new TaskInfo();
        // 如果是获取到任务id，那么就去获取任务详情
        if (StringUtils.isNotBlank(req.getTaskId())) {
            // 查询任务信息
            // 查询推送任务
            MaocheTaskDO maocheTaskDO = new MaocheTaskDO();
            maocheTaskDO.setId(req.getTaskId());
            MaocheTaskDO taskDO = maocheTaskService.get(maocheTaskDO);
            if (taskDO == null) {
                return null;
            }
            List<MaochePushTaskDO> pushTaskDOs = maochePushTaskService.getByTaskIds(Collections.singletonList(req.getTaskId()));
            if (CollectionUtils.isEmpty(pushTaskDOs) || pushTaskDOs.size() > 1) {
                return null;
            }

            // 新的模式下，一个任务只有一个推送任务
            MaochePushTaskDO pushTaskDO = pushTaskDOs.get(0);
            PushTaskContent taskContent = JsonUtils.toReferenceType(taskDO.getContent(), new TypeReference<PushTaskContent>() {
            });

            int displayTimeType = 0;
            long delayTime = 0;
            if (taskContent != null) {
                displayTimeType = Optional.ofNullable(taskContent.getDisplayTimeType()).orElse(0);
                delayTime = Optional.ofNullable(taskContent.getDelayTime()).orElse(0L);
            }

            taskInfo.setTaskId(taskDO.getId());
            taskInfo.setTaskSwitch(taskDO.getTaskSwitch());
            taskInfo.setDelayTime(delayTime);
            taskInfo.setDisplayTimeType(displayTimeType);
            taskInfo.setTimeType(taskDO.getTimeType());
            taskInfo.setPublishDate(taskDO.getPublishDate());
            taskInfo.setTitle(taskDO.getTitle());
            taskInfo.setStatus(taskDO.getStatus());

            taskInfo.setPushType(pushTaskDO.getPushType());
            taskInfo.setPushTaskId(pushTaskDO.getId());
            String detail = pushTaskDO.getDetail();
            if (StringUtils.isNotBlank(detail)) {
                TaskDetail taskDetail = JsonUtils.toReferenceType(detail, new TypeReference<TaskDetail>() {
                });
                taskInfo.setDetail(taskDetail);
            }

            return Result.OK(taskInfo);

        } else {
            String source = req.getSource();
            if ("ocean".equals(source)) {
                TaskDetail oceanTaskDetail = getOceanTaskDetail(req);
                oceanTaskDetail.setMiniProgram(miniProgram);
                taskInfo.setDetail(oceanTaskDetail);
                return Result.OK(taskInfo);
            } else if ("product".equals(source)) {
                // 获取到商品的item_id然后走口令的逻辑
                List<String> resourceIds = req.getResourceIds();
                if (CollectionUtils.isEmpty(resourceIds)) {
                    return null;
                }

                TaskDetail detail = new TaskDetail();
                List<ProductDetail> products = new ArrayList<>();

                for (String resourceId : resourceIds) {
                    TaskDetail item = taskDetailHelper.convertTb(resourceId);
                    if (item == null || CollectionUtils.isEmpty(item.getProducts())) {
                        continue;
                    }
                    products.addAll(item.getProducts());
                }

                detail.setProducts(products);
                detail.setMiniProgram(miniProgram);

                taskInfo.setDetail(detail);
                return Result.OK(taskInfo);
            } else if ("command".equals(source)) {
                String content = req.getResourceIds().get(0);
                TaskDetail item = null;
                // 判断是淘宝还是京东
                if ("tb".equals(req.getAffType())) {
                    item = taskDetailHelper.convertTb(content);

                } else if ("jd".equals(req.getAffType())) {
                    item = taskDetailHelper.convertJd(content);
                }
                if (item == null) {
                    return null;
                }

                item.setMiniProgram(miniProgram);
                taskInfo.setDetail(item);
                return Result.OK(taskInfo);
            }
        }

        return Result.ERROR(404, "获取任务详情失败");
    }

    private TaskDetail getOceanTaskDetail(TaskDetailGetReq req) {
        String sourceId = req.getSourceId();
        long msgId = NumberUtils.toLong(sourceId);
        String affType = req.getAffType();
        TaskDetail detail = new TaskDetail();
        if (msgId <= 0) {
            return detail;
        }
        // 查询公海数据
        MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
        query.setId(sourceId);
        MaocheRobotCrawlerMessageSyncDO messageSyncDO = maocheRobotCrawlerMessageSyncService.get(query);
        if (messageSyncDO == null) {
            return detail;
        }

        // 获取商品详情
        // 判断是tb还是jd
        if ("tb".equals(affType)) {

           detail = taskDetailHelper.convertTb(messageSyncDO.getMsg());

        } else if ("jd".equals(affType)) {
            // 京东
            detail = taskDetailHelper.convertJd(messageSyncDO.getMsg());
        }

        return detail;
    }

    // 新增商品
    @RequestMapping(value = "/source/task/product/detail/add")
    public Result<TaskDetail> addTaskProductDetail(@RequestBody TaskDetailGetReq req) {

        if (CollectionUtils.isEmpty(req.getResourceIds())) {
            return Result.ERROR(500, "资源参数错误");
        }

        if (StringUtils.isBlank(req.getAffType())) {
            return Result.ERROR(500, "类型不能为空");
        }

        String content = req.getResourceIds().get(0);
        TaskDetail item = null;
        // 判断是淘宝还是京东
        if ("tb".equals(req.getAffType())) {
            item = taskDetailHelper.convertTb(content);

        } else if ("jd".equals(req.getAffType())) {
            item = taskDetailHelper.convertJd(content);
        }
        if (item != null) {
            return Result.OK(item);
        }


        return Result.ERROR(404, "获取任务详情失败");
    }

    // 更新or修改任务
    @RequestMapping(value = "/source/task/edit")
    public Result<?> createTask(@RequestBody SourceTaskCreateReq req) {

        if (req == null || req.getDetail() == null) {
            return Result.ERROR(500, "参数错误");
        }

        String title = null;
        TaskDetail detail = req.getDetail();
        List<ProductDetail> products = detail.getProducts();
        if (CollectionUtils.isNotEmpty(products)) {
            ProductDetail productDetail = products.get(0);
            title = productDetail.getTitle();
        }
        if (StringUtils.isNotBlank(req.getTitle())) {
            title = req.getTitle();
        }
        if (StringUtils.isBlank(title)) {
            title =  UUID.randomUUID().toString();
        }

        PushTypeEnum pushTypeEnum = PushTypeEnum.getByName(req.getPushType());
        if (pushTypeEnum == null) {
            return Result.ERROR(500, "推送类型不能为空");
        }

        // 保存任务
        // 创建任务
        MaocheTaskDO task = new MaocheTaskDO();
        req.initPublishTime();

        task.setId(req.getTaskId());
        task.setTitle(title);
        task.setSubTitle("NEW-PUSH");
        task.setTaskType(TaskTypeEnum.PUSH.name());
        task.setTimeType(req.getTimeType());
        task.setPublishDate(req.getPublishDate());

        PushTaskContent taskContent = new PushTaskContent();
        taskContent.setSource(req.getSource());
        taskContent.setDelayTime(req.getDelayTime());
        taskContent.setDisplayTimeType(req.getDisplayTimeType());

        task.setContent(JsonUtils.toJSONString(taskContent));
        boolean res = maocheTaskService.createOrUpdateTask(task);
        if (!res) {
            return Result.ERROR(500, "任务创建失败");
        }

        // 创建推送任务
        MaochePushTaskDO pushTaskDO = new MaochePushTaskDO();
        pushTaskDO.setId(req.getPushTaskId());
        pushTaskDO.setTitle(title);
        pushTaskDO.setTaskId(task.getId());

        pushTaskDO.setSubTitle("NEW-PUSH");
        pushTaskDO.setResourceId("");
        pushTaskDO.setResourceType("");
        pushTaskDO.setPushType(pushTypeEnum.name());
        pushTaskDO.setPublishDate(new Date());
        pushTaskDO.setStatus(TaskStatusEnum.INIT.name());

        // todo 通过任务详情，获取到发送内容
        TaskDetail taskDetail = req.getDetail();
        pushTaskDO.setDetail(JsonUtils.toJSONString(taskDetail));
        PushTaskContentDetail content = PushTaskContentDetail.buildContent(pushTaskDO, taskDetail);
        pushTaskDO.setContent(JsonUtils.toJSONString(content));

        // 创建发送内容
        maochePushTaskService.createOrUpdatePushTask(pushTaskDO);

        taskEsService.indexEs(Collections.singletonList(pushTaskDO.getId()), 10);

        return Result.OK("处理完成");
    }

    @RequestMapping(value = "/source/push/task/lib/match")
    public Result<List<BelongLibDTO>> matchLibs(@RequestBody BelongLibReq req) {
        try {
            if (req == null || StringUtils.isBlank(req.getContent())) {
                return Result.ERROR(400, "参数异常");
            }
            // 关键词匹配
            String content = req.getContent();
            MaocheBrandLibKeywordDO keywordDO = brandLibTaskService.matchBrandLib(content);
            if (keywordDO == null) {
                return Result.OK(new ArrayList<>());
            }
            Long iid = keywordDO.getIid();
            Long libId = keywordDO.getBrandLibId();
            // 查询
            MaocheBrandLibDO brandLibDO = maocheBrandLibDao.getById(libId);
            BelongLibDTO dto = BelongLibDTO.toDTO(keywordDO, brandLibDO);

            return Result.OK(Collections.singletonList(dto));
        } catch (Exception e) {
            return Result.ERROR(500, "处理失败");
        }
    }


    @Resource
    private PushTaskIndexXxlJob pushTaskIndexXxlJob;

    @RequestMapping(value = "/source/push/task/index")
    public Result<?> indexAll() {

        try {
            pushTaskIndexXxlJob.execute();
        } catch (Exception e) {
            return Result.ERROR(500, "处理失败");
        }

        return Result.OK("处理完成");
    }

}
