package com.jeesite.modules.cat.service.interceptor;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.util.JdbcConstants;
import com.jeesite.modules.cat.common.SpringContextUtil;
import com.jeesite.modules.cat.service.message.DingDingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
@Intercepts({@Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
), @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
), @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
)})
@Component
public class SlowSqlInterceptor implements Interceptor {

    private final static SQLParserFeature[] DEFAULT_FEATURE = {
            SQLParserFeature.EnableSQLBinaryOpExprGroup,
            SQLParserFeature.UseInsertColumnsCache,
            SQLParserFeature.OptimizedForParameterized,
            SQLParserFeature.OptimizedForForParameterizedSkipValue,
    };


    private long slowSqlTime = 500;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        long start = System.currentTimeMillis();

        Object result = invocation.proceed(); // Invoke the next interceptor or the target method

        long left = System.currentTimeMillis() - start;
        if (slowSqlTime > 0 && left >= slowSqlTime) {
            DingDingService dingDingService = SpringContextUtil.getBean("dingDingService", DingDingService.class);
            if (dingDingService != null) {
                // 记录慢sql
                String formatContent = getFormatContent(invocation, left);
                dingDingService.sendDingDingMsg(formatContent);
            }
        }

        // Add post-processing logic if needed
        return result;
    }

    public static String getFormatContent(Invocation invocation, long left) {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = statement.getBoundSql(parameterObject);
        String sql = boundSql.getSql();

        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL, DEFAULT_FEATURE);
        SQLStatement sqlStatement = parser.parseStatement();
        sql = SqlVisitorHelper.formatStatement(sql, sqlStatement);

//        StringBuilder out = new StringBuilder();
//        SQLASTOutputVisitor visitor = SqlVisitorHelper.buildCustomSqlOutputVisitor(out, sql, true);
//        sqlStatement.accept(visitor);

        String timeDesc = "执行时间：" + left + "ms";
        // 大于10s的记录为秒
        if (left >= 10000) {
            // 毫秒转换为秒
            timeDesc = "执行时间：" + TimeUnit.MILLISECONDS.toSeconds(left) + "s";
        }

        String slowFormatSql = "当前sql：" + sql + " \n" + timeDesc;

        return slowFormatSql;
    }

    @Override
    public Object plugin(Object target) {
        return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}
