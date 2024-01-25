package com.jeesite.modules.cat.es.config.es7;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchResponseSections;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@DependsOn(value = "elasticSearch7Config")
public class ElasticSearch7Service {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    private static ExecutorService executor = null;

    @PostConstruct
    public void init() {
        executor = new ThreadPoolExecutor(5, 20,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("es-executor"));
    }

    /**
     * 查询
     * @param searchSourceBuilder
     * @param indexEnum
     * @param converter
     * @return
     * @param <R>
     */
    public <R, A> ElasticSearchData<R, A> search(SearchSourceBuilder searchSourceBuilder,
                                           ElasticSearchIndexEnum indexEnum,
                                           AggregationBuilder aggregation,
                                           Function<String, R> converter,
                                           Function<Aggregations, Map<String, List<A>>> bucketConverter) {

        searchSourceBuilder.sort("_score", SortOrder.DESC);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexEnum.getIndex());
        searchRequest.types(indexEnum.getType());
        searchRequest.source(searchSourceBuilder);
        if (aggregation != null) {
            searchSourceBuilder.aggregation(aggregation);
        }

        try {
//            log.info("es search indexEnum {}, source :{}", indexEnum.getIndex(), searchSourceBuilder.toString());
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse == null) {
                return null;
            }
            SearchResponseSections internalResponse = searchResponse.getInternalResponse();
            if (internalResponse == null) {
                return null;
            }
            SearchHits searchHits = internalResponse.hits();
            ElasticSearchData<R, A> response = new ElasticSearchData<>();
            List<R> hits = new ArrayList<>();

            for (SearchHit hit : searchHits.getHits()) {
                R apply = converter.apply(hit.getSourceAsString());
                if (apply != null) {
                    hits.add(apply);
                }
            }

            Map<String, List<A>> aggBucketMap = new HashMap<>();
            if (bucketConverter != null && internalResponse.aggregations() != null) {
                Map<String, List<A>> apply = bucketConverter.apply(internalResponse.aggregations());
                if (MapUtils.isNotEmpty(apply)) {
                    aggBucketMap = apply;
                }
            }

            long total = 0;
            TotalHits totalHits = searchHits.getTotalHits();
            if (totalHits != null) {
                total = totalHits.value;
                if (total >= 10000) {
                    CountRequest countRequest = new CountRequest();
                    countRequest.query(searchSourceBuilder.query());
                    countRequest.indices(indexEnum.getIndex());
                    countRequest.types(indexEnum.getType());
                    CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                    if (count != null) {
                        total = count.getCount();
                    }
                }
            }

            response.setTotal(total);
            response.setDocuments(hits);
            response.setBucketMap(aggBucketMap);

