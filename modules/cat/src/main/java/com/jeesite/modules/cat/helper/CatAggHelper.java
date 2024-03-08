package com.jeesite.modules.cat.helper;

import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

public class CatAggHelper {

    // 选品页顶部tab ，全部，未上架，已上架
    public static AggregationBuilder buildSelectionTabAgg(CatUnionProductCondition condition) {

        return AggregationBuilders
                .terms("groupBySaleStatus")
                .field("saleStatus")
                .size(10);
    }
}
