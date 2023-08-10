package com.jeesite.modules.cgcat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.enums.task.PushTypeEnum;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.enums.task.TaskSwitchEnum;
import com.jeesite.modules.cat.enums.task.TaskTypeEnum;
import com.jeesite.modules.cat.enums.task.TimeTypeEnum;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.PriceHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.model.PriceChartSkuBaseTO;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.model.task.content.PushTaskContent;
import com.jeesite.modules.cat.model.task.content.PushTaskContentDetail;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaochePushTaskService;
import com.jeesite.modules.cat.service.MaocheTaskService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.task.PushTaskBizService;
import com.jeesite.modules.cgcat.dto.PushTaskDetail;
import com.jeesite.modules.cgcat.dto.PushTaskEditRequest;
import com.jeesite.modules.cgcat.dto.PushTaskResponse;
import com.jeesite.modules.cgcat.dto.TaskEditRequest;
import com.jeesite.modules.cgcat.dto.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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

    @RequestMapping(value = "/push/task/list")
    @ResponseBody
    public Page<MaocheTaskDO> taskList(HttpServletRequest request, HttpServletResponse response) {

        Page<MaocheTaskDO> page = new Page<>(request, response);

//        int from = (page.getPageNo() - 1) * page.getPageSize();

        int total = maocheTaskService.getTotal();
        int size = page.getPageSize();

        List<MaocheTaskDO> servicePage = maocheTaskService.getPage(page.getPageNo(), size);

        if (servicePage == null) {
            return page;
        }


        Page<MaocheTaskDO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), total, servicePage);

        return toPage;
    }

    // 推送任务创建
    @RequestMapping(value = "/push/task/create")
    @ResponseBody
    public Result<String> createTask(TaskRequest request) {

        if (request == null) {
            return null;
        }

        List<Long> ids = request.getProductIds();
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        ids = ids.stream().distinct().collect(Collectors.toList());

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setIds(ids);

        // 1. 索引数据
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition, null, 0, ids.size());
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return null;
        }
        CarAlimamaUnionProductIndex index = searchData.getDocuments().get(0);
        // 猫粮 - 零食罐 价格 - 价格
        String title = getTaskTitle(searchData.getDocuments());

        Map<Long, CarAlimamaUnionProductIndex> productIndexMap = searchData.getDocuments().stream().collect(Collectors.toMap(CarAlimamaUnionProductIndex::getId, Function.identity(), (k1, k2) -> k1));

        PushTaskContent content = new PushTaskContent();
        content.setIds(ids);

        // 创建任务
        MaocheTaskDO task = new MaocheTaskDO();
        task.setTitle(title);
        task.setTaskSwitch(TaskSwitchEnum.CLOSE.name());
        task.setSubTitle("");
        task.setTaskType(TaskTypeEnum.PUSH.name());
        task.setSwitchDate(new Date());
        task.setTimeType(TimeTypeEnum.NOW.name());
        task.setPublishDate(new Date());
        task.setContent(JsonUtils.toJSONString());
        task.setStatus(TaskStatusEnum.NORMAL.name());
        task.setCreateBy("admin");
        task.setUpdateBy("admin");
        task.setCreateDate(new Date());
        task.setUpdateDate(new Date());

        maocheTaskService.save(task);

        if (StringUtils.isBlank(task.getId())) {
            return null;
        }

        List<MaochePushTaskDO> pushTasks = new ArrayList<>();
        // 创建子任务
        for (Long id : ids) {
            CarAlimamaUnionProductIndex productIndex = productIndexMap.get(id);
            if (productIndex == null) {
                continue;
            }

            try {
                MaochePushTaskDO pushTaskDO = new MaochePushTaskDO();
                pushTaskDO.setTitle(productIndex.getTitle());
                pushTaskDO.setSubTitle("");
                pushTaskDO.setTaskId(task.getId());
                pushTaskDO.setResourceId(String.valueOf(id));
                pushTaskDO.setResourceType("PRODUCT");
                pushTaskDO.setPushType(PushTypeEnum.SCHEDULE.name());
                pushTaskDO.setPublishDate(new Date());
                pushTaskDO.setStatus(TaskStatusEnum.INIT.name());

                pushTaskDO.setCreateBy("admin");
                pushTaskDO.setUpdateBy("admin");
                pushTaskDO.setCreateDate(new Date());
                pushTaskDO.setUpdateDate(new Date());

                // 创建发送内容
                pushTaskDO.setContent(JsonUtils.toJSONString(buildPushTaskContentDetail(index)));
                maochePushTaskService.save(pushTaskDO);

                pushTasks.add(pushTaskDO);

            } catch (Exception e) {
                log.error("创建子任务失败，id:{}", id, e);
            }
        }

        return Result.OK("创建完成");
    }

    // 获取任务详情
    @RequestMapping(value = "/push/task/detail")
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
            return Result.ERROR(404, "任务不存在");
        }

        List<Long> ids = pushTasks.stream().map(MaochePushTaskDO::getResourceId).map(Long::valueOf).collect(Collectors.toList());

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setIds(ids);
        // 1. 索引数据
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition, null, 0, ids.size());
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return null;
        }
        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
        if (CollectionUtils.isEmpty(productTOs)) {
            return null;
        }

        Map<Long, UnionProductTO> productTOMap = productTOs.stream().collect(Collectors.toMap(UnionProductTO::getId, Function.identity(), (k1, k2) -> k1));
        List<com.jeesite.modules.cgcat.dto.PushTaskDetail> details = new ArrayList<>();

        List<PushTaskDetail> pushTaskDetails = new ArrayList<>();
        for (MaochePushTaskDO push : pushTasks) {
            long productId = NumberUtils.toLong(push.getResourceId());
            UnionProductTO productTO = productTOMap.get(productId);

            if (productTO == null) {
                continue;
            }
            PushTaskDetail detail = new PushTaskDetail();
            detail.setId(push.getId());
            detail.setStatus(push.getStatus());
            detail.setProduct(productTO);
            detail.setFinishedDate(push.getFinishedDate());
            detail.setPublishDate(push.getPublishDate());
            detail.setPushType(push.getPushType());

            PushTaskContentDetail taskContent = JsonUtils.toReferenceType(push.getContent(), new TypeReference<PushTaskContentDetail>() {
            });
            if (taskContent != null) {
                detail.setContent(taskContent.getDetail());
            }

            pushTaskDetails.add(detail);
        }

        PushTaskResponse response = new PushTaskResponse();
        response.setTaskId(request.getTaskId());
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


        task.setTitle(request.getTitle());
        task.setTimeType(request.getTimeType());
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
            return Result.ERROR(404, "推送任务不存在");
        }

        String content = pushTask.getContent();
        PushTaskContentDetail taskContent = null;
        if (StringUtils.isBlank(content)) {
            taskContent = new PushTaskContentDetail();
        } else {
            taskContent = JsonUtils.toReferenceType(content, new TypeReference<PushTaskContentDetail>() {
            });
        }

        pushTask.setTitle(request.getTitle());
