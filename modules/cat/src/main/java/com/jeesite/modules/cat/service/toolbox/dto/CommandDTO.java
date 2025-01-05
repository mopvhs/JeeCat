
package com.jeesite.modules.cat.service.toolbox.dto;

import com.jeesite.common.collect.MapUtils;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class CommandDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1079339594295738996L;

    private String content;

    private List<Product> products;

    public Map<String, ShortUrlDetail> shortUrlDetailMap;

    @Data
    public static class Product implements Serializable {
        @Serial
        private static final long serialVersionUID = -2016623871274343829L;

        private String itemUrl;

        private String command;

        private List<String> couponUrls;

        private String couponUrl;

        private Item item;
    }

    @Data
    public static class Item implements Serializable {

        @Serial
        private static final long serialVersionUID = -8745486897501280486L;

        private Long id;

        private String numIid;

        private String image;

        // 到手价
        private Long reservePrice;

        // 原价
        private Long originalPrice;

        // 店铺dsr
        private Long shopDsr;

        // 月销量
        private Long volume;

        // 商品标题
        private String title;

        // 佣金率
        private Long commissionRate;

        // 佣金率
        private Long commission;

        // 店铺名称
        private String shopTitle;

    }
}
