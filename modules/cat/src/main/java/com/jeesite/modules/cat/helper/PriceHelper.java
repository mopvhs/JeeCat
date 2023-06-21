package com.jeesite.modules.cat.helper;

import java.math.BigDecimal;
import java.util.Objects;

public class PriceHelper {

    // 价格处理
    public static long formatPrice(Object obj) {
        if (Objects.isNull(obj)) {
            return 0L;
        }

        BigDecimal price = null;
        if (obj instanceof String) {
            price = new BigDecimal((String) obj);
        } else if (obj instanceof BigDecimal) {
            price = (BigDecimal) obj;
        } else if (obj instanceof Number) {
            price = new BigDecimal((String.valueOf(obj)));
        }

        if (price == null) {
            return 0L;
        }

        return price.multiply(new BigDecimal("100")).longValue();
    }
}
