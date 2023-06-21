package com.jeesite.modules.cat.config;

import com.dtk.fetch.client.DtkFetchClient;
import com.dtk.fetch.client.DtkFetchClientFactory;
import com.dtk.fetch.config.DtkFetchConfig;
import com.jeesite.modules.cat.service.cg.DtkConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Resource;

@AutoConfiguration
@DependsOn("dtkConsumer")
public class CatDtkFetchConfig {

    @Resource
    private DtkConsumer dtkConsumer;

    @Value(value = "${dtk.appKey}")
    private String appKey;

    @Value(value = "${dtk.appSecret}")
    private String appSecret;

    @Bean
    public DtkFetchClient createDtkFetchClient() {
        // 数据拉取的对象配置

        DtkFetchConfig fetchConfig = new DtkFetchConfig();

        // 开放平台应用-appKey

        fetchConfig.setAppKey(appKey);

        // 开放平台应用-appSecret

        fetchConfig.setAppSecret(appSecret);

        // 数据拉取的消费对象

        fetchConfig.setConsumer(dtkConsumer);

        // 构建请求客户端对象

        DtkFetchClient fetchClient = DtkFetchClientFactory.getInstance().clientBuild(fetchConfig);

        return fetchClient;
    }
}
