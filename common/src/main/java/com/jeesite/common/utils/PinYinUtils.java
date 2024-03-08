package com.jeesite.common.utils;

import com.jeesite.common.lang.StringUtils;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

public class PinYinUtils {

    /**
     * 获取首字母，大写
     * @param chinese
     * @return
     */
    public static String getInitial(String chinese) {
        if (StringUtils.isBlank(chinese)) {
            return null;
        }
        String pinyin = toHanYuPinyin(chinese, HanyuPinyinCaseType.UPPERCASE);
        return pinyin.substring(0, 1);
    }

    /**
     * 获取汉语拼音
     * @param chinese
     * @param caseType
     * @return
     */
    public static String toHanYuPinyin(String chinese, HanyuPinyinCaseType caseType) {

        // 创建汉语拼音处理类
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        // 输出设置 大小写 音标方式
        if (caseType == null) {
            caseType = HanyuPinyinCaseType.UPPERCASE;
        }
        format.setCaseType(caseType);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        // 获取汉语拼音
        String pinyinStr = "";
        try {
            pinyinStr = PinyinHelper.toHanYuPinyinString(chinese, format, "", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pinyinStr;
    }
}