            return response;
        } catch (Exception e) {
            log.error("查询ElasticSearch7Service查询异常，searchRequest:{}, indexEnum：{}", JSON.toJSONString(searchRequest), JSON.toJSONString(indexEnum), e);
        }

        return null;
    }

    /**
     * 查询
     * @param searchSourceBuilder
     * @param indexEnum
     * @param converter
     * @return
     * @param <R>
     */
    public <R, A> ElasticSearchData<R, A> search(SearchSourceBuilder searchSourceBuilder,
                                                 ElasticSearchIndexEnum indexEnum,
                                                 Function<String, R> converter,
                                                 Function<Aggregations, Map<String, List<A>>> bucketConverter) {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexEnum.getIndex());
        searchRequest.types(indexEnum.getType());
        searchRequest.source(searchSourceBuilder);

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            stopWatch.stop();
//            log.info("es search2 indexEnum {}, time:{}, source :{}", indexEnum.getIndex(), stopWatch.toString(), searchSourceBuilder.toString());

            if (searchResponse == null) {
                return null;
            }
            SearchResponseSections internalResponse = searchResponse.getInternalResponse();
            if (internalResponse == null) {
                return null;
            }
            SearchHits searchHits = internalResponse.hits();
            ElasticSearchData<R, A> response = new ElasticSearchData<>();
            List<R> hits = new ArrayList<>();

            for (SearchHit hit : searchHits.getHits()) {
                R apply = converter.apply(hit.getSourceAsString());
                if (apply != null) {
                    hits.add(apply);
                }
            }

            Map<String, List<A>> aggBucketMap = new HashMap<>();
            if (bucketConverter != null && internalResponse.aggregations() != null) {
                Map<String, List<A>> apply = bucketConverter.apply(internalResponse.aggregations());
                if (MapUtils.isNotEmpty(apply)) {
                    aggBucketMap = apply;
                }
            }

            long total = 0;
            TotalHits totalHits = searchHits.getTotalHits();
            if (totalHits != null) {
                total = totalHits.value;
                if (total >= 10000) {
                    CountRequest countRequest = new CountRequest();
                    countRequest.query(searchSourceBuilder.query());
                    countRequest.indices(indexEnum.getIndex());
                    countRequest.types(indexEnum.getType());
                    CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                    if (count != null) {
                        total = count.getCount();
                    }
                }
            }

            response.setTotal(total);
            response.setDocuments(hits);
            response.setBucketMap(aggBucketMap);

            return response;
        } catch (Exception e) {
            log.error("查询ElasticSearch7Service2查询异常，searchRequest:{}, indexEnum：{}", JSON.toJSONString(searchRequest), JSON.toJSONString(indexEnum), e);
        }

        return null;
    }

    /**
     * 写入数据
     * @param data
     * @param indexEnum
     * @param resourceId
     */
    public void index(Map<String, Object> data, ElasticSearchIndexEnum indexEnum, String resourceId) {
        if (data == null || data.size() <= 0) {
            return;
        }

        if (StringUtils.isBlank(resourceId)) {
            return;
        }

        IndexRequest indexRequest = new IndexRequest(indexEnum.getIndex(), indexEnum.getType(), resourceId);
        indexRequest.source(data);

        try {
            IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

            DocWriteResponse.Result result = response.getResult();
//            log.info("indexEs resourceId {}, indexEnum {}, result {}",resourceId, indexEnum.name(), JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("indexEs error data {}, resourceId {}", JSON.toJSONString(data), resourceId, e);
        }
    }

    /**
     * 写入数据
     * @param data
     * @param indexEnum
     * @param resourceIdKey
     */
    public void index(List<Map<String, Object>> data, ElasticSearchIndexEnum indexEnum, String resourceIdKey, int limit) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }

        if (StringUtils.isBlank(resourceIdKey)) {
            return;
        }

        // 每执行limit条等待
        List<List<Map<String, Object>>> partition = Lists.partition(data, limit);
        for (List<Map<String, Object>> list : partition) {

            List<Future<Boolean>> futures = new ArrayList<>();
            for (Map<String, Object> item : list) {
                String id = String.valueOf(item.get(resourceIdKey));

                IndexRequest indexRequest = new IndexRequest(indexEnum.getIndex(), indexEnum.getType(), id);
                indexRequest.source(item);
                Future<Boolean> submit = executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                            DocWriteResponse.Result result = response.getResult();
//                            log.info("indexEs resourceId {}, indexEnum {}, result {}", id, indexEnum.getIndex(), JSON.toJSONString(result));
                        } catch (Exception e) {
                            log.error("indexEs error data {}, resourceId {}", JSON.toJSONString(data), id, e);
                            // 异常的话，再执行一次
                            try {
                                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                            } catch (Exception ee) {
                                log.error("indexEs error second times data {}, resourceId {}", JSON.toJSONString(data), id, ee);

                            }
                            return false;
                        }

                        return true;
                    }
                });
                futures.add(submit);
            }

            for (Future<Boolean> f : futures) {
                try {
                    f.get();
                } catch (Exception e) {
                    log.error("写入异常");
                }
            }
        }
    }

    /**
     * 更新
     * @param data
     * @param indexEnum
     * @param resourceIdKey
     */
    public void update(List<Map<String, Object>> data, ElasticSearchIndexEnum indexEnum, String resourceIdKey, int limit) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }

        if (StringUtils.isBlank(resourceIdKey)) {
            return;
        }

        // 每执行limit条等待
        List<List<Map<String, Object>>> partition = Lists.partition(data, limit);
        for (List<Map<String, Object>> list : partition) {

            List<Future<Boolean>> futures = new ArrayList<>();
            for (Map<String, Object> item : list) {
                String id = String.valueOf(item.get(resourceIdKey));

                UpdateRequest updateRequest = new UpdateRequest(indexEnum.getIndex(), indexEnum.getType(), id);
                updateRequest.doc(item);
                Future<Boolean> submit = executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            UpdateResponse response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
                            DocWriteResponse.Result result = response.getResult();
//                            log.info("indexEs resourceId {}, indexEnum {}, result {}", id, indexEnum.getIndex(), JSON.toJSONString(result));
                        } catch (Exception e) {
                            log.error("update es error data {}, resourceId {}", JSON.toJSONString(data), id, e);

                            return false;
                        }

                        return true;
                    }
                });
                futures.add(submit);
            }

            for (Future<Boolean> f : futures) {
                try {
                    f.get();
                } catch (Exception e) {
                    log.error("更新异常");
                }
            }
        }
    }

    /**
     * 删除索引
     * @param ids
     * @param indexEnum
     */
    public void delIndex(List<Long> ids, ElasticSearchIndexEnum indexEnum) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        ids = ids.stream().distinct().collect(Collectors.toList());

        List<Future<Boolean>> futures = new ArrayList<>();
            for (Long id : ids) {
                Future<Boolean> submit = executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            DeleteRequest deleteRequest = new DeleteRequest(indexEnum.getIndex());
                            deleteRequest.id(String.valueOf(id));
                            DeleteResponse delete = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
//                            log.info("del index resourceId {}, indexEnum {}, result {}", id, indexEnum.getIndex(), JSON.toJSONString(delete));
                        } catch (Exception e) {
                            log.error("del index error resourceId {}", id, e);
                            return false;
                        }

                        return true;
                    }
                });
                futures.add(submit);
            }

            for (Future<Boolean> f : futures) {
                try {
                    f.get();
                } catch (Exception e) {
                    log.error("删除异常");
                }
            }
        }

    /**
     * 删除索引
     * @param indexEnum
     */
    public GetMappingsResponse getIndexMapping(ElasticSearchIndexEnum indexEnum) {
        try {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(indexEnum.getIndex());
            GetMappingsResponse mapping = restHighLevelClient.indices().getMapping(request, RequestOptions.DEFAULT);

            return mapping;
        } catch (Exception e) {
            log.error("", e);
        }

        return null;
    }

}
