package com.jeesite.modules.cat.service;

import com.jeesite.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.net.URL;

@Slf4j
@Component
public class FlameProxyHttpService {

//    private static String pageUrl = "https://dev.kdlapi.com/testproxy"; // 要访问的目标网页
    private static String proxyIp = "106.12.252.217"; // 代理服务器IP
    private static int proxyPort = 15818; // 端口号
    // 用户名密码认证(私密代理/独享代理)
    private static String username = "t12677002118988";
    private static String password = "Jtyb9DODw7vzYsO";

    /**
     * 代理https
     */
    public Result<String> doGetProxy(String pageUrl) {

//        videoProxyBO.setHost("tps208.kdlapi.com");
//        videoProxyBO.setPort(15818);
//        videoProxyBO.setUsername("t12677002118988");
//        videoProxyBO.setPassword("Jtyb9DODw7vzYsO");

        // JDK 8u111版本后，目标页面为HTTPS协议，启用proxy用户密码鉴权
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope("tps208.kdlapi.com", proxyPort),
                new UsernamePasswordCredentials(username, password));
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        try {
            URL url = new URL(pageUrl);
            HttpHost target = new HttpHost(url.getHost(), url.getDefaultPort(), url.getProtocol());
//            HttpHost proxy = new HttpHost(proxyIp, proxyPort);
            HttpHost proxy = new HttpHost("tps208.kdlapi.com", 15818);

            RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
            HttpGet httpget = new HttpGet(url.getPath());
            httpget.setConfig(config);
            httpget.addHeader("Accept-Encoding", "gzip"); // 使用gzip压缩传输数据让访问更快
            httpget.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36");
            CloseableHttpResponse response = httpclient.execute(target, httpget);
            try {
                String content = EntityUtils.toString(response.getEntity());
                return Result.OK(content);
            } catch (Exception e) {
                log.error("httpclient error", e);
            } finally {
                response.close();
            }
        } catch (Exception e) {
            log.error("httpclient error", e);
        } finally {
            try {
                httpclient.close();
            } catch (Exception e) {
                log.error("httpclient close error", e);
            }
        }

        return Result.ERROR(500, "代理失败");
    }

}
