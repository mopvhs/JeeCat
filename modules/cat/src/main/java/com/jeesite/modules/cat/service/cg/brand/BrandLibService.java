package com.jeesite.modules.cat.service.cg.brand;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.modules.cat.BrandLibCondition;
import com.jeesite.modules.cat.aop.MaocheBrandIndex;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.MaocheBrandLibraryIndex;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.model.condition.PushTaskIndexCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.service.MaochePushTaskRuleService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.cg.search.BrandLibSearchService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.PushTaskIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.jeesite.modules.cat.enums.ElasticSearchIndexEnum.MAOCHE_BRAND_INDEX;
import static com.jeesite.modules.cat.enums.ElasticSearchIndexEnum.MAOCHE_BRAND_LIBRARY_INDEX;

@Slf4j
@Component
public class BrandLibService {

    @Resource
    private MaochePushTaskRuleService maochePushTaskRuleService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private OceanSearchService oceanSearchService;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private BrandLibSearchService brandLibSearchService;


    // 获取品牌库的关键词数量
    public long getKeywordsProductCnt(List<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return 0;
        }
        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setKeywords(keywords);

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, CatUnionProductCondition.class);
        brandLibProductQuery(condition, boolBuilder);

        List<QueryBuilder> must = boolBuilder.must();
        if (CollectionUtils.isEmpty(must)) {
            return 0;
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ES搜索条件
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);
        searchSourceBuilder.query(boolBuilder);

        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(searchSourceBuilder);
        if (searchData == null) {
            return 0;
        }

        return searchData.getTotal();
    }

    // 获取品牌库的关键词数量
    public long getKeywordsOceanCnt(List<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return 0;
        }
        OceanMessageCondition condition = new OceanMessageCondition();
