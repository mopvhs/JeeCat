package com.jeesite.modules.cat.service.cg.third.tb;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.common.MtxHttpClientUtils;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class TbApiService {

    @Resource
    private CgUnionProductService cgUnionProductService;

    private static final String vekey = "V73687541H40026415";
    private static final String pid = "mm_30153430_909250463_109464700418";


    /**
     * 淘客产品和官方活动万能转链-升级版
     * @param content
     * @param extraMap
     * @return
     */
    public Result<CommandResponse> getCommonCommand(String content, Map<String, Object> extraMap) {

        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        CloseableHttpClient httpClient = MtxHttpClientUtils.getHttpsClient();
        String res = "";
        String errorMsg = "查询失败";
        try {
            String encode = URLEncoder.encode(content, StandardCharsets.UTF_8);
            String method = "GET";
            // API网址
            String url = "http://api.veapi.cn/tbk/hcapi_v2?vekey=%s&para=%s&pid=%s&";
            url = String.format(url, vekey, encode, pid);

            if (MapUtils.isNotEmpty(extraMap)) {
                String extraUrl = "";
                for (Map.Entry<String, Object> entry : extraMap.entrySet()) {
                    extraUrl += "&" + entry.getKey() + "=" + entry.getValue();
                }
                url += extraUrl;
            }

            RequestBuilder builder = RequestBuilder.create(method);
            RequestConfig configBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
            builder.setConfig(configBuilder);
            builder.setUri(url);


            CloseableHttpResponse response = httpClient.execute(builder.build());
            HttpEntity entity = response.getEntity();
            String resp = EntityUtils.toString(entity, "UTF-8");
            // {"msg":"产品或活动转链失败，请检查参数。淘口令无效或超过有效期了","data":{},"error":"15","request_id":"3mR4Ew6","trans_type":0}
            JSONObject jsonObject = JSONObject.parseObject(resp);
            if (jsonObject != null) {
                int error = NumberUtils.toInt(jsonObject.getString("error"), -1);
                if (error != 0) {
                    return Result.ERROR(error, jsonObject.getString("msg"));
                }

                CommandResponse data = jsonObject.getObject("data", CommandResponse.class);
                if (data != null) {
                    return Result.OK(data);
                }
            }
        } catch (Exception e) {
            log.error("getAuthUrl 获取授权地址失败", e);
        }

        return Result.ERROR(500, errorMsg);
    }
}
