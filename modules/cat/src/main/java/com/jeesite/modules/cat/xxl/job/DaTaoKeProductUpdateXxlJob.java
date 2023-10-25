//package com.jeesite.modules.cat.xxl.job;
//
//import com.xxl.job.core.context.XxlJobHelper;
//import com.xxl.job.core.handler.IJobHandler;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.time.StopWatch;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
//@Slf4j
////@Component
//public class DaTaoKeProductUpdateXxlJob extends IJobHandler {
//
////    @Resource
////    private DtkFetchClient dtkFetchClient;
//
//    @Value(value = "${dtk.appKey}")
//    private String appKey;
//
//    @Value(value = "${dtk.appSecret}")
//    private String appSecret;
//
//    @Override
//    @XxlJob("daTaoKeProductUpdateXxlJob")
//    public void execute() throws Exception {
//        XxlJobHelper.log("daTaoKeProductUpdateXxlJob xxl job start");
//
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
////        try {
////
////            String jobParam = XxlJobHelper.getJobParam();
////            if (StringUtils.isBlank(jobParam)) {
////                return;
////            }
////
////            String serverUrl = "https://openapi.dataoke.com/api/goods/get-goods-details?appKey=%s&version=v1.2.3&id=%d";
////            CloseableHttpClient httpsClient = MtxHttpClientUtils.getHttpsClient();
////
////            String method = "GET";
////            // API网址
////            serverUrl = String.format(serverUrl, appKey, NumberUtils.toInt(jobParam));
////
////            RequestBuilder builder = RequestBuilder.create(method);
////            RequestConfig configBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
////            builder.setConfig(configBuilder);
////
//////        HttpEntity httpEntity = new StringEntity(JSON.toJSONString(params), ContentType.create("application/json", Charset.forName("UTF-8")));
//////        builder.setEntity(httpEntity);
////            String res = "";
////            try {
////                CloseableHttpResponse response = httpClient.execute(builder.build());
////                HttpEntity entity = response.getEntity();
////                String resp = EntityUtils.toString(entity, "UTF-8");
////
////                JSONObject jsonObject = JSONObject.parseObject(resp);
////                if (jsonObject != null) {
////                    JSONObject data = jsonObject.getJSONObject("data");
////                    if (data != null) {
////                        res = data.getString("tbk_pwd");
////                    }
////                }
////            } catch (Exception e) {
////                log.error("getAuthUrl 获取授权地址失败", e);
////            }
////
////        } catch (Exception e) {
////            log.error(e.getMessage(), e);
////        }
//    }
//}
