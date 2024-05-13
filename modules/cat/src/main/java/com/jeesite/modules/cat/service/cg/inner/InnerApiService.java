package com.jeesite.modules.cat.service.cg.inner;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import com.jeesite.common.codec.EncodeUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.FlameProxyHttpService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class InnerApiService {

    @Resource
    private FlameProxyHttpService flameProxyHttpService;

    private RateLimiter proxyUrlLimiter = RateLimiter.create(1);

    // 异步同步淘宝商品
    public Result<String> syncTbProduct(String numIId) {

        String url = "https://wx.mtxtool.com/maoche/add_new_product.html";
        Map<String, String> data = new HashMap<>();
        data.put("url", "https://uland.taobao.com/item/edetail?id=" + numIId);
        String base64 = EncodeUtils.encodeBase64(JsonUtils.toJSONString(data).getBytes());

        Map<String, String> params = new HashMap<>();
        params.put("data", base64);

        // {"success":false,"message":"商品已存在，ID：196154，请勿重复添加！","results":[]}
        String response = FlameHttpService.doFormPost(url, params);
        if (StringUtils.isBlank(response)) {
            return Result.ERROR(500, "接口数据为空");
        }

        log.info("同步淘宝商品接口返回数据：{}", response);

        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject == null) {
            return Result.ERROR(500, "接口数据解析异常");
        }

        Object o = jsonObject.get("success");
        if (o == null) {
            return Result.ERROR(501, "接口数据解析异常");
        }
        Boolean success = (Boolean) o;
        if (BooleanUtil.isTrue(success)) {
            return Result.OK(response);
        }

        Integer code = jsonObject.getInteger("code");
        if (code != null && code == 10086) {
            JSONObject res = jsonObject.getJSONObject("result");
            if (res != null && res.get("id") != null) {
                Result<String> result = Result.OK(String.valueOf(res.get("id")));
                result.setCode(10086);
                return result;
            }
        }

        String msg = Optional.ofNullable(jsonObject.getString("message")).orElse("解析错误");

        return Result.ERROR(505, msg);
    }

    /**
     * 解析淘宝商品，获取商品iid
     */
    public Result<Long> getAnalysisTbIid(String url) {

        if (StringUtils.isBlank(url)) {
            return Result.ERROR(500, "url不能为空");
        }

        // 专门的错误码
        if (!proxyUrlLimiter.tryAcquire()) {
            return Result.ERROR(10300, "请求过于频繁，请稍后再试");
        }

//        String html = flameHttpService.doGet(url);
        Result<String> result = flameProxyHttpService.doGetProxy(url);
        if (!Result.isOK(result)) {
            return Result.ERROR(10500, "查询失败，Proxy异常");
        }
        String html = result.getResult();

        Pattern tbItemDetail = Pattern.compile("(https|https):\\/\\/[a-zA-Z0-9-\\.]+\\.[a-z]{2,}(\\/\\S*)");

        // 解析获取document对象
        Document parse = Jsoup.parse(html);
        // 根据对象的select方法，使用选择器来获取指定的元素，该选择器与CSS及jquery的选择器类似
        String select = "script";
        Elements elements = parse.select(select);

        String matchUrl = "";
        for (Element element : elements) {
            List<Node> nodes = element.childNodes();
            if (CollectionUtils.isEmpty(nodes)) {
                continue;
            }
            for (Node node : nodes) {
                Matcher matcher = tbItemDetail.matcher(node.outerHtml());
                if (matcher.find()) {
                    String group = matcher.group();
                    if (group.contains("item.taobao.com/item.htm")) {
                        matchUrl = group;
                        break;
                    }
                }
            }
            if (StringUtils.isNotBlank(matchUrl)) {
                break;
            }
        }

        // 提取id
        if (StringUtils.isNotBlank(matchUrl)) {
            // 找到?后面的参数
            String[] split = matchUrl.split("\\?");
            if (split.length > 1) {
                String[] params = split[1].split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length > 1 && kv[0].equals("id")) {
                        return Result.OK(NumberUtils.toLong(kv[1]));
                    }
                }
            }
        }

        return Result.ERROR(404, "未找到商品id");
    }



}
