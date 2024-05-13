package com.jeesite.modules.cgcat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.BrandLibCondition;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.entity.MaochePushTaskRuleDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.BrandLibTO;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.SpecificationTO;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.model.MaocheBrandLibraryIndex;
import com.jeesite.modules.cat.model.condition.PushTaskIndexCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaochePushTaskRuleService;
import com.jeesite.modules.cat.service.MaocheTagService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibConvertService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.cg.search.BrandLibSearchService;
import com.jeesite.modules.cat.service.es.BrandLibEsService;
import com.jeesite.modules.cat.service.es.TaskEsService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.es.dto.PushTaskIndex;
import com.jeesite.modules.cat.xxl.job.task.BrandLibCntIndexXxlJob;
import com.jeesite.modules.cgcat.dto.BrandLibPageDetailVO;
import com.jeesite.modules.cgcat.dto.ProductSpecificationTemplateVO;
import com.jeesite.modules.cgcat.dto.PushTaskRuleDTO;
import com.jeesite.modules.cgcat.dto.PushTaskRuleKeywordRequest;
import com.jeesite.modules.cgcat.dto.PushTaskRuleRequest;
import com.jeesite.modules.cgcat.dto.ocean.OceanMessageVO;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.annotations.Param;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class CgPushTaskRuleController {

    @Resource
    private MaochePushTaskRuleService maochePushTaskRuleService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheTagService maocheTagService;

    @Resource
    private BrandLibEsService brandLibEsService;

    @Resource
    private BrandLibSearchService brandLibSearchService;

    @Resource
    private BrandLibCntIndexXxlJob brandLibCntIndexXxlJob;

    @Resource
    private BrandLibConvertService brandLibConvertService;

    @Resource
    private BrandLibService brandLibService;

    @Resource
    private OceanSearchService oceanSearchService;

    @Resource
    private MaocheCategoryMappingService maocheCategoryMappingService;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private TaskEsService taskEsService;

    // 线程池
    private static ExecutorService threadPool = new ThreadPoolExecutor(5, 20,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
            new DefaultThreadFactory("push-task-rule"));


    // 规则列表头部详情信息获取
    @RequestMapping(value = "/product/push/task/rule/page/detail")
    @ResponseBody
    public BrandLibPageDetailVO pageDetail() {
        // 获取所有的5星规则
        BrandLibCondition condition = new BrandLibCondition();
        condition.setStar(5L);
        ElasticSearchData<MaocheBrandLibraryIndex, CatProductBucketTO> search = brandLibSearchService.search(condition, null, null, 0, 1000);

        List<OceanMessageVO> superStarOceans = new ArrayList<>();
        // 关键词
        List<String> keywords = new ArrayList<>();
        if (search != null && CollectionUtils.isNotEmpty(search.getDocuments())) {
            search.getDocuments().stream().map(MaocheBrandLibraryIndex::getKeyword).forEach(keywords::addAll);
        }
        if (CollectionUtils.isNotEmpty(keywords)) {
            keywords = keywords.stream().distinct().collect(Collectors.toList());
            // 查询名字关键词的最新20条公海数据
            OceanMessageCondition oceanMessageCondition = new OceanMessageCondition();
            oceanMessageCondition.setSorts(List.of("createDate desc"));
            oceanMessageCondition.setStatus("NORMAL");
            oceanMessageCondition.setKeywords(keywords);
            ElasticSearchData<MaocheMessageSyncIndex, CatProductBucketTO> searchMsg =
                    oceanSearchService.searchMsg(oceanMessageCondition, null, null, brandLibService::brandLibOceanQuery, 0, 20);
            if (searchMsg != null && CollectionUtils.isNotEmpty(searchMsg.getDocuments())) {

                for (MaocheMessageSyncIndex messageSyncIndex : searchMsg.getDocuments()) {
                    OceanMessageVO vo = OceanMessageVO.toVO(messageSyncIndex);
                    if (vo != null) {
                        superStarOceans.add(vo);
                    }
                }
            }
        }

        // 今天的开始时间
        Date day = DateTimeUtils.getDay(new Date());
//        getBrandLibFinishedTaskCnt
        // 昨天的开始时间
        Date twoDay = new Date(day.getTime() - 86400000);

        long dayCnt = brandLibService.getBrandLibFinishedTaskCnt(day);
        long day2Cnt = brandLibService.getBrandLibFinishedTaskCnt(twoDay);

        BrandLibPageDetailVO detailVO = new BrandLibPageDetailVO();
        detailVO.setSuperStarOceans(superStarOceans);

        detailVO.setQuantity(0L);
        detailVO.setBrandLibTaskCnt(dayCnt);
        detailVO.setYesterdayBrandLibTaskCnt(day2Cnt - dayCnt);

        // 类目
        List<BrandLibPageDetailVO.CategoryVO> categoryNum = getCategoryNum();
        detailVO.setCategories(categoryNum);

        return detailVO;
    }

    public List<BrandLibPageDetailVO.CategoryVO> getCategoryNum() {

        // 全部
        Map<String, CatProductBucketTO> totalCnt = getBrandLibCategoryCnt(null);

        // 今天的开始时间
        Date day = DateTimeUtils.getDay(new Date());
        Map<String, CatProductBucketTO>  todayCnt = getBrandLibCategoryCnt(day);

        // 昨天的开始时间
        Date twoDay = new Date(day.getTime() - 86400000);
        Map<String, CatProductBucketTO> twoDayCnt = getBrandLibCategoryCnt(twoDay);

        // 父类目
        List<MaocheCategoryMappingDO> roots = maocheCategoryMappingService.getCategoryFromCache(0L);
        Map<String, BrandLibPageDetailVO.CategoryVO> rootNameMap = new HashMap<>();
        for (MaocheCategoryMappingDO item : roots) {
            BrandLibPageDetailVO.CategoryVO categoryVO =  new BrandLibPageDetailVO.CategoryVO();
            categoryVO.setName(item.getName());
            categoryVO.setQuantity(0L);
            String key = "agg_" + item.getId();
            rootNameMap.put(key, categoryVO);
        }

        fillCategoryCnt(totalCnt, rootNameMap, "total");
        fillCategoryCnt(todayCnt, rootNameMap, "today");
        fillCategoryCnt(twoDayCnt, rootNameMap, "twoDay");

        calYesterdayCategoryCnt(rootNameMap);

        return new ArrayList<>(rootNameMap.values());
    }

    public void calYesterdayCategoryCnt(Map<String, BrandLibPageDetailVO.CategoryVO> rootNameMap) {
        if (MapUtils.isEmpty(rootNameMap)) {
            return;
        }
        for (Map.Entry<String, BrandLibPageDetailVO.CategoryVO> entry : rootNameMap.entrySet()) {
            BrandLibPageDetailVO.CategoryVO value = entry.getValue();
            if (value == null) {
                continue;
            }
            Long total = value.getTotal();
            if (total == null) {
                value.setTotal(0L);
            }
            Long today = value.getToday();
            if (today == null) {
                value.setToday(0L);
            }
            Long yesterday = value.getYesterday();
            if (today == null) {
                value.setYesterday(0L);
            }
            value.setYesterday(value.getYesterday() - value.getToday());
        }
    }

    public void fillCategoryCnt(Map<String, CatProductBucketTO> bucketMap, Map<String, BrandLibPageDetailVO.CategoryVO> rootNameMap, String type) {
        if (MapUtils.isEmpty(bucketMap)) {
            return;
        }
        for (Map.Entry<String, CatProductBucketTO> entry : bucketMap.entrySet()) {
            String key = entry.getKey();
            BrandLibPageDetailVO.CategoryVO categoryVO = rootNameMap.get(key);
            if (categoryVO == null) {
                continue;
            }
            Long count = entry.getValue().getCount();
            if ("total".equals(type)) {
                categoryVO.setTotal(count);
            } else if ("today".equals(type)) {
                categoryVO.setToday(count);
            } else if ("twoDay".equals(type)) {
                // 这个需要减去今天的
                categoryVO.setYesterday(count);
            }
        }
    }

    /**
     *
     * @param time
     * @return
     * {
     *     "agg_25":{"key":null,"name":"agg_25","count":0,"doubleCount":null}
     * }
     */
    public Map<String, CatProductBucketTO> getBrandLibCategoryCnt(Date time) {
        PushTaskIndexCondition condition = new PushTaskIndexCondition();
        condition.setHadBrandLibId(true);
        condition.setStatus(TaskStatusEnum.FINISHED.name());
        condition.setStatus(TaskStatusEnum.INIT.name());
        long startFinishedTime = 0;
        if (time != null) {
            startFinishedTime = time.getTime();
        }
        condition.setGteFinishTime(startFinishedTime);
        SearchSourceBuilder todaySource = taskEsService.searchSource(condition, this::buildRootCategoryAgg, null, null, 0, 0);
        ElasticSearchData<PushTaskIndex, CatProductBucketTO> search = taskEsService.search(todaySource);
        // {"total":727,"documents":[],"bucketMap":{"agg_25":[{"key":null,"name":"agg_25","count":0,"doubleCount":null}],"agg_1":[{"key":null,"name":"agg_1","count":1,"doubleCount":null}],"agg_50":[{"key":null,"name":"agg_50","count":2,"doubleCount":null}],"agg_23":[{"key":null,"name":"agg_23","count":0,"doubleCount":null}]}}
        if (search == null || MapUtils.isEmpty(search.getBucketMap())) {
            return new HashMap<>();
        }
        Map<String, CatProductBucketTO> map = new HashMap<>();
        for (Map.Entry<String, List<CatProductBucketTO>> entry : search.getBucketMap().entrySet()) {
            if (CollectionUtils.isEmpty(entry.getValue())) {
                continue;
            }
            map.put(entry.getKey(), entry.getValue().get(0));
        }

        return map;
    }

    /**
     * 大类目下的小类目数据
     * @param condition
     * @return
     */
    public List<AggregationBuilder> buildRootCategoryAgg(PushTaskIndexCondition condition) {
        long startTime = System.currentTimeMillis();
        List<AggregationBuilder> builders = new ArrayList<>();
        String fieldName = "categorys";
        String aggNameSuffix = "agg_";
        // 一级类目id
        List<Long> rootCids = maocheCategoryMappingService.getRootCids();
        for (Long cid : rootCids) {
            String name = aggNameSuffix + cid;
            // 获取所有子类目
//            log.info("buildRootCategoryAgg 类目：{} 开始", cid);
            List<MaocheCategoryMappingDO> categories = maocheCategoryMappingService.getCategoryFromCache(cid);
            if (CollectionUtils.isEmpty(categories)) {
                continue;
            }
            List<String> cNames = categories.stream().map(MaocheCategoryMappingDO::getName).collect(Collectors.toList());
            FilterAggregationBuilder builder = AggregationBuilders
                    .filter(name, QueryBuilders.termsQuery(fieldName, cNames))
                    .subAggregation(AggregationBuilders.count(name).field("categorys"));

            builders.add(builder);
        }
//        log.info("buildRootCategoryAgg 耗时:{}", System.currentTimeMillis() - startTime);


        return builders;
    }

    // 规则列表获取
    @RequestMapping(value = "/product/push/task/rule/list")
    @ResponseBody
    public Page<BrandLibTO> listRules(@RequestBody BrandLibCondition condition, HttpServletRequest request, HttpServletResponse response) {

        Page<BrandLibTO> page = new Page<>(request, response);
        if (condition == null) {
            return page;
        }

        int pageNo = condition.getPageNo() == null ? 1 : condition.getPageNo();
        int pageSize = condition.getPageSize() == null || condition.getPageSize() <= 0 ? 100 : condition.getPageSize();
        int from = (pageNo - 1) * pageSize;

        processSorts(condition);
        if (StringUtils.isNotBlank(condition.getRootCategoryName())) {
            List<String> relationRootName = maocheCategoryMappingService.getRelationRootName(condition.getRootCategoryName());
            if (CollectionUtils.isNotEmpty(relationRootName)) {
                condition.setCategoryNames(relationRootName);
            }
        }

        ElasticSearchData<MaocheBrandLibraryIndex, CatProductBucketTO> search = brandLibSearchService.search(condition, null, null, from, pageSize);
        if (search == null) {
            return page;
        }
        long total = search.getTotal();
        List<MaocheBrandLibraryIndex> list = search.getDocuments();

        List<BrandLibTO> brandListTOs = new ArrayList<>();
        for (MaocheBrandLibraryIndex index : list) {
            BrandLibTO brandLibTO = brandLibConvertService.convertBrandLibTO(index);
            if (brandLibTO == null) {
                continue;
            }
            brandListTOs.add(brandLibTO);
        }

        Page<BrandLibTO> toPage = new Page<>(page.getPageNo() + 1, pageSize, total, brandListTOs);

        return toPage;
    }


    public void processSorts(BrandLibCondition condition) {
        List<String> sorts = condition.getSorts();
        if (CollectionUtils.isEmpty(sorts)) {
            condition.setSorts(List.of("id asc"));
            return;
        }
        Map<String, String> sortMap = new HashMap<>();
        sortMap.put("pushDailyIncDesc desc", "pushDailyInc desc");
        sortMap.put("pushDailyIncDesc asc", "pushDailyInc asc");
        sortMap.put("nextPushTimeDesc desc", "nextPushTime desc");
        sortMap.put("nextPushTimeDesc asc", "nextPushTime asc");
        sortMap.put("lastPushTimeDesc desc", "lastPushTime desc");
        sortMap.put("lastPushTimeDesc asc", "lastPushTime asc");
        sortMap.put("brand desc", "brand.keyword desc");
        sortMap.put("brand asc", "brand.keyword asc");
        sortMap.put("productName desc", "productName.keyword desc");
        sortMap.put("productName asc", "productName.keyword asc");
        // 替换
        List<String> newSorts = new ArrayList<>();
        for (String sort : sorts) {
            if (StringUtils.isBlank(sort)) {
                continue;
            }
            String newSort = Optional.ofNullable(sortMap.get(sort)).orElse(sort);
            newSorts.add(newSort);
        }

        condition.setSorts(newSorts);
    }

    // 新增或者修改规则
    @RequestMapping(value = "/product/push/task/rule/edit")
    @ResponseBody
    public Result<String> addOrUpdatePushTaskRule(@RequestBody PushTaskRuleRequest request) {
        if (request == null) {
            return Result.ERROR(400, "参数不能为空");
        }

        if (StringUtils.isBlank(request.getProductName())) {
            return Result.ERROR(400, "品名不能为空");
        }
        if (StringUtils.isBlank(request.getBrand())) {
            return Result.ERROR(400, "品牌不能为空");
        }
        if (request.getStar() == null) {
            return Result.ERROR(400, "星级不能为空");
        }

        MaochePushTaskRuleDO ruleDO = new MaochePushTaskRuleDO();
        ruleDO.setBrand(request.getBrand());
        ruleDO.setEnglishBrand(request.getEnglishBrand());
        ruleDO.setProductName(request.getProductName());
        ruleDO.setKeyword(JsonUtils.toJSONString(request.getKeywords()));

        ruleDO.setCategoryId(request.getCategoryId());
        ruleDO.setCategoryName(request.getCategory());
        ruleDO.setLevelOneCategoryId(request.getLevelOneCategoryId());
        ruleDO.setLevelOneCategoryName(request.getLevelOneCategoryName());

        ruleDO.setStar(request.getStar());
        ruleDO.setPolling(request.getPolling());
        ruleDO.setSpecifications(JsonUtils.toJSONString(request.getSpecifications()));

        ruleDO.setUpdateDate(new Date());
        ruleDO.setStatus("NORMAL");
        ruleDO.setDescription(request.getDescribe());

        List<Long> tagIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getTagIds())) {
            tagIds = request.getTagIds().stream().map(NumberUtils::toLong).collect(Collectors.toList());
        }
        ruleDO.setTag(JsonUtils.toJSONString(tagIds));

        // 判断关键词是否重复
        List<String> repeatKeywords = brandLibService.getRepeatKeywords(request.getId(), request.getKeywords());
        if (CollectionUtils.isNotEmpty(repeatKeywords)) {
            return Result.ERROR(400, "关键词重复：" + repeatKeywords);
        }

        // 更新
        if (request.getId() != null && request.getId() > 0) {
            ruleDO.setId(String.valueOf(request.getId()));
            ruleDO.setUiid(request.getId());
            // 更新
            maochePushTaskRuleService.update(ruleDO);

            brandLibEsService.indexEs(ruleDO.getUiid());
            return Result.OK("更新完成");
        }

        ruleDO.setRemarks("");
        ruleDO.setUpdateBy("admin");
        ruleDO.setCreateBy("admin");
        ruleDO.setCreateDate(new Date());

        // 新增
        maochePushTaskRuleService.save(ruleDO);

        brandLibEsService.indexEs(ruleDO.getUiid());

        return Result.OK("新增完成");
    }

    // 删除规则
    @RequestMapping(value = "/product/push/task/rule/delete")
    @Validated
    @ResponseBody
    public Result<String> deletePushTaskRule(@RequestBody PushTaskRuleRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            return Result.ERROR(400, "参数不能为空");
        }

        long id = request.getId();
        MaochePushTaskRuleDO query = new MaochePushTaskRuleDO();
        query.setId(String.valueOf(id));
        MaochePushTaskRuleDO ruleDO = maochePushTaskRuleService.get(query);
        if (ruleDO == null) {
            return Result.ERROR(404, "资源不存在，删除失败");
        }
        // 新增
        maochePushTaskRuleService.deleteById(id);
        brandLibEsService.delete(request.getId());

        return Result.OK("删除完成");
    }

    // 规则详情获取
    @RequestMapping(value = "/product/push/task/rule/get")
    @ResponseBody
    public Result<PushTaskRuleDTO> getPushRule(@RequestBody PushTaskRuleRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            return Result.ERROR(400, "参数不能为空");
        }

        long id = request.getId();
        MaochePushTaskRuleDO query = new MaochePushTaskRuleDO();
        query.setId(String.valueOf(id));
        // 新增
        MaochePushTaskRuleDO ruleDO = maochePushTaskRuleService.get(query);
        if (ruleDO == null) {
            return Result.ERROR(404, "资源不存在");
        }

        PushTaskRuleDTO dto = new PushTaskRuleDTO();
        dto.setId(ruleDO.getUiid());
        dto.setBrand(ruleDO.getBrand());
        dto.setEnglishBrand(Optional.ofNullable(ruleDO.getEnglishBrand()).orElse(""));
        dto.setProductName(ruleDO.getProductName());
        dto.setKeywords(JsonUtils.toReferenceType(ruleDO.getKeyword(), new TypeReference<List<String>>() {
        }));