//        pushTask.setPushType(request.getPushType());
//        pushTask.setPublishDate(request.getPublishDate());
        pushTask.setContent(JsonUtils.toJSONString(taskContent));

        maochePushTaskService.update(pushTask);

        return Result.OK("更新成功");
    }

    private PushTaskContentDetail buildPushTaskContentDetail(CarAlimamaUnionProductIndex index) {
        StringBuilder content = new StringBuilder();

        content.append(index.getTitle()).append("\n");

        content.append("日常\uD83D\uDCB0").append(PriceHelper.formatPrice(index.getReservePrice(), ".00", ""))
                .append("，到手价\uD83D\uDCB0").append(PriceHelper.formatPrice(index.getPromotionPrice(), ".00", "")).append("\n");

        List<PriceChartSkuBaseTO> priceChartSkuBases = index.getPriceChartSkuBases();
        if (CollectionUtils.isNotEmpty(priceChartSkuBases)) {

            List<String> compareDescList = new ArrayList<>();
            for (PriceChartSkuBaseTO skuBaseTO : priceChartSkuBases) {
                if (skuBaseTO == null || StringUtils.isBlank(skuBaseTO.getCompareDesc())) {
                    continue;
                }
                compareDescList.add(skuBaseTO.getCompareDesc());
            }
            content.append(StringUtils.join(compareDescList, " ")).append("\n");
        }

        if (StringUtils.isNotBlank(index.getItemDescription())) {
            content.append(index.getItemDescription()).append("\n");
        }

        // 淘宝口令
        String apiUrl = cgUnionProductService.getEApiUrl("V73687541H40026415", index.getItemId(), "mm_30153430_909250463_109464700418");

        content.append(apiUrl).append("\n");

        content.append("整段复制后打开🍑查车，长按我头像@我+产品名~");

        PushTaskContentDetail detail = new PushTaskContentDetail();

        detail.setDetail(content.toString());

        return detail;
    }

    private static String getTaskTitle(List<CarAlimamaUnionProductIndex> indices) {
        // 猫粮 - 零食罐 价格 - 价格
        String titleFormat = "%s - %s %s-%s";

        if (CollectionUtils.isEmpty(indices)) {
            return "";
        }
        CarAlimamaUnionProductIndex index = indices.get(0);
        long minPrice = index.getPromotionPrice();
        long maxPrice = index.getPromotionPrice();
        for (CarAlimamaUnionProductIndex item : indices) {
            if (minPrice > item.getPromotionPrice()) {
                minPrice = item.getPromotionPrice();
            }
            if (maxPrice < item.getPromotionPrice()) {
                maxPrice = item.getPromotionPrice();
            }
        }

        return String.format(titleFormat, index.getLevelOneCategoryName(), index.getCategoryName(), PriceHelper.formatPrice(minPrice, ".00", ""), PriceHelper.formatPrice(maxPrice, ".00", ""));

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

        pushTask.setStatus(TaskStatusEnum.DELETE.name());

        maochePushTaskService.updateStatus(pushTask);

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
                .filter(i -> !TaskStatusEnum.STOP.name().equals(i.getStatus()) && !TaskStatusEnum.FINISHED.name().equals(i.getStatus()))
                .toList();

        if (CollectionUtils.isEmpty(taskDOS)) {
            return Result.ERROR(404, "推送任务不存在");
        }

        // 如果是关闭的话，直接关闭
        if (TaskSwitchEnum.CLOSE == switchEnum) {
            task.setTaskSwitch(TaskSwitchEnum.CLOSE.name());
            maocheTaskService.updateStatus(task);
            // 把所有的都改成停止
            for (MaochePushTaskDO push : taskDOS) {
                push.setStatus(TaskStatusEnum.STOP.name());
                // 更新
                maochePushTaskService.update(push);
            }
            List<String> pushIds = taskDOS.stream().map(MaochePushTaskDO::getId).toList();
            maochePushTaskService.updateStatus(pushIds, TaskStatusEnum.STOP.name());

            return Result.OK("更新成功");
        }

        // 如果是开启的话，把所有初始状态的都改成正常，并且设置发布时间
        // 根据不同的推送类型，设置不同的发布时间
        if (task.getTimeType().equals(TimeTypeEnum.DAILY_SCHEDULE.name())) {
            // 获取发布时间
            Date publishDate = task.getPublishDate();
            // 获取到时 分
            Date time = DateTimeUtils.getTodyDate(publishDate);
            for (MaochePushTaskDO push : taskDOS) {
                push.setStatus(TaskStatusEnum.NORMAL.name());
                push.setPublishDate(time);

                // 更新
                maochePushTaskService.update(push);

                time = new Date(time.getTime() + 86400000L);
            }
        } else {

            Date time = new Date();
            if (task.getTimeType().equals(TimeTypeEnum.SCHEDULE.name())) {
                time = request.getPublishDate();
            }
            for (MaochePushTaskDO push : taskDOS) {
                push.setStatus(TaskStatusEnum.NORMAL.name());
                push.setPublishDate(time);

                // 更新
                maochePushTaskService.update(push);
            }
        }

        task.setTaskSwitch(request.getTaskSwitch());
        task.setStatus(TaskStatusEnum.NORMAL.name());
        maocheTaskService.update(task);

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
