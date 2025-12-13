package com.jeesite.modules.cat.service;

import com.jeesite.common.collect.MapUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

@Slf4j
//@Component
public class OkHttpService {

    private static final OkHttpClient client = new OkHttpClient();

    // 用户名密码, 若已添加白名单则不需要添加
    private static final String username = "t15608511728842";
    private static final String password = "bbui5s3f";

    private static String ip = "z236.kdltps.com";   // 代理服务器IP
    private static int port = 15818;


    public static HttpUrl doGetHttpUrl(String url) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        HttpUrl resp = null;
        try {
            Response response = client.newCall(request).execute();
            resp = response.request().url();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return resp;
    }


    public static HttpUrl doGetHttpUrlWithProxy(String url) {
        Response response = null;
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
            log.info("doGetHttpUrlWithProxy url:{}", url);
            Authenticator authenticator = new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                }
            };
            OkHttpClient client = new OkHttpClient.Builder()
                    .proxy(proxy)
                    .proxyAuthenticator(authenticator)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3100.0 Safari/537.36")
                    .addHeader("Connection","close")
                    .build();

            response = client.newCall(request).execute();

            return response.request().url();
        } catch (Exception e) {
            log.error("doGetHttpUrlWithProxy exception url:{}", url, e);
        } finally {
            // 确保响应被关闭
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    public static String doGetHtmlWithProxy(String url) {
        Response response = null;

        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));

            Authenticator authenticator = new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                }
            };
            OkHttpClient client = new OkHttpClient.Builder()
                    .proxy(proxy)
                    .proxyAuthenticator(authenticator)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3100.0 Safari/537.36")
                    .addHeader("Connection","close")
                    .build();

            response = client.newCall(request).execute();

            return response.body().string();
        } catch (Exception e) {
            log.error("doGetHttpUrlWithProxy exception url:{}", url, e);
        } finally {
            // 确保响应被关闭
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    public static String doGetHtmlWithProxyHeaders(String url, Map<String, String> headers) {
        Response response = null;

        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));

            Authenticator authenticator = new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                }
            };
            OkHttpClient client = new OkHttpClient.Builder()
                    .proxy(proxy)
                    .proxyAuthenticator(authenticator)
                    .build();

            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3100.0 Safari/537.36")
                    .addHeader("Connection", "close");
            if (MapUtils.isNotEmpty(headers)) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            Request request = builder.build();
            response = client.newCall(request).execute();

            return response.body().string();
        } catch (Exception e) {
            log.error("doGetHttpUrlWithProxy exception url:{}", url, e);
        } finally {
            // 确保响应被关闭
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    public static String doGetHtml(String url) {

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3100.0 Safari/537.36")
                    .addHeader("Connection","close")
                    .build();

            Response response = client.newCall(request).execute();

            return response.body().string();
        } catch (Exception e) {
            log.error("doGetHttpUrlWithProxy exception url:{}", url, e);
        }
        return null;
    }

}
