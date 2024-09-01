package com.jeesite.modules.cat.service.cg.third.dto;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShortUrlDetail implements Serializable {

    private static final long serialVersionUID = -4361659073794798489L;

    // 被替换的地址，包括dwz，原始地址
    private String replaceUrl;

    // 解析后的用来搜索的原始地址
    private String searchUrl;

    // 公海消息的原地址
    private String contentUrl;

    // jd商品信息
    private JdUnionIdPromotion promotion;
    // tb商品信息
    private CommandResponseV2 tbProduct;

    // 转链接口是否成功
    private Boolean apiRes;

    // 是否支持转换为短网址dwz
    private boolean supportDwz;
    // 支持短网址的url
    private String supportDwzUrl;

    // 转链历史
    private List<String> exchangeLog;

    public void addExchangeLog(String url) {
        if (StringUtils.isBlank(url)) {
            return;
        }
        if (exchangeLog == null) {
            exchangeLog = new ArrayList<>();
        }

        exchangeLog.add(url);
    }
}
