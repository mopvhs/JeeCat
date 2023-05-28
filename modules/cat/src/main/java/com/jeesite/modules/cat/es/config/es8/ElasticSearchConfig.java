//package com.jeesite.modules.cat.es.config.es8;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.json.jackson.JacksonJsonpMapper;
//import co.elastic.clients.transport.ElasticsearchTransport;
//import co.elastic.clients.transport.rest_client.RestClientTransport;
//import org.apache.http.HttpHost;
//import org.elasticsearch.client.RestClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.context.annotation.Bean;
//
////@AutoConfiguration
//public class ElasticSearchConfig {
//
//    @Value(value = "${elasticsearch.ip}")
//    private String ip;
//
//    @Value(value = "${elasticsearch.port}")
//    private Integer port;
//
//    @Value(value = "${elasticsearch.scheme}")
//    private String scheme;
//
//    @Bean(name = "elasticsearchClient")
//    public ElasticsearchClient initClient() {
//
//        // Create the low-level client
//        RestClient httpClient = RestClient.builder(new HttpHost(ip, port)).build();
//
//        // Create the HLRC
////        RestHighLevelClient hlrc = new RestHighLevelClientBuilder(httpClient)
////                .setApiCompatibilityMode(true)
////                .build();
//
//        // Create the Java API Client with the same low level client
//        ElasticsearchTransport transport = new RestClientTransport(
//                httpClient,
//                new JacksonJsonpMapper()
//        );
//
//        ElasticsearchClient esClient = new ElasticsearchClient(transport);
//        // hlrc and esClient share the same httpClient
//
//        return esClient;
//    }
//}
