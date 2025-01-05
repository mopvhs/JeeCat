package com.jeesite.modules.cat.service.cg;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class CgUnionProductSearchService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    /**
     * 查询商品索引数据
     * @param condition
     * @param from
     * @param size
     */
//    public ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchProduct(CatUnionProductCondition condition,
//                                                                                            Function<CatUnionProductCondition, AggregationBuilder> aggregation,
//                                                                                            Function<CatUnionProductCondition, CatProductBucketTO> bucketConverter,
//                                                                                            int from,
//                                                                                            int size) {
//        log.info("productWarehouseDetail condition:{}", JsonUtils.toJSONString(condition));
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, CatUnionProductCondition.class);
//
//        List<String> sorts = condition.getSorts();
//        if (CollectionUtils.isNotEmpty(sorts)) {
//            for (String sort : sorts) {
//                if (StringUtils.isBlank(sort)) {
//                    continue;
//                }
//                String[] sortArr = sort.split(" ");
//                String name = sortArr[0];
//                SortOrder sortOrder = SortOrder.DESC;
//                if (sortArr.length > 1) {
//                    String order = sortArr[1];
//                    if (order.equals("asc")) {
//                        sortOrder = SortOrder.ASC;
//                    }
//                }
//                if ("commission".equals(name)) {
//                    // 自定义排序脚本
//                    String code = "return (doc['commissionRate'].value / 100) * (doc['reservePrice'].value / 100)";
//
//                    Script script = new Script(
//                            Script.DEFAULT_SCRIPT_TYPE,
//                            Script.DEFAULT_SCRIPT_LANG,
//                            code,
//                            new HashMap<>(),
//                            new HashMap<>()
//                    );
//
//                    ScriptSortBuilder scriptSortBuilder = SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.NUMBER);
//                    scriptSortBuilder.order(sortOrder);
//                    searchSourceBuilder.sort(scriptSortBuilder);
//                } else {
//                    searchSourceBuilder.sort(name, sortOrder);
//                }
//            }
//        }
//        // ES搜索条件
//        searchSourceBuilder.from(from);
//        searchSourceBuilder.size(size);
//        searchSourceBuilder.query(boolBuilder);
//
//        AggregationBuilder aggregationBuilder = null;
//        if (aggregation != null) {
//            aggregationBuilder = aggregation.apply(condition);
//        }
//
//        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = elasticSearch7Service.search(searchSourceBuilder,
//                ElasticSearchIndexEnum.CAT_PRODUCT_INDEX,
//                aggregationBuilder,
//                CatRobotHelper::convertUnionProduct,
//                CatRobotHelper::convertUnionProductAggregationMap
//        );
//
////        log.info("maocheSearch response {}", JSON.toJSONString(searchData));
//
//        return searchData;
//    }
}
