package com.jeesite.modules.cat.es.config.es7;

import com.jeesite.common.lang.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
public class ElasticSearch7Config {

    @Value(value = "${elasticsearch.ip}")
    private String ip;

    @Value(value = "${elasticsearch.port}")
    private Integer port;

    @Value(value = "${elasticsearch.scheme}")
    private String scheme;

    @Value(value = "${elasticsearch.user}")
    private String user;

    @Value(value = "${elasticsearch.passwd}")
    private String passwd;

    @Bean(name = "restHighLevelClient")
    public RestHighLevelClient initClient() {
        log.info("初始化es7客户端开始");

        CredentialsProvider credentialsProvider = null;
        if (StringUtils.isNotBlank(user)) {
            // 设置验证信息，填写账号及密码
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(user, passwd));
        }

        RestClientBuilder builder = RestClient.builder(new HttpHost(ip, port, scheme));

        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(10000)    // 连接超时（毫秒）
                        .setSocketTimeout(120000)     // 读写超时（毫秒）
                );

        if (credentialsProvider != null) {
            // 设置认证信息
            CredentialsProvider finalCredentialsProvider = credentialsProvider;
            builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    httpClientBuilder.setMaxConnTotal(100);        // 最大连接数
                    httpClientBuilder.setMaxConnPerRoute(50);     // 每路由最大连接数
                    return httpClientBuilder.setDefaultCredentialsProvider(finalCredentialsProvider);
                }
            });
        }
        // 设置超时时间
//        builder.setMaxRetryTimeoutMillis(10000);

        RestHighLevelClient client = new RestHighLevelClient(builder);
        log.info("初始化es7客户端完成");
        return client;
    }
}
