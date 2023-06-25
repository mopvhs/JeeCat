package com.jeesite.modules.cat.helper;

import com.alibaba.fastjson.JSON;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.aop.EsItemAspect;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
import com.jeesite.modules.cat.model.MaocheProductIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.ParsedMin;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public class CatRobotHelper {

    public static CarRobotCrawlerMessageIndex convert(String index) {

        return JSON.parseObject(index, CarRobotCrawlerMessageIndex.class);
    }

    public static CarAlimamaUnionProductIndex convertUnionProduct(String index) {

        return JSON.parseObject(index, CarAlimamaUnionProductIndex.class);
    }

    public static MaocheProductIndex convertMaocheProduct(String index) {

        return JSON.parseObject(index, MaocheProductIndex.class);
    }

    public static Map<String, List<CatProductBucketTO>> convertUnionProductAggregationMap(Aggregations aggregations) {
        if (aggregations == null) {
            return new HashMap<>();
        }

        Map<String, List<CatProductBucketTO>> map = new HashMap<>();
        Iterator<Aggregation> iterator = aggregations.iterator();
        while (iterator.hasNext()) {
            Aggregation next = iterator.next();
            if (next == null) {
                continue;
            }
            List<CatProductBucketTO> dataList = new ArrayList<>();
            if (next instanceof ParsedStringTerms) {
                ParsedStringTerms terms = (ParsedStringTerms) next;
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                for (Terms.Bucket item : buckets) {
                    CatProductBucketTO data = new CatProductBucketTO();
                    data.setName(String.valueOf(item.getKey()));
                    data.setCount(item.getDocCount());
                    dataList.add(data);
                }
            } else if (next instanceof ParsedTerms) {
                ParsedTerms terms = (ParsedTerms) next;
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                for (Terms.Bucket item : buckets) {
                    CatProductBucketTO data = new CatProductBucketTO();
                    data.setName(String.valueOf(item.getKey()));
                    data.setCount(item.getDocCount());
                    dataList.add(data);
                }
            } else if (next instanceof ParsedMin min) {
                CatProductBucketTO data = new CatProductBucketTO();
                long count = 0;
                if (!Double.isInfinite(min.getValue())) {
                    count = new BigDecimal(String.valueOf(min.getValue())).longValue();
                }
                data.setName(min.getName());
                data.setCount(count);
                data.setDoubleCount(Long.valueOf(count).doubleValue());

                dataList.add(data);
            } else if (next instanceof ParsedMax max) {
                CatProductBucketTO data = new CatProductBucketTO();
                long count = 0;
                if (!Double.isInfinite(max.getValue())) {
                    count = new BigDecimal(String.valueOf(max.getValue())).longValue();
                }
                data.setName(max.getName());
                data.setCount(count);
                data.setDoubleCount(Long.valueOf(count).doubleValue());

                dataList.add(data);
            } else if (next instanceof ParsedFilter filter) {
                CatProductBucketTO data = new CatProductBucketTO();
                data.setName(filter.getName());
                data.setCount(filter.getDocCount());
                dataList.add(data);
            }
            String name = next.getName();

            map.put(name, dataList);

        }

        return map;
    }

    public static <T> BoolQueryBuilder buildQuery(T condition, Class clazz) {
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();

       try {
           buildConvert(boolBuilder, condition, clazz);
       } catch (Exception e) {
           log.error("解析参数异常, condition:{}", JSON.toJSONString(condition), e);
       }

        return boolBuilder;
    }

    public static <T> BoolQueryBuilder buildQuery(T condition, BiFunction<T, BoolQueryBuilder, BoolQueryBuilder> biFunction, Class clazz) {
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();

        try {
            buildConvert(boolBuilder, condition, clazz);
//            if (shouldFunction != null) {
//                List<QueryBuilder> apply = shouldFunction.apply(condition);
//                if (CollectionUtils.isNotEmpty(apply)) {
//                    for (QueryBuilder item : apply) {
//                        boolBuilder.should(item);
//                    }
//                }
//            }
            if (biFunction != null) {
                boolBuilder = biFunction.apply(condition, boolBuilder);
            }
        } catch (Exception e) {
            log.error("解析参数异常, condition:{}", JSON.toJSONString(condition), e);
        }

        return boolBuilder;
    }


    public static <T> void buildConvert(BoolQueryBuilder boolBuilder, T condition, Class clazz) throws IllegalAccessException {
//        Class<CatRobotMessageCondition> clazz = CatRobotMessageCondition.class;
        Field[] fields = clazz.getDeclaredFields();
        Map<String, List<Field>> rangMap = new HashMap<>();

        for (Field field : fields) {
            EsItemAspect annotation = field.getDeclaredAnnotation(EsItemAspect.class);
            if (annotation == null) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(condition);
            if (value == null) {
                continue;
            }

            if (value instanceof String) {
                if (StringUtils.isBlank(String.valueOf(value))) {
                    continue;
                }
            }

            String q = annotation.queryType();
            String name = field.getName();
            if (StringUtils.isNotBlank(annotation.field())) {
                name = annotation.field();
            }
            switch (q) {
                case "itemQuery":
                    boolBuilder.must(QueryBuilders.termQuery(name, value));
                    break;
                case "matchQuery":
                    boolBuilder.must(QueryBuilders.matchQuery(name, value));
                    break;
                case "matchPhraseQuery":
                    boolBuilder.must(QueryBuilders.matchPhraseQuery(name, value).slop(100));
                    break;
                case "itemsQuery":
                    boolBuilder.must(QueryBuilders.termsQuery(name, (Collection) value));
                    break;
                case "mustNotItemQuery":
                    boolBuilder.mustNot(QueryBuilders.termQuery(name, value));
                    break;
                case "mustNotItemsQuery":
                    boolBuilder.mustNot(QueryBuilders.termsQuery(name, (Collection) value));
                    break;
                case "existsQuery":
                    if (value instanceof Boolean b) {
                        if (b) {
                            boolBuilder.must(new ExistsQueryBuilder(name));
                        } else {
                            boolBuilder.mustNot(new ExistsQueryBuilder(name));
                        }
                    }

                    break;
                case "rangeQuery":
                    List<Field> fieldList = rangMap.get(name);
                    if (CollectionUtils.isEmpty(fieldList)) {
                        fieldList = new ArrayList<>();
                    }
                    fieldList.add(field);
                    rangMap.put(name, fieldList);
                    break;
            }
        }

        if (MapUtils.isNotEmpty(rangMap)) {
            for (Map.Entry<String, List<Field>> entry : rangMap.entrySet()) {
                String fieldName = entry.getKey();
                RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(fieldName);
                for (Field f : entry.getValue()) {
                    f.setAccessible(true);
                    Object rangeValue = f.get(condition);
                    EsItemAspect fa = f.getDeclaredAnnotation(EsItemAspect.class);
                    String op = fa.rangeOp();
                    switch (op) {
                        case "gte" -> rangeQuery.gte(rangeValue);
                        case "lte" -> rangeQuery.lte(rangeValue);
                        case "gt" -> rangeQuery.gt(rangeValue);
                        case "lt" -> rangeQuery.lt(rangeValue);
                    }
                }
                boolBuilder.must(rangeQuery);
            }
        }


    }
}
