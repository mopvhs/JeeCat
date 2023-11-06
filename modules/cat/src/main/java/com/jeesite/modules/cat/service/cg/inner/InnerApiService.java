package com.jeesite.modules.cat.service.cg.inner;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.codec.EncodeUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.service.FlameHttpService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InnerApiService {

    @Resource
    private FlameHttpService flameHttpService;

    // 异步同步淘宝商品
    public Result<String> syncTbProduct(String numIId) {

        String url = "https://wx.mtxtool.com/maoche/add_new_product.html";
        Map<String, String> data = new HashMap<>();
        data.put("url", "https://uland.taobao.com/item/edetail?id=" + numIId);
        String base64 = EncodeUtils.encodeBase64(JsonUtils.toJSONString(data).getBytes());

        Map<String, String> params = new HashMap<>();
        params.put("data", base64);

        // {"success":false,"message":"商品已存在，ID：196154，请勿重复添加！","results":[]}
        String response = flameHttpService.doFormPost(url, params);
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

        String msg = Optional.ofNullable(jsonObject.getString("message")).orElse("解析错误");

        return Result.ERROR(505, msg);
    }



}
