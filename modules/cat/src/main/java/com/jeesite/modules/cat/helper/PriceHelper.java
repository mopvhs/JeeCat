package com.jeesite.modules.cat.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public static String formatPrice(Long price, String replaceTarget, String replacement) {
        if (price == null) {
            return "0";
        }

        String format = new BigDecimal(price).divide(new BigDecimal("100"), 2, RoundingMode.UP).toString();
        if (replaceTarget != null && replacement != null) {
//            format = format.replace(".00", "");
            format = format.replace(replaceTarget, replacement);
        }

        return format;
    }
}
