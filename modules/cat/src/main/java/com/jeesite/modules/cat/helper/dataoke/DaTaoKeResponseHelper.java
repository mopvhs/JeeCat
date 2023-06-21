package com.jeesite.modules.cat.helper.dataoke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DaTaoKeResponseHelper {

    // 获取大淘客历史最底价的商品id
    public static List<Long> getHistoryLowPriceIds(String response) {

        if (StringUtils.isBlank(response)) {
            return null;
        }
        List<Long> dtkIds = new ArrayList<>();

        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(response);
        } catch (Exception e) {
            log.error("解析返回结果失败 response:{}", response, e);
        }
        if (jsonObject == null) {
            return null;
        }
        Object list = jsonObject.get("list");
        if (!(list instanceof JSONArray)) {
            return null;
        }
        JSONArray array = (JSONArray) list;
        for (int i = 0; i < array.size(); i++) {
            JSONObject o = array.getJSONObject(i);
            dtkIds.add(o.getLong("id"));
        }

        return dtkIds;
    }

    public static long getHistoryLowPriceTotal(String response) {

        if (StringUtils.isBlank(response)) {
            return 0;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(response);
        } catch (Exception e) {
            log.error("解析返回结果失败 response:{}", response, e);
        }
        if (jsonObject == null) {
            return 0;
        }
        Object list = jsonObject.get("totalNum");
        if (!(list instanceof Number)) {
            return 0;
        }


        return NumberUtils.toLong(String.valueOf(list));
    }
}
