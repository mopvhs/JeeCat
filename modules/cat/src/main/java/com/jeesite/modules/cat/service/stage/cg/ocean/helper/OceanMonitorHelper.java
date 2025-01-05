package com.jeesite.modules.cat.service.stage.cg.ocean.helper;

import com.jeesite.common.utils.DateTimeUtils;
import lombok.Getter;

import java.util.Date;

public class OceanMonitorHelper {

    /**
     * 机器人采集数量
     * @param affType
     * @return
     */
    public static String getRobotMsgNumKey(String affType) {
        String date = DateTimeUtils.getStringDateShort(new Date());

        return "robot_msg_affNum_" + date + "_" + affType;
    }

    /**
     * 机器人采集数量相同
     * @param affType
     * @return
     */
    public static String getRobotMsgSameNumKey(String affType) {
        String date = DateTimeUtils.getStringDateShort(new Date());

        return "robot_same_msg_affNum_" + date + "_" + affType;
    }

    // 淘宝 & 京东单子次数
    public static String getOceanNumKey(String affType) {
        String date = DateTimeUtils.getStringDateShort(new Date());

        return "affNum_" + date + "_" + affType;
    }

    // 淘宝 & 京东链接次数
    public static String getOceanUrlSizeKey(String affType) {
        String date = DateTimeUtils.getStringDateShort(new Date());

        return "urlSize_" + date + "_" + affType;
    }

    // 淘宝 & 京东 耗时区间
    public static String getOceanTsKey(String affType, long ts) {
        String date = DateTimeUtils.getStringDateShort(new Date());
        OceanTimeRangeEnum range = OceanTimeRangeEnum.getRange(ts);

        return OceanTimeRangeEnum.getRedisKey(affType, date, range);
    }

    // 淘宝 & 京东 总耗时
    public static String getOceanTsTotalKey(String affType) {
        String date = DateTimeUtils.getStringDateShort(new Date());

        return "tsTotal_" + date + "_" +  affType;
    }

    @Getter
    public enum OceanTimeRangeEnum {

        TS_100("0_100", 0, 100L),
        TS_500("100_500", 100L, 500L),
        TS_1000("500_1000", 500L, 1000L),
        TS_2000("1000_2000", 1000L, 2000L),
        TS_4000("2000_4000", 2000L, 4000L),
        TS_MAX("4000_max", 4000L, 99999999999L),

        ;

        private String key;

        private long tsMin;

        private long tsMax;

        OceanTimeRangeEnum(String key, long tsMin, long tsMax) {
            this.key = key;
            this.tsMin = tsMin;
            this.tsMax = tsMax;
        }

        public static OceanTimeRangeEnum getRange(long ts) {

            for (OceanTimeRangeEnum obe : values()) {
                if (obe.tsMin <= ts && ts < obe.tsMax) {
                    return obe;
                }
            }

            return OceanTimeRangeEnum.TS_MAX;
        }

        public static String getRedisKey(String affType, String date, OceanTimeRangeEnum rangeEnum) {
            return  "singleTsKey_" + date + "_" + affType + "_" + rangeEnum.getKey();
        }
    }
}
