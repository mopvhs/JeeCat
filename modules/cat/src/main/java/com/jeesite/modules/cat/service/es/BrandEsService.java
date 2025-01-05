package com.jeesite.modules.cat.service.es;

import com.jeesite.modules.cat.aop.MaocheBrandIndex;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.brandlib.BrandLibIndex;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandCondition;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibCondition;
import com.jeesite.modules.cat.service.es.common.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Component
public class BrandEsService {

    @Resource
    private SearchService searchService;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    public ElasticSearchData<MaocheBrandIndex, Object> searchBrand(BrandCondition condition,
                                                                      Function<BrandCondition, List<AggregationBuilder>> aggregation,
                                                                      BiConsumer<BrandCondition, BoolQueryBuilder> customBoolQueryBuilder,
                                                                      int from,
                                                                      int size) {

        SearchSourceBuilder source = searchService.searchSource(condition,
                aggregation,
                null,
                customBoolQueryBuilder,
                BrandCondition.class,
                from,
                size);

        return elasticSearch7Service.search(source, ElasticSearchIndexEnum.MAOCHE_BRAND_INDEX, null, CatRobotHelper::convertMaocheBrand, null);
    }
}
