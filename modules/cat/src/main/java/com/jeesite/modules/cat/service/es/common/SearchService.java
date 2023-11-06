package com.jeesite.modules.cat.service.es.common;

import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Component
public class SearchService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

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
    public <T> SearchSourceBuilder searchSource(T condition,
                                            Function<T, List<AggregationBuilder>> aggregations,
                                            BiConsumer<T, SearchSourceBuilder> sort,
                                            BiConsumer<T, BoolQueryBuilder> customBoolQueryBuilder,
                                            Class clazz,
                                            int from,
                                            int size) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, clazz);

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

}
