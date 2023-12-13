package com.jeesite.modules.cat.service.interceptor;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SqlUtils {

    public static final String TERM_WHERE = "where";
    public static final String TERM_UPDATE = "update";
    public static final String TERM_SELECT = "select";
    public static final String OP_PLUS = "+";
    public static final String OP_MINUS = "-";

    public static String parseTable(String sql) {
        String[] splits = sql.split(" +");
        if (splits.length < 2) {
            return null;
        }
        return splits[1].trim();
    }

    public static String buildWhereParams(String sql, List<Object> params) {
        String lowerSql = sql.toLowerCase();
        int idx = lowerSql.indexOf(TERM_WHERE);
        // 不处理没有where子句的情况
        if (idx == -1) {
            return null;
        }
        // 获取where字句之后内容
        String where = sql.substring(idx + TERM_WHERE.length()).trim();
        // 将?进行替换
        StringBuilder builder = new StringBuilder();
        int paramIdx = params.size() - 1;
        char last = '_';
        for (int i = where.length() - 1; i >= 0; i--) {
            char c = where.charAt(i);
            // 移除引号，特殊字符
            if (c == '\'' || c == '"' || c == '\n' || c == '\t') {
                continue;
            }
            // 连续空格只保留一个
            if (c == ' ' && c == last) {
                continue;
            }
            if (c == '?') {
                // 越界
                if (paramIdx < 0) {
                    return null;
                }
                String item = String.valueOf(params.get(paramIdx));
                paramIdx -= 1;
                for (int j = item.length() - 1; j >= 0; j--) {
                    builder.append(item.charAt(j));
                }
            } else {
                builder.append(c);
                last = c;
            }
        }
        builder.reverse();
        return builder.toString();
    }

    public static boolean isUpdate(String sql) {
        return sql.toLowerCase().startsWith(TERM_UPDATE);
    }

    public static boolean isSelect(String sql) {
        return sql.toLowerCase().startsWith(TERM_SELECT);
    }

    public static boolean isCountRelated(String sql) {
        return sql.contains(OP_PLUS) || sql.contains(OP_MINUS);
    }

    public static String getDbAddress(String dbUrl) {
        if (dbUrl.contains("//")) {
            dbUrl = StringUtils.substringAfter(dbUrl, "//");
        }
        if (dbUrl.contains("?")) {
            dbUrl = StringUtils.substringBefore(dbUrl, "?");
        }
        if (dbUrl.contains("/")) {
            dbUrl = StringUtils.substringBefore(dbUrl, "/");
        }
        return dbUrl;
    }

    public static boolean isIgnoreOpConfig() {
        String val = System.getProperty("dao.ignore.opconfig");
        return Boolean.parseBoolean(val);
    }
}
