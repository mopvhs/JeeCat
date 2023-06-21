package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Data
public class ProductScoreModel implements Serializable {

    @Serial
    private static final long serialVersionUID = -4110950663055724939L;

    // 描述分
    private Long descScore;

    // 描述相符
    private Long dsrScore;

    // 描述同行比
    private Long dsrPercent;

    // 物流服务
    private Long shipScore;

    // 物流同行比
    private Long shipPercent;

    // 服务态度
    private Long serviceScore;

    // 服务同行比
    private Long servicePercent;

    // 热推值
    private Long hotPush;

    /**
     * 扩大10000倍
     * @param score
     * @return
     */
    public static Long formatScore(Object score) {
        if (score == null) {
            return 0L;
        }

        BigDecimal bd = null;
        if (score instanceof BigDecimal) {
            bd = (BigDecimal) score;
        } else {
            bd = new BigDecimal(String.valueOf(score));
        }

        return bd.multiply(new BigDecimal("10000")).longValue();
    }

}
