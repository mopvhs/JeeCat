package com.jeesite.modules.cat.service.interceptor;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SqlVisitorHelper {

    public static SQLUtils.FormatOption FORMAT = new SQLUtils.FormatOption(true, false);

    private static final int MAX_SIZE = 1000;

    private static ConcurrentHashMap<String, Pair<String, SqlInfo>> stripedMap = new ConcurrentHashMap<>();

    private final static SQLParserFeature[] DEFAULT_FEATURE = {
            SQLParserFeature.EnableSQLBinaryOpExprGroup,
            SQLParserFeature.UseInsertColumnsCache,
            SQLParserFeature.OptimizedForParameterized,
            SQLParserFeature.OptimizedForForParameterizedSkipValue,
    };

    public static SQLASTOutputVisitor buildCustomSqlOutputVisitor(StringBuilder out, String sql) {
        // todo yhq
        //        boolean needIntercept = SqlConfig.getInstance().isNeedWork();
        boolean needIntercept = true;
        SQLASTOutputVisitor visitor = new CustomSqlOutputVisitor(out, sql, needIntercept);
        decorateSqlVisitor(visitor, false);
        return visitor;
    }

    public static SQLASTOutputVisitor buildCustomSqlOutputVisitor(StringBuilder out, String sql, boolean parameterized) {
//        boolean needIntercept = SqlConfig.getInstance().isNeedWork();
        // todo yhq
        boolean needIntercept = true;
        SQLASTOutputVisitor visitor = new CustomSqlOutputVisitor(out, sql, parameterized, needIntercept);
        decorateSqlVisitor(visitor, parameterized);
        return visitor;
    }

    public static SQLASTVisitorAdapter buildCustomSqlVisitorAdapter(Map<String, String> tableMap, boolean select) {
        return new CustomSqlVisitorAdapter(tableMap, select);
    }

    private static void decorateSqlVisitor(SQLASTOutputVisitor visitor, boolean parameterized) {
        SQLUtils.FormatOption option = FORMAT;
        visitor.setUppCase(option.isUppCase());
        visitor.setPrettyFormat(option.isPrettyFormat());
        visitor.setParameterized(option.isParameterized() || parameterized);
    }

    public static String formatStatement(String sql, SQLStatement statement) {
        StringBuilder out = new StringBuilder();
        SQLASTOutputVisitor visitor = buildCustomSqlOutputVisitor(out, sql);
        statement.accept(visitor);
        return out.toString();
    }

    public static String stripSql(String sql) {
        Pair<String, SqlInfo> pair = stripSqlWithCondition(sql);
        return pair.getLeft();
    }

    public static Pair<String, SqlInfo> stripSqlWithCondition(String sql) {
        Pair<String, SqlInfo> pair = stripedMap.get(sql);
        if (pair != null) {
            return pair;
        }
        pair = doStripSqlWithCondition(sql);
        String stripedSql = pair.getLeft();
        // 只记录sql中不带参数的情况
        if (stripedMap.size() < MAX_SIZE && sql.equalsIgnoreCase(stripedSql)) {
            stripedMap.putIfAbsent(stripedSql, pair);
        }
        return pair;
    }

    private static Pair<String, SqlInfo> doStripSqlWithCondition(String sql) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL, DEFAULT_FEATURE);
        SQLStatement statement = parser.parseStatement();
        StringBuilder out = new StringBuilder();
        SQLASTOutputVisitor visitor = buildCustomSqlOutputVisitor(out, sql, true);
        statement.accept(visitor);
        return Pair.of(out.toString(), ((CustomSqlOutputVisitor) visitor).getSqlInfo());
    }

    public static String simpleFormatSql(String sql, List<Object> params) {
        if (params == null || params.size() == 0) {
            return sql;
        }
        return String.format("%s|||%s", sql, JSON.toJSONString(params));
    }

    public static String formatSql(String sql, List<Object> params) {
        if (params == null || params.size() == 0) {
            return sql;
        }
        StringBuilder out = new StringBuilder();
        SQLASTOutputVisitor visitor = buildCustomSqlOutputVisitor(out, sql);
        visitor.setInputParameters(params);
        SQLStatement statement = SQLUtils.parseSingleStatement(sql, JdbcConstants.MYSQL);
        statement.accept(visitor);
        return out.toString();
    }

    public static void clearStripedMap() {
        stripedMap.clear();
    }
}