//        condition.setBrandLibId(brandLibId);
        condition.setKeywords(keywords);
        condition.setStatus("NORMAL");
        // 时间要求是当天
        condition.setGteCreateDate(DateTimeUtils.getDay(new Date()).getTime());

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, OceanMessageCondition.class);
        brandLibOceanQuery(condition, boolBuilder);

        List<QueryBuilder> must = boolBuilder.must();
        if (CollectionUtils.isEmpty(must)) {
            return 0;
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ES搜索条件
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);
        searchSourceBuilder.query(boolBuilder);

        ElasticSearchData<MaocheMessageProductIndex, Object> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX,
                null,
                OceanSearchService::convertMessageProduct,
                null);

        if (searchData == null) {
            return 0;
        }

        return searchData.getTotal();
    }


    // 获取品牌库的关键词数量
    public void brandLibProductQuery(CatUnionProductCondition condition, BoolQueryBuilder builder) {
        List<String> brandLibKeywords = condition.getKeywords();
        if (CollectionUtils.isEmpty(brandLibKeywords)) {
            return;
        }
        BoolQueryBuilder should = new BoolQueryBuilder();
        for (String title : brandLibKeywords) {
            should.should(QueryBuilders.matchPhraseQuery("title", title).slop(100));
        }

        builder.must(should);
        // 时间要求是当天
        builder.must(QueryBuilders.rangeQuery("createTime").gte(DateTimeUtils.getDay(new Date()).getTime()));
    }

    public void brandLibOceanQuery(OceanMessageCondition condition, BoolQueryBuilder builder) {
        List<String> brandLibKeywords = condition.getKeywords();
        if (CollectionUtils.isEmpty(brandLibKeywords)) {
            return;
        }
        BoolQueryBuilder should = new BoolQueryBuilder();
        for (String title : brandLibKeywords) {
            should.should(QueryBuilders.matchPhraseQuery("msg", title).slop(100));
            should.should(QueryBuilders.matchPhraseQuery("msgNgram", title).slop(100));
        }

        builder.must(should);
    }

    /**
     * 历史推送完成的任务数量
     * @param brandLibId
     * @return
     */
    public long getPushTaskCnt(Long brandLibId, Date finishDate) {
        if (brandLibId == null || brandLibId <= 0) {
            return 0;
        }
        PushTaskIndexCondition condition = new PushTaskIndexCondition();
        condition.setBrandLibId(brandLibId);
        condition.setStatus(StringUtils.lowerCase(TaskStatusEnum.FINISHED.name()));
        if (finishDate != null) {
            condition.setGteFinishTime(finishDate.getTime());
        }

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, PushTaskIndexCondition.class);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ES搜索条件
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);
        searchSourceBuilder.query(boolBuilder);
        ElasticSearchData<PushTaskIndex, Object> search = elasticSearch7Service.search(searchSourceBuilder, ElasticSearchIndexEnum.PUSH_TASK_INDEX, null, CatRobotHelper::convertPushTaskIndex, null);
        if (search == null) {
            return 0;
        }

        return search.getTotal();
    }


    /**
     * 获取品牌库的任务数量(已发布)
     * @param finishDate 开始时间
     * @return
     */
    public long getBrandLibFinishedTaskCnt(Date finishDate) {
        if (finishDate == null) {
            return 0;
        }
        PushTaskIndexCondition condition = new PushTaskIndexCondition();
        condition.setHadBrandLibId(true);
        condition.setStatus(TaskStatusEnum.FINISHED.name());
        condition.setGteFinishTime(finishDate.getTime());

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, PushTaskIndexCondition.class);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ES搜索条件
        searchSourceBuilder.query(boolBuilder);
        return elasticSearch7Service.count(searchSourceBuilder, ElasticSearchIndexEnum.PUSH_TASK_INDEX);
    }

    /**
     * 获取到上次推送的时间
     */
    public Long getLastPushTime(Long brandLibId) {
        if (brandLibId == null || brandLibId <= 0) {
            return null;
        }
        PushTaskIndexCondition condition = new PushTaskIndexCondition();
        condition.setBrandLibId(brandLibId);
        condition.setStatus(StringUtils.lowerCase(TaskStatusEnum.FINISHED.name()));
        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, PushTaskIndexCondition.class);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ES搜索条件
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);
        searchSourceBuilder.query(boolBuilder);
        buildSort(searchSourceBuilder, List.of("finishTime desc"));

        ElasticSearchData<PushTaskIndex, Object> search = elasticSearch7Service.search(searchSourceBuilder, ElasticSearchIndexEnum.PUSH_TASK_INDEX, null, CatRobotHelper::convertPushTaskIndex, null);
        if (search == null || CollectionUtils.isEmpty(search.getDocuments())) {
            return null;
        }

        return search.getDocuments().get(0).getFinishTime();
    }

    /**
     * 下次推送时间
     */
    public Long getNextPushTime(Long brandLibId) {
        if (brandLibId == null || brandLibId <= 0) {
            return null;
        }
        PushTaskIndexCondition condition = new PushTaskIndexCondition();
        condition.setBrandLibId(brandLibId);
        condition.setStatus(StringUtils.lowerCase(TaskStatusEnum.NORMAL.name()));

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, PushTaskIndexCondition.class);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ES搜索条件
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);
        searchSourceBuilder.query(boolBuilder);
        buildSort(searchSourceBuilder, List.of("finishTime asc"));

        ElasticSearchData<PushTaskIndex, Object> search = elasticSearch7Service.search(searchSourceBuilder, ElasticSearchIndexEnum.PUSH_TASK_INDEX, null, CatRobotHelper::convertPushTaskIndex, null);
        if (search == null || CollectionUtils.isEmpty(search.getDocuments())) {
            return null;
        }

        return search.getDocuments().get(0).getFinishTime();
    }

    public static void buildSort(SearchSourceBuilder searchSourceBuilder, List<String> sorts) {
        if (CollectionUtils.isEmpty(sorts)) {
            return;
        }
        for (String sort : sorts) {
            if (StringUtils.isBlank(sort)) {
                continue;
            }
            String[] sortArr = sort.split(" ");
            String name = sortArr[0];
            SortOrder sortOrder = SortOrder.DESC;
            if (sortArr.length > 1) {
                String order = sortArr[1];
                if (order.equals("asc")) {
                    sortOrder = SortOrder.ASC;
                }
            }

            searchSourceBuilder.sort(name, sortOrder);
        }
    }

    // 获取重复的关键词
    public List<String> getRepeatKeywords(Long id, List<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return null;
        }

        BrandLibCondition condition = new BrandLibCondition();
        if (id != null && id > 0) {
            condition.setFilterIds(Collections.singletonList(id));
        }
        condition.setKeywords(keywords);

        ElasticSearchData<MaocheBrandLibraryIndex, CatProductBucketTO> search = brandLibSearchService.search(condition, null, null, 0, 1000);
        if (search == null) {
            return null;
        }
        if (CollectionUtils.isEmpty(search.getDocuments())) {
            return new ArrayList<>();
        }
        // 获取所有的keyword
        List<String> allKeywords = new ArrayList<>();
        for (MaocheBrandLibraryIndex index : search.getDocuments()) {
            List<String> keyword = index.getKeyword();
            if (CollectionUtils.isEmpty(keyword)) {
                continue;
            }
            allKeywords.addAll(keyword);
        }

        allKeywords = allKeywords.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        // 获取到交集
        return keywords.stream().filter(allKeywords::contains).collect(Collectors.toList());
    }

    // 品牌信息
    public ElasticSearchData<MaocheBrandIndex, Object> suggestBrands(String keyword, String firstSpell, int from, int size) {

        if (size <= 0) {
            size = 20;
        }
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.matchPhrasePrefixQuery("brand.kw", keyword));

        if (StringUtils.isNotBlank(firstSpell)) {
            queryBuilder.must(QueryBuilders.matchPhraseQuery("firstSpell", StringUtils.lowerCase(firstSpell)));
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(from)
                .size(size)
                .query(queryBuilder);

        return elasticSearch7Service.search(searchSourceBuilder, MAOCHE_BRAND_INDEX, CatRobotHelper::convertMaocheBrand, null);
    }

}
