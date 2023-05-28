//package com.jeesite.modules.cat.es.config.es8;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.SortOptions;
//import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
//import co.elastic.clients.elasticsearch.core.IndexRequest;
//import co.elastic.clients.elasticsearch.core.IndexResponse;
//import co.elastic.clients.elasticsearch.core.SearchRequest;
//import co.elastic.clients.elasticsearch.core.SearchResponse;
//import co.elastic.clients.elasticsearch.core.search.Hit;
//import co.elastic.clients.util.ObjectBuilder;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.DependsOn;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Function;
//
////@Slf4j
////@Component
////@DependsOn("elasticSearchConfig")
//public class SearchService {
//
//
//    @Resource
//    private ElasticsearchClient elasticsearchClient;
//
//    public <T, C> List<C> search(String index,
//                                 Class<T> clazz,
//                                 Function<Query.Builder, ObjectBuilder<Query>> query,
//                                 Function<SortOptions.Builder, ObjectBuilder<SortOptions>> sort,
//                                 Integer from,
//                                 Integer size,
//                                 Function<T, C> converter) throws IOException {
//
//        SearchResponse<T> search = elasticsearchClient.search(s -> s
//                        .index(index)
//                        .sort(sort)
//                        .from(from)
//                        .size(size)
//                        .query(query),
//                clazz);
//
//        List<C> hits = new ArrayList<>();
//        for (Hit<T> hit: search.hits().hits()) {
//            C apply = converter.apply(hit.source());
//            if (apply != null) {
//                hits.add(apply);
//            }
//        }
//
//        return hits;
//    }
//
//    public void searchCondition(String index) {
//        SearchRequest.Builder builder = new SearchRequest.Builder();
//
//        Query toQuery = MatchQuery.of(r -> r
//                .field("msg")
//                .query("猫砂")
//        )._toQuery();
//
//        SearchRequest request =  builder.build();
//
//
//
//    }
//
//
//    public <TDocument> IndexResponse index(String index, String id, TDocument document) throws IOException {
////        TDocument document = converter.apply(param);
//
//        IndexRequest.Builder<TDocument> builder = new IndexRequest.Builder<>();
//        builder.index(index)
//                .id(id)
//                .document(document);
//
//        IndexRequest<TDocument> indexRequest = builder.build();
//
//        IndexResponse response = elasticsearchClient.index(indexRequest);
//
//        return response;
//    }
//
//
//
//}
