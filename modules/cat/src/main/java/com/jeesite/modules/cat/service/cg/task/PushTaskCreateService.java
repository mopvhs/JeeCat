package com.jeesite.modules.cat.service.cg.task;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.enums.task.TaskResourceTypeEnum;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.enums.task.TaskTypeEnum;
import com.jeesite.modules.cat.enums.task.TimeTypeEnum;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.PriceHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.model.PriceChartSkuBaseTO;
import com.jeesite.modules.cat.model.task.content.PushTaskContent;
import com.jeesite.modules.cat.model.task.content.PushTaskContentDetail;
import com.jeesite.modules.cat.model.task.dto.PushTaskCreateRequest;
import com.jeesite.modules.cat.model.task.dto.TaskRequest;
import com.jeesite.modules.cat.service.MaochePushTaskService;
import com.jeesite.modules.cat.service.MaocheTaskService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PushTaskCreateService {


    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheTaskService maocheTaskService;

    @Resource
    private MaochePushTaskService maochePushTaskService;

    @Resource
    private TbApiService tbApiService;

    /**
     * 创建任务
     * @param request
     * @return
     */
    public Result<?> createTask(TaskRequest request) {
        log.info("创建推送任务 request:{}", JsonUtils.toJSONString(request));

        Result<?> result = commonCheck(request);
        if (result == null || !result.isSuccess()) {
            return result;
        }

        if (CollectionUtils.isNotEmpty(request.getProductIds())) {
            return createProductTask(request);
        } else if (CollectionUtils.isNotEmpty(request.getPushTaskCreateDetails())) {
            return createCustomTask(request);
        }

         return Result.error("参数错误");
    }

    public Result<?> commonCheck(TaskRequest request) {
        if (request == null) {
            return Result.error("参数错误");
        }
        // 如果是每日重复任务，只能是自定义的消息任务。并且只能创建一个子任务
        if (CollectionUtils.isEmpty(request.getProductIds())) {
            TimeTypeEnum typeEnum = TimeTypeEnum.getByName(request.getTimeType());
            if (typeEnum == null) {
                return Result.error(500, "发布类型错误");
            }
            if (CollectionUtils.isEmpty(request.getPushTaskCreateDetails())) {
                return Result.error(500, "推送任务信息不能为空");
            }
            if (typeEnum == TimeTypeEnum.REPEAT_DAILY_SCHEDULE && request.getPushTaskCreateDetails().size() != 1) {
                return Result.error(500, "【每日重复推送】只能设置一个推送子任务");
            }
        }

        return Result.OK("校验通过");
    }

    public Result<?> createCustomTask(TaskRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getPushTaskCreateDetails())) {
            return Result.error("参数错误!");
        }
        TimeTypeEnum timeTypeEnum = TimeTypeEnum.getByName(request.getTimeType());
        // 参数校验
        if (timeTypeEnum == null) {
            return Result.error("发布类型错误");
        }
        if (request.getPublishDate() == null) {
            return Result.error("发布时间不能为空");
        }
        if (StringUtils.isBlank(request.getTitle())) {
            return Result.error("标题不能为空");
        }

        // 创建任务
        MaocheTaskDO task = new MaocheTaskDO();
        task.setTitle(request.getTitle());
        task.setSubTitle(Optional.ofNullable(request.getSubTitle()).orElse(""));
        task.setTaskType(TaskTypeEnum.PUSH.name());
        task.setTimeType(timeTypeEnum.name());
        task.setPublishDate(request.getPublishDate());
        task.setContent(JsonUtils.toJSONString(new PushTaskContent()));
        boolean res = maocheTaskService.createTask(task);
        if (!res) {
            return Result.ERROR(500, "创建失败");
        }

        String taskId = task.getId();
        // 创建子任务
        for (PushTaskCreateRequest item : request.getPushTaskCreateDetails()) {
            if (item == null) {
                continue;
            }
            TaskResourceTypeEnum resourceTypeEnum = TaskResourceTypeEnum.getByName(item.getResourceType());
            if (resourceTypeEnum == null) {
                continue;
            }

            try {
                MaochePushTaskDO pushTaskDO = new MaochePushTaskDO();
                pushTaskDO.setTitle(item.getTitle());
                pushTaskDO.setSubTitle("");
                pushTaskDO.setTaskId(taskId);
                pushTaskDO.setPushType(item.getPushType());
                pushTaskDO.setResourceId(Optional.ofNullable(item.getResourceId()).orElse(""));
                pushTaskDO.setResourceType(resourceTypeEnum.name());
                pushTaskDO.setPushType("");
                pushTaskDO.setPublishDate(new Date());
                pushTaskDO.setStatus(TaskStatusEnum.INIT.name());
                pushTaskDO.setCreateBy("admin");
                pushTaskDO.setUpdateBy("admin");
                pushTaskDO.setCreateDate(new Date());
                pushTaskDO.setUpdateDate(new Date());

                PushTaskContentDetail detail = new PushTaskContentDetail(item.getDetail(), item.getImg());
                // 创建发送内容
                pushTaskDO.setContent(JsonUtils.toJSONString(detail));
                maochePushTaskService.save(pushTaskDO);

            } catch (Exception e) {
                log.error("创建子任务失败，item:{}", JsonUtils.toJSONString(item), e);
            }
        }

        return Result.OK("创建完成");
    }

    public Result<?> createProductTask(TaskRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getProductIds())) {
            return Result.error("参数错误!!");
        }

        // 创建商品的推送任务
        List<Long> ids = request.getProductIds();
        if (CollectionUtils.isEmpty(ids)) {
            return Result.ERROR(500, "参数错误");
        }
        ids = ids.stream().distinct().collect(Collectors.toList());
        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setIds(ids);

        // 1. 索引数据
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition, null, 0, ids.size());
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return Result.ERROR(401, "商品不存在");
        }
        // 猫粮 - 零食罐 价格 - 价格
        String title = getTaskTitle(searchData.getDocuments());
        Map<Long, CarAlimamaUnionProductIndex> productIndexMap = searchData.getDocuments().stream().collect(Collectors.toMap(CarAlimamaUnionProductIndex::getId, Function.identity(), (k1, k2) -> k1));

        PushTaskContent content = new PushTaskContent();
        content.setIds(ids);

        // 创建任务
        MaocheTaskDO task = new MaocheTaskDO();
        task.setTitle(title);
        task.setSubTitle("");
        task.setTaskType(TaskTypeEnum.PUSH.name());
        task.setTimeType(TimeTypeEnum.NOW.name());
        task.setPublishDate(new Date());
        task.setContent(JsonUtils.toJSONString(content));
        maocheTaskService.createTask(task);

        if (StringUtils.isBlank(task.getId())) {
            return Result.ERROR(402, "任务创建失败");
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
                pushTaskDO.setResourceType(TaskResourceTypeEnum.PRODUCT.name());
                pushTaskDO.setPushType("");
                pushTaskDO.setPublishDate(new Date());
                pushTaskDO.setStatus(TaskStatusEnum.INIT.name());

                pushTaskDO.setCreateBy("admin");
                pushTaskDO.setUpdateBy("admin");
                pushTaskDO.setCreateDate(new Date());
                pushTaskDO.setUpdateDate(new Date());

                // 创建发送内容
                pushTaskDO.setContent(JsonUtils.toJSONString(buildPushTaskContentDetail(productIndex)));
                maochePushTaskService.save(pushTaskDO);

                pushTasks.add(pushTaskDO);

            } catch (Exception e) {
                log.error("创建子任务失败，id:{}", id, e);
            }
        }

        return Result.OK("创建完成");
    }

    /**
     * 获取商品推送任务的标题
     * @param indices
     * @return
     */
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

    private PushTaskContentDetail buildPushTaskContentDetail(CarAlimamaUnionProductIndex index) {
        StringBuilder content = new StringBuilder();

        content.append(index.getTitle()).append("\n");

        content.append("日常\uD83D\uDCB0").append(PriceHelper.formatPrice(index.getReservePrice(), ".00", ""))
                .append("，券后价\uD83D\uDCB0").append(PriceHelper.formatPrice(index.getPromotionPrice(), ".00", "")).append("\n");

        List<PriceChartSkuBaseTO> priceChartSkuBases = index.getPriceChartSkuBases();
        if (CollectionUtils.isNotEmpty(priceChartSkuBases)) {
            List<String> compareDescList = new ArrayList<>();
            for (PriceChartSkuBaseTO skuBaseTO : priceChartSkuBases) {
                if (skuBaseTO == null || StringUtils.isBlank(skuBaseTO.getCompareDesc())) {
                    continue;
                }
                compareDescList.add(skuBaseTO.getCompareDesc());
            }
            if (CollectionUtils.isNotEmpty(compareDescList)) {
                content.append(StringUtils.join(compareDescList, " ")).append("\n");
            }
        }

        if (StringUtils.isNotBlank(index.getCustomBenefit())) {
            content.append(index.getCustomBenefit()).append("\n");
        } else if (StringUtils.isNotBlank(index.getItemDescription())) {
            content.append(index.getItemDescription()).append("\n");
        }

        // 淘宝口令
//        Result<String> eApiUrl = cgUnionProductService.getEApiUrl("V73687541H40026415", index.getItemId(), "mm_30153430_909250463_109464700418");
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("detail", 2);
        objectMap.put("deepcoupon", 1);
        objectMap.put("couponId", 1);
        Result<CommandResponseV2> commonCommand = tbApiService.getCommonCommand(index.getItemId(), objectMap);
        if (!Result.isOK(commonCommand)) {
            return null;
        }
        CommandResponseV2 result = commonCommand.getResult();
        if (result == null) {
            return null;
        }
        content.append(result.getTbkPwd()).append("\n");
        content.append("---------------------\n");
        content.append("整段复制后打开\uD83C\uDF51\n自助查车@猫车选品官 +产品名");

        PushTaskContentDetail detail = new PushTaskContentDetail();
        // 发送文本
        detail.setDetail(content.toString());
        // 发送的图片地址
        detail.setImg(index.getProductImage());

        return detail;
    }
}
