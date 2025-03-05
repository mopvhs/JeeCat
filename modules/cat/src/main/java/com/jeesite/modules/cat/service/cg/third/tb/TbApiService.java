package com.jeesite.modules.cat.service.cg.third.tb;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.common.MtxHttpClientUtils;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponse;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.cg.third.tb.dto.GeneralConvertResp;
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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class TbApiService {

    @Resource
    private CgUnionProductService cgUnionProductService;

    private static final String vekey = "V73687541H40026415";
    private static final String pid = "mm_30153430_909250463_109464700418";

    private static final String specialPid = "mm_30153430_909250463_115891100328";
    private static final Long specialRelationId = 3175101489L;


    /**
     * 淘客产品和官方活动万能转链-升级版
     * @param content
     * @param extraMap
     * @return
     */
    public Result<CommandResponseV2> getCommonCommand(String content, Map<String, Object> extraMap) {

        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        CloseableHttpClient httpClient = MtxHttpClientUtils.getHttpsClient();
        String res = "";
        String errorMsg = "查询失败";
        try {
            extraMap = Optional.ofNullable(extraMap).orElse(new HashMap<>());

            String activityId = decTbUrl4ActivityId(content);
            String encode = URLEncoder.encode(content, StandardCharsets.UTF_8);

            if (StringUtils.isNotBlank(activityId)) {
                extraMap.put("activityId", activityId);
            }
            String method = "GET";
            // API网址
            String url = "http://api.veapi.cn/tbk/hcapi_v2?vekey=%s&para=%s&pid=%s";
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

                CommandResponseV2 data = jsonObject.getObject("data", CommandResponseV2.class);

                if (StringUtils.isNotBlank(data.getTbkPwd())) {
                    String replaceUrl = data.getTbkPwd();
                    // 替换掉一个￥
                    replaceUrl = "(" + replaceUrl.substring(1);
                    // 替换第二个￥
                    replaceUrl = replaceUrl.replace("￥", ")");
                    replaceUrl += "/ CA21,)/ AC01";

                    data.setTbkPwd(replaceUrl);
                }

                return Result.OK(data);
            }

        } catch (Exception e) {
            log.error("getAuthUrl 获取授权地址失败", e);
        }

        return Result.ERROR(500, errorMsg);
    }

    /**
     * https://www.veapi.cn/apidoc/taobaolianmeng/124
     * @param content
     */
    public String decTbUrl4ActivityId(String content) {
        try {
            CloseableHttpClient httpClient = MtxHttpClientUtils.getHttpsClient();

            String encode = URLEncoder.encode(content, StandardCharsets.UTF_8);
            // 口令与链接解密
            String decUrl = "http://api.veapi.cn/tbk/dec?vekey=%s&para=%s&parsetkl=1&pid=%s";
            decUrl = String.format(decUrl, vekey, encode, pid);
            RequestBuilder builder = RequestBuilder.create("GET");
            RequestConfig configBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
            builder.setConfig(configBuilder);
            builder.setUri(decUrl);

            CloseableHttpResponse response = httpClient.execute(builder.build());
            HttpEntity entity = response.getEntity();
            String resp = EntityUtils.toString(entity, "UTF-8");
            // {"msg":"产品或活动转链失败，请检查参数。淘口令无效或超过有效期了","data":{},"error":"15","request_id":"3mR4Ew6","trans_type":0}
            JSONObject jsonObject = JSONObject.parseObject(resp);

            if (jsonObject == null || jsonObject.get("data") == null) {
                return null;
            }

            String decodedQuery = URLDecoder.decode(jsonObject.getString("data"), "UTF-8");

            String[] split = decodedQuery.split("&");
            // 将解析后的键值对存储到Map中
            for (String param : decodedQuery.split("&")) {
                String[] pair = param.split("=");
                String key = pair[0];
                if ("activityId".equals(key)) {
                    String value = pair.length > 1 ? pair[1] : "";
                    return value;
                }
            }


        } catch (Exception e) {
            log.error("decTbUrl 解密失败", e);
        }
        return null;
    }

    public static void main(String[] args) {
//        String content = "6$H9WLe8NUQ0Q$:// ZH9114";
//        System.out.println(generalConvert(content, null));
    }


    public Result<GeneralConvertResp> generalConvert(String content, Map<String, Object> extraMap) {

        // https://www.veapi.cn/apidoc/taobaolianmeng/283
        CloseableHttpClient httpClient = MtxHttpClientUtils.getHttpsClient();
        String res = "";
        String errorMsg = "查询失败";
        try {
            extraMap = Optional.ofNullable(extraMap).orElse(new HashMap<>());
            String encode = URLEncoder.encode(content, StandardCharsets.UTF_8);
            String method = "GET";
            // API网址
            String url = "http://api.veapi.cn/tbk/generalconvert?vekey=%s&para=%s&pid=%s&relation_id=%d&detail=2";
            url = String.format(url, vekey, encode, specialPid, specialRelationId);

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

                // 是否有券：1该产品有优惠券，2该产品无券,0表示当前为非商品（比如店铺转链或会场转链）【说明】本字段值1或2决定了下面coupon_short_url或coupon_long_url二合一链接，还是cps_short_url 或 cps_long_url直跳详情页的链接，您要用哪一个链接跳转。
                Integer result = jsonObject.getInteger("result");
                GeneralConvertResp data = jsonObject.getObject("data", GeneralConvertResp.class);

                String tbkPwd = data.getCouponShortTpwd();
                if (result == 2) {
                    tbkPwd = data.getCpsShortTpwd();
                }
                if (StringUtils.isBlank(tbkPwd)) {
                    tbkPwd = data.getCpsShortTpwd();
                }

                if (StringUtils.isNotBlank(tbkPwd)) {
                    String replaceUrl = tbkPwd;
                    // 替换掉一个￥
                    replaceUrl = "(" + replaceUrl.substring(1);
                    // 替换第二个￥
                    replaceUrl = replaceUrl.replace("￥", ")");
                    replaceUrl += "/ CA21,)/ AC01";

                    data.setTbkPwd(replaceUrl);
                }

                return Result.OK(data);
            }
        } catch (Exception e) {
            log.error("getAuthUrl 获取授权地址失败", e);
        }

        return Result.ERROR(500, errorMsg);
    }
}
