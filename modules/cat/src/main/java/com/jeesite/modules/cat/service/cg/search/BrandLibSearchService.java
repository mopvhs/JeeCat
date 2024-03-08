package com.jeesite.modules.cat.service.cg.search;

import com.alibaba.fastjson.JSON;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.BrandLibCondition;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.MaocheBrandLibraryIndex;
import com.jeesite.modules.cat.service.es.common.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class BrandLibSearchService {

    @Resource
    private SearchService searchService;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    public ElasticSearchData<MaocheBrandLibraryIndex, CatProductBucketTO> search(BrandLibCondition condition,
                                                                                        Function<BrandLibCondition, List<AggregationBuilder>> aggregations,
                                                                                        Function<Aggregations, Map<String, List<CatProductBucketTO>>> bucketConverter,
                                                                                        int from,
                                                                                        int size) {

        SearchSourceBuilder source = searchService.searchSource(condition, aggregations, this::defaultSorts, null, BrandLibCondition.class, from, size);

        return elasticSearch7Service.search(source, ElasticSearchIndexEnum.MAOCHE_BRAND_LIBRARY_INDEX, null, BrandLibSearchService::convertIndex, bucketConverter);
    }

    // 结构化
    public static MaocheBrandLibraryIndex convertIndex(String index) {

        return JSON.parseObject(index, MaocheBrandLibraryIndex.class);
    }

    public void defaultSorts(BrandLibCondition condition, SearchSourceBuilder searchSourceBuilder) {

        List<String> sorts = condition.getSorts();
        if (CollectionUtils.isEmpty(sorts)) {
            sorts = Collections.singletonList("createTime desc");
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
