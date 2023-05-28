package com.jeesite.common.utils;

import com.jeesite.common.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtils {

    /**
     * 获取@的正则，以空格为结尾
     * @return
     */
    public static List<String> matchMention(String input) {

        if (StringUtils.isBlank(input)) {
            return new ArrayList<>();
        }

//        String input = "这是一条@猫车 的测试消息。";

        String regex = "@[\\u4e00-\\u9fa5A-Za-z]+\\s";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        List<String> finds = new ArrayList<>();
        while (matcher.find()) {
            finds.add(matcher.group());
        }

        return finds;
    }
}
