package com.jeesite.modules.cat.xxl.job;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.PageUtil;
import com.alibaba.fastjson.JSONObject;
import com.dtk.fetch.client.DtkFetchClient;
import com.dtk.fetch.client.DtkFetchClientUtil;
import com.dtk.fetch.exception.DtkExceptionEnum;
import com.dtk.fetch.request.DtkGoodsInvalidRequest;
import com.dtk.fetch.request.DtkGoodsListRequest;
import com.dtk.fetch.request.DtkGoodsNewestRequest;
import com.dtk.fetch.request.DtkGoodsUpdateRequest;
import com.dtk.fetch.request.DtkPageRequest;
import com.dtk.fetch.response.AbstractDtkPageResponse;
import com.dtk.fetch.response.DtkGoodsInvalidResponse;
import com.dtk.fetch.response.DtkGoodsListResponse;
import com.dtk.fetch.response.DtkGoodsUpdateResponse;
import com.dtk.fetch.response.DtkPageResponse;
import com.dtk.fetch.util.JsonUtil;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.modules.cat.common.MtxHttpClientUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@Component
public class DaTaoKeProductUpdateXxlJob extends IJobHandler {

    @Resource
    private DtkFetchClient dtkFetchClient;

    @Value(value = "${dtk.appKey}")
    private String appKey;

    @Value(value = "${dtk.appSecret}")
    private String appSecret;

    @Override
    @XxlJob("daTaoKeProductUpdateXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("daTaoKeProductUpdateXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
//        try {
//
//            String jobParam = XxlJobHelper.getJobParam();
//            if (StringUtils.isBlank(jobParam)) {
//                return;
//            }
//
//            String serverUrl = "https://openapi.dataoke.com/api/goods/get-goods-details?appKey=%s&version=v1.2.3&id=%d";
//            CloseableHttpClient httpsClient = MtxHttpClientUtils.getHttpsClient();
//
//            String method = "GET";
//            // API网址
//            serverUrl = String.format(serverUrl, appKey, NumberUtils.toInt(jobParam));
//
//            RequestBuilder builder = RequestBuilder.create(method);
//            RequestConfig configBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
//            builder.setConfig(configBuilder);
//
////        HttpEntity httpEntity = new StringEntity(JSON.toJSONString(params), ContentType.create("application/json", Charset.forName("UTF-8")));
////        builder.setEntity(httpEntity);
//            String res = "";
//            try {
//                CloseableHttpResponse response = httpClient.execute(builder.build());
//                HttpEntity entity = response.getEntity();
//                String resp = EntityUtils.toString(entity, "UTF-8");
//
//                JSONObject jsonObject = JSONObject.parseObject(resp);
//                if (jsonObject != null) {
//                    JSONObject data = jsonObject.getJSONObject("data");
//                    if (data != null) {
//                        res = data.getString("tbk_pwd");
//                    }
//                }
//            } catch (Exception e) {
//                log.error("getAuthUrl 获取授权地址失败", e);
//            }
//
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
    }
}
