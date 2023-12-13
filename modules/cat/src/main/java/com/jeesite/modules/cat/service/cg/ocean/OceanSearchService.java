package com.jeesite.modules.cat.service.cg.ocean;

import com.alibaba.fastjson.JSON;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageProductCondition;
import com.jeesite.modules.cat.service.es.common.SearchService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Component
public class OceanSearchService {

    @Resource
    private SearchService searchService;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;


    public ElasticSearchData<MaocheMessageSyncIndex, CatProductBucketTO> searchMsg(OceanMessageCondition condition,
                                                                       Function<OceanMessageCondition, List<AggregationBuilder>> aggregations,
                                                                       Function<Aggregations, Map<String, List<CatProductBucketTO>>> bucketConverter,
                                                                       BiConsumer<OceanMessageCondition, BoolQueryBuilder> customBoolQueryBuilder,
                                                                       int from, int size) {

        SearchSourceBuilder source = searchService.searchSource(condition, aggregations, this::sort, customBoolQueryBuilder, OceanMessageCondition.class, from, size);


        return elasticSearch7Service.search(source, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, null, OceanSearchService::convertMessage, bucketConverter);
    }

    public ElasticSearchData<MaocheMessageProductIndex, CatProductBucketTO> searchProduct(OceanMessageProductCondition condition,
                                                                              Function<OceanMessageProductCondition, List<AggregationBuilder>> aggregations,
                                                                              Function<Aggregations, Map<String, List<CatProductBucketTO>>> bucketConverter,
                                                                              int from,
                                                                              int size) {

        SearchSourceBuilder source = searchService.searchSource(condition, aggregations, this::sortProduct, null, OceanMessageProductCondition.class, from, size);

        return elasticSearch7Service.search(source, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_PRODUCT_INDEX, null, OceanSearchService::convertMessageProduct, bucketConverter);
    }

    // 结构化
    public static MaocheMessageProductIndex convertMessageProduct(String index) {

        return JSON.parseObject(index, MaocheMessageProductIndex.class);
    }


    // 结构化
    public static MaocheMessageSyncIndex convertMessage(String index) {

        return JSON.parseObject(index, MaocheMessageSyncIndex.class);
    }

    public void sort(OceanMessageCondition condition, SearchSourceBuilder searchSourceBuilder) {

        List<String> sorts = condition.getSorts();
        if (CollectionUtils.isEmpty(sorts)) {
            sorts = Collections.singletonList("createDate desc");
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

    public void sortProduct(OceanMessageProductCondition condition, SearchSourceBuilder searchSourceBuilder) {

        List<String> sorts = condition.getSorts();
        if (CollectionUtils.isEmpty(sorts)) {
            sorts = Collections.singletonList("createDate desc");
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

}
