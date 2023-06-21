package com.jeesite.modules.cat.service;

import com.jeesite.modules.cat.common.MtxHttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

@Slf4j
@Component
public class FlameHttpService {

    private CloseableHttpClient httpClient;

    @PostConstruct
    public void init() {
        httpClient = MtxHttpClientUtils.getHttpsClient();
    }

    public String doPost(String url, String stringEntity) {

        String method = "POST";
        RequestBuilder builder = RequestBuilder.create(method);
        RequestConfig configBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        builder.setConfig(configBuilder);
        builder.setUri(url);

        HttpEntity httpEntity = new StringEntity(stringEntity, ContentType.create("application/json", Charset.forName("UTF-8")));
        builder.setEntity(httpEntity);

        String resp = null;
        try {
            CloseableHttpResponse response = httpClient.execute(builder.build());
            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            log.error("doPost 请求失败 url {}, stringEntity {} ", url, stringEntity, e);
        }

        return resp;
    }

    public String doGet(String url) {

        String method = "GET";
        RequestBuilder builder = RequestBuilder.create(method);
        RequestConfig configBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        builder.setConfig(configBuilder);
        builder.setUri(url);

        String resp = null;
        try {
            CloseableHttpResponse response = httpClient.execute(builder.build());
            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            log.error("doGet 请求失败 url {}", url, e);
        }

        return resp;
    }

}
