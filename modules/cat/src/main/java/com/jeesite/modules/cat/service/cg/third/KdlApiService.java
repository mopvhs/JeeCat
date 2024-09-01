package com.jeesite.modules.cat.service.cg.third;

import com.jeesite.modules.cat.common.MtxHttpClientUtils;
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

import javax.annotation.PostConstruct;
import java.net.URL;

@Slf4j
@Component
public class KdlApiService {

    @PostConstruct
    public void init() {
        // JDK 8u111版本后，目标页面为HTTPS协议，启用proxy用户密码鉴权
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
    }

    private static String pageUrl = "https://dev.kdlapi.com/testproxy"; // 要访问的目标网页
    private static String proxyIp = "e670.kdltps.com"; // 隧道服务器域名
    private static int proxyPort = 15818; // 端口号
    // 用户名密码, 若已添加白名单则不需要添加
    private static String username = "t12465579219035";
    private static String password = "9uulmf3g";

    /**
     * https://www.kuaidaili.com/doc/api/#2-api
     *
     订单API密钥
     SecretId： osz2yhvax4m5d00caasu
     SecretKey：x7g6jvljm0ixrcetisqv1299omdagsgs

     账户API密钥
     SecretId：u6v7tmg6257ybddsszry
     SecretKey：ldz638j0k9hphgnrrz6bpgvpozpivhct
     */
    public String requestKdl(String pageUrl) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxyIp, proxyPort),
                new UsernamePasswordCredentials(username, password));

        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        try {
            URL url = new URL(pageUrl);
            HttpHost target = new HttpHost(url.getHost(), url.getDefaultPort(), url.getProtocol());
            HttpHost proxy = new HttpHost(proxyIp, proxyPort);

            /*
            httpclient各个版本设置超时都略有不同, 此处对应版本4.5.6
            setConnectTimeout：设置连接超时时间
            setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间
            setSocketTimeout：请求获取数据的超时时间
            */
            RequestConfig config = RequestConfig.custom().setProxy(proxy).setConnectTimeout(6000)
                    .setConnectionRequestTimeout(2000).setSocketTimeout(6000).build();
            HttpGet httpget = new HttpGet(url.getPath());
            httpget.setConfig(config);
            httpget.addHeader("Accept-Encoding", "gzip"); // 使用gzip压缩传输数据让访问更快
            httpget.addHeader("Connection", "close");
            httpget.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36");
            CloseableHttpResponse response = httpclient.execute(target, httpget);
            try {
                String string = httpget.getURI().toString();
                return EntityUtils.toString(response.getEntity());
            } finally {
                response.close();
            }
        } catch (Exception e ) {
            log.error("kdl close httpclient exception e ", e);
        } finally {
            try {
                httpclient.close();
            } catch (Exception e1) {
                log.error("kdl close httpclient exception e1 ", e1);
            }
        }

        return null;
    }
}
