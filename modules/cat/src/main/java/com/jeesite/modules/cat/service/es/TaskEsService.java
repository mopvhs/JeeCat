package com.jeesite.modules.cat.service.es;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.model.condition.PushTaskIndexCondition;
import com.jeesite.modules.cat.service.MaochePushTaskService;
import com.jeesite.modules.cat.service.MaocheTaskService;
import com.jeesite.modules.cat.service.es.dto.PushTaskIndex;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TaskEsService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaochePushTaskService maochePushTaskService;

    @Resource
    private MaocheTaskService maocheTaskService;

    public boolean indexEs(List<String> pushIds, int corePoolSize) {
        if (CollectionUtils.isEmpty(pushIds)) {
            return false;
        }
        pushIds = pushIds.stream().distinct().collect(Collectors.toList());

        List<MaochePushTaskDO> pushTaskDOs = maochePushTaskService.getByIds(pushIds);
        if (CollectionUtils.isEmpty(pushTaskDOs)) {
            return false;
        }

        List<String> taskIds = pushTaskDOs.stream().map(MaochePushTaskDO::getTaskId).distinct().toList();
        List<MaocheTaskDO> maocheTaskDOs = maocheTaskService.listByIds(taskIds);
        Map<String, MaocheTaskDO> taskDOMap = maocheTaskDOs.stream().collect(Collectors.toMap(MaocheTaskDO::getId, Function.identity(), (k1, k2) -> k1));

        List<Map<String, Object>> messageSyncIndex = build(pushTaskDOs, taskDOMap);

        elasticSearch7Service.index(messageSyncIndex, ElasticSearchIndexEnum.PUSH_TASK_INDEX, "id", corePoolSize);

        return true;
    }

    private List<Map<String, Object>> build(List<MaochePushTaskDO> pushTaskDOs,
                                            Map<String, MaocheTaskDO> taskDOMap) {
        if (CollectionUtils.isEmpty(pushTaskDOs)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        
        for (MaochePushTaskDO push : pushTaskDOs) {
            MaocheTaskDO maocheTaskDO = taskDOMap.get(push.getTaskId());
            PushTaskIndex index = PushTaskIndex.toIndex(push, maocheTaskDO);
            if (index == null) {
                continue;
            }
            Map<String, Object> map = JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
            });

            if (MapUtils.isEmpty(map)) {
                continue;
            }

            list.add(map);
        }
        return list;
    }

    /**
     *
     * @param condition 条件
     * @param aggregations 聚合
     * @param sort 排序
     * @param customBoolQueryBuilder 自定义build
     * @param from from
     * @param size size
     * @return
     */
    public SearchSourceBuilder searchSource(PushTaskIndexCondition condition,
                                            Function<PushTaskIndexCondition, List<AggregationBuilder>> aggregations,
                                            BiConsumer<PushTaskIndexCondition, SearchSourceBuilder> sort,
                                            BiConsumer<PushTaskIndexCondition, BoolQueryBuilder> customBoolQueryBuilder,
                                            int from,
                                            int size) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, PushTaskIndexCondition.class);

        if (customBoolQueryBuilder != null) {
            customBoolQueryBuilder.accept(condition, boolBuilder);
        }

        // ES搜索条件
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.query(boolBuilder);

        if (aggregations != null) {
            List<AggregationBuilder> aggs = aggregations.apply(condition);
            if (CollectionUtils.isNotEmpty(aggs)) {
                for (AggregationBuilder agg : aggs) {
                    searchSourceBuilder.aggregation(agg);
                }
            }
        }

        if (sort != null) {
            sort.accept(condition, searchSourceBuilder);
        }

        return searchSourceBuilder;
    }

    public ElasticSearchData<PushTaskIndex, CatProductBucketTO> search(SearchSourceBuilder searchSourceBuilder) {

        ElasticSearchData<PushTaskIndex, CatProductBucketTO> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.PUSH_TASK_INDEX,
                CatRobotHelper::convertPushTaskIndex,
                CatRobotHelper::convertUnionProductAggregationMap
        );

        return searchData;
    }
}
