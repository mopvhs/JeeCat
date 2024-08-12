package com.jeesite.modules.cat.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
//@Component
public class OkHttpService {

    private static final OkHttpClient client = new OkHttpClient();


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

}
