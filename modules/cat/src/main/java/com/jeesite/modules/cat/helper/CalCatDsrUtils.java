package com.jeesite.modules.cat.helper;

import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;

import java.math.BigDecimal;

/**
 * https://docs.qq.com/doc/DQkFHSmx4dWlWRm1G
 */
public class CalCatDsrUtils {


    public static long calCarRate(Long shopDsr, Long volume, Long creditLevel, String fans, Long commissionRate) {
        int shopDsrFactor = getShopDsrFactor(shopDsr);
        int volumeFactor = getProductVolumeFactor(volume);
        int creditLevelFactor = getProductCreditLevelFactor(creditLevel);
        int fansFactor = getProductFansFactor(fans);
        int commissionRateFactor = getProductCommissionRateFactor(commissionRate);

        // 评分公式：（P1=0.3*店铺评分A +0.3*月销量C +0.1*店铺等级D+0.15*店铺粉丝数E+0.15*佣金率F ）
        long catRate = new BigDecimal("0.3").multiply(new BigDecimal(shopDsrFactor)).longValue() +
                new BigDecimal("0.3").multiply(new BigDecimal(volumeFactor)).longValue() +
                new BigDecimal("0.1").multiply(new BigDecimal(creditLevelFactor)).longValue() +
                new BigDecimal("0.15").multiply(new BigDecimal(fansFactor)).longValue() +
                new BigDecimal("0.15").multiply(new BigDecimal(commissionRateFactor)).longValue();

        return Math.min(catRate, 50000);
    }

    /**
     * 评分公式：（P1=0.4*A + 0.6*B  ）
     * 例：某店铺评分4.89，商品好评率99%，则：[ （P1=0.4*4.6 + 0.6*4.9 ） ] =4.78
     *
     * ┃A ┃【店铺评分】
     * all≥5.0	                    5
     * 4.99>all>4.95	    4.8
     * 4.94>all>4.90	    4.6
     * 4.89>all>4.85	    4.4
     * 4.84>all>4.82	    4.2
     * 4.81>all>4.8	           4
     *
     * ┃B ┃【商品好评率】
     * 100%		5
     * 99%		4.9
     * 98%		4.7
     * 97%		4.5
     * 96%		4.3
     * 95%  	       4
     */



    public static int getShopDsrFactor(Long shopDsr) {
        if (shopDsr == null) {
            return 30000;
        }
        if (shopDsr >= 50000) {
            return 50000;
        } else if (shopDsr >= 49500) {
            return 48000;
        } else if (shopDsr >= 49000) {
            return 46000;
        } else if (shopDsr >= 48500) {
            return 44000;
        } else if (shopDsr >= 48200) {
            return 42000;
        } else if (shopDsr >= 48000) {
            return 44000;
        } else {
            return 30000;
        }
    }

    public static int getProductVolumeFactor(Long goodRate) {
        if (goodRate == null) {
            return 30000;
        }
        if (goodRate >= 50000) {
            return 50000;
        } else if (goodRate >= 40000) {
            return 49000;
        } else if (goodRate >= 30000) {
            return 48000;
        } else if (goodRate >= 20000) {
            return 47000;
        } else if (goodRate >= 5000) {
            return 45000;
        } else if (goodRate >= 2000) {
            return 44000;
        } else if (goodRate >= 1000) {
            return 42000;
        } else if (goodRate >= 500) {
            return 41000;
        } else if (goodRate >= 100) {
            return 40000;
        } else {
            return 30000;
        }
    }

    public static int getProductGoodRateFactor(Integer goodRate) {
        if (goodRate == null) {
            return 30000;
        }
        if (goodRate >= 50000) {
            return 50000;
        } else if (goodRate >= 49500) {
            return 48000;
        } else if (goodRate >= 49000) {
            return 46000;
        } else if (goodRate >= 48500) {
            return 44000;
        } else if (goodRate >= 48200) {
            return 42000;
        } else if (goodRate >= 48000) {
            return 44000;
        } else {
            return 30000;
        }
    }

    public static int getProductCommissionRateFactor(Long goodRate) {
        if (goodRate == null) {
            return 30000;
        }

        if (goodRate >= 3000) {
            return 50000;
        } else if (goodRate >= 2000) {
            return 49000;
        } else if (goodRate >= 1000) {
            return 48500;
        } else if (goodRate >= 500) {
            return 47500;
        } else if (goodRate >= 300) {
            return 45000;
        } else if (goodRate >= 200) {
            return 44000;
        }

        return 30000;
    }

    public static int getProductCreditLevelFactor(Long creditLevel) {
        if (creditLevel == null) {
            return 10000;
        }

        int factor = 10000;
        if (creditLevel == 20L) {
            factor = 50000;
        } else if (creditLevel == 19L) {
            factor = 49500;
        } else if (creditLevel == 18L) {
            factor = 49000;
        } else if (creditLevel == 17L) {
            factor = 48500;
        } else if (creditLevel == 16L) {
            factor = 48000;
        } else if (creditLevel == 15L) {
            factor = 47000;
        } else if (creditLevel == 14L) {
            factor = 46000;
        } else if (creditLevel == 13L) {
            factor = 44000;
        } else if (creditLevel == 12L) {
            factor = 42000;
        } else if (creditLevel == 11L) {
            factor = 40000;
        } else if (creditLevel == 10L) {
            factor = 30000;
        } else if (creditLevel == 9L) {
            factor = 28000;
        } else if (creditLevel == 8L) {
            factor = 26000;
        } else if (creditLevel == 7L) {
            factor = 24000;
        } else if (creditLevel == 6L) {
            factor = 22000;
        } else if (creditLevel == 5L) {
            factor = 20000;
        } else if (creditLevel == 4L) {
            factor = 18000;
        } else if (creditLevel == 3L) {
            factor = 16000;
        } else if (creditLevel == 2L) {
            factor = 13000;
        } else if (creditLevel == 1L) {
            factor = 10000;
        }

        return factor;
    }

    // fans: "29.5万"
    public static int getProductFansFactor(String fans) {

        long fansNum = 0;
        if (StringUtils.contains(fans, "万")) {
            fans = StringUtils.replace(fans, "万", "");
            fansNum = new BigDecimal(fans).multiply(new BigDecimal("10000")).longValue();
        } else {
            fansNum = NumberUtils.toLong(fans);
        }

        if (fansNum >= 10000000) {
            return 50000;
        } else if (fansNum >= 5000000) {
            return 49900;
        } else if (fansNum >= 2000000) {
            return 49500;
        } else if (fansNum >= 1000000) {
            return 49000;
        } else if (fansNum >= 500000) {
            return 48500;
        } else if (fansNum >= 200000) {
            return 48000;
        } else if (fansNum >= 100000) {
            return 47500;
        } else if (fansNum >= 50000) {
            return 47000;
        } else if (fansNum >= 20000) {
            return 45000;
        } else if (fansNum >= 10000) {
            return 43000;
        }

        return 30000;
    }
}