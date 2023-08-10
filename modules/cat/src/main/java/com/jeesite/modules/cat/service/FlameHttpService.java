package com.jeesite.modules.cat.service;

import cn.hutool.http.body.RequestBody;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.common.MtxHttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public String doFormPost(String url, Map<String, String> params) {

        HttpPost post = new HttpPost(url);
        post.addHeader("Content-type", "application/x-www-form-urlencoded; Charset=utf-8");

        List<NameValuePair> pairList = new ArrayList<>();
        if (MapUtils.isNotEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                pairList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        String resp = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));
            CloseableHttpResponse response = httpClient.execute(post);

            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            log.error("doFormPost 请求失败 url {}, stringEntity {} ", url, JsonUtils.toJSONString(params), e);
        }

        return resp;
    }

    public String doUploadFilePost(String url, File file) {

        HttpPost post = new HttpPost(url);
        post.addHeader("Content-type", "image/png; multipart/form-data; Charset=utf-8");

        FileBody fileBody = new FileBody(file);

        HttpEntity requestEntity = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .build();
        post.setEntity(requestEntity);

        String resp = null;
        try {
            CloseableHttpResponse response = httpClient.execute(post);

            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            log.error("doUploadFilePost 请求失败 url {}", url, e);
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
