package com.jeesite.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;

import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class UrlUtils {

    public static Map<String, String> getParameters(String urlString) {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        try {
            URL url = new URL(urlString);
            String query = url.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return query_pairs;
    }

    public static void main(String[] args) {
        String url = "https://sup331.kuaizhan.com/?iyunzk=1#/pages/h5?temp=super_page&k=1BHshc ";
        Map<String, String> parametersWithSpilt = getParametersWithSpilt(url);
        System.out.println(JSONObject.toJSONString(parametersWithSpilt));
    }

    public static Map<String, String> getParametersWithSpilt(String urlString) {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        try {
            // 找到第一个?号
            int i = StringUtils.indexOf(urlString, "?");
            if (i > 0) {
                String query = StringUtils.substring(urlString, i + 1);
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return query_pairs;
    }
}