//        dto.setCategoryId(ruleDO.getCategoryId());
        dto.setCategory(ruleDO.getCategoryName());
//        dto.setLevelOneCategoryId(ruleDO.getLevelOneCategoryId());
        dto.setLevelOneCategoryName(ruleDO.getLevelOneCategoryName());
        dto.setStar(ruleDO.getStar());
        dto.setPolling(ruleDO.getPolling());
        dto.setDescribe(Optional.ofNullable(ruleDO.getDescription()).orElse(""));

        dto.setSpecifications(JsonUtils.toReferenceType(ruleDO.getSpecifications(), new TypeReference<List<SpecificationTO>>() {
        }));

        List<Long> tagIds = Optional.ofNullable(JsonUtils.toReferenceType(ruleDO.getTag(), new TypeReference<List<Long>>() {
        })).orElse(new ArrayList<>());
        dto.setTagIds(tagIds);

        return Result.OK(dto);
    }



    // 获取关键词最大的类目
    @RequestMapping(value = "/product/push/task/rule/keyword/category/get")
    @ResponseBody
    public Result<Map<String, Object>> getKeywordCategory(@RequestBody PushTaskRuleKeywordRequest request) {

        if (request == null || CollectionUtils.isEmpty(request.getKeywords())) {
            return Result.ERROR(400, "关键词不能为空");
        }

        List<String> keywords = request.getKeywords();
        if (CollectionUtils.isEmpty(keywords)) {
            return Result.ERROR(400, "关键词不能为空");
        }

        List<Future<CatProductBucketTO>> futures = new ArrayList<>();

        for (String c : keywords) {
            Future<CatProductBucketTO> submit = threadPool.submit(new Callable<CatProductBucketTO>() {
                @Override
                public CatProductBucketTO call() throws Exception {
                    return doGetKeywordCategory(c);
                }
            });

            futures.add(submit);
        }

        // 数量最大的类目
        CatProductBucketTO maxCategory = null;
        for (Future<CatProductBucketTO> future : futures) {
            try {
                CatProductBucketTO catProductBucketTO = future.get();
                if (catProductBucketTO != null) {
                    if (maxCategory == null) {
                        maxCategory = catProductBucketTO;
                    } else if (maxCategory.getCount() < catProductBucketTO.getCount()) {
                        maxCategory = catProductBucketTO;
                    }
                }
            } catch (Exception e) {
                log.error("获取关键词类目失败", e);
            }
        }

        if (maxCategory == null) {
            return Result.ERROR(400, "查询失败");
        }

        String categoryName = maxCategory.getName();
        String levelOneCategoryName = "宠物/宠物食品及用品";
        // 获取父类
        MaocheCategoryMappingDO category = maocheCategoryMappingService.getParentCategory(categoryName);
        if (category != null) {
            levelOneCategoryName = category.getName();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("categoryName", categoryName);
        data.put("levelOneCategoryName", levelOneCategoryName);

        return Result.OK(data);
    }

    private CatProductBucketTO doGetKeywordCategory(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return null;
        }

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setTitle(keyword);
        SearchSourceBuilder source = cgUnionProductService.searchSource(condition,
                CgPushTaskRuleController::buildCategoryTermsAgg, null, null, 0, 1);

        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> search = cgUnionProductService.search(source);

        if (search == null) {
            return null;
        }

        List<CatProductBucketTO> categories = search.getBucketMap().get("category");
        if (CollectionUtils.isEmpty(categories)) {
            return null;
        }

        return categories.get(0);
    }

    /**
     * 大类目下的小类目数据
     * @param condition
     * @return
     */
    private static List<AggregationBuilder> buildCategoryTermsAgg(CatUnionProductCondition condition) {

        List<AggregationBuilder> builders = new ArrayList<>();

        TermsAggregationBuilder builder = AggregationBuilders
                .terms("category")
                .field("categoryName")
                .size(10);

        builders.add(builder);

        return builders;
    }

    // 检查关键词是否重复
    @RequestMapping(value = "/product/push/task/rule/check/keyword")
    @ResponseBody
    public Result<Boolean> checkKeyword(@Param("keyword") String keyword) {

        if (StringUtils.isBlank(keyword)) {
            return Result.ERROR(400, "关键词不能为空");
        }

        boolean checkKeyword = maochePushTaskRuleService.checkKeyword(keyword);
        if (!checkKeyword) {
            // 关键词不可用
            return Result.ERROR(400, "关键词已存在");
        }

        return Result.OK(checkKeyword);
    }

    // 获取规则模板 - specification
    @RequestMapping(value = "/product/push/task/rule/specification/template/get")
    @ResponseBody
    public Result<ProductSpecificationTemplateVO> addPushTaskRule() {

        // todo yhq

        return null;
    }

    @RequestMapping(value = "/source/push/task/rule/index/cnt/update")
    public Result<?> updateCnt() {

        try {
            brandLibCntIndexXxlJob.execute();
        } catch (Exception e) {
            return Result.ERROR(500, "处理失败");
        }

        return Result.OK("处理完成");
    }


}
