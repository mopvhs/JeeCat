package com.jeesite.modules.cat.service.interceptor;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.visitor.ExportParameterVisitorUtils;
import lombok.Getter;

@Getter
public class CustomSqlOutputVisitor extends MySqlOutputVisitor {

    private SqlInfo sqlInfo;
    private boolean limitOffsetRelated;
    private boolean needIntercept;

    public CustomSqlOutputVisitor(Appendable appender, String sql, boolean needIntercept) {
        super(appender);
        this.limitOffsetRelated = isLimitOffsetRelated(sql);
        this.needIntercept = needIntercept;
        sqlInfo = new SqlInfo();
    }

    public CustomSqlOutputVisitor(Appendable appender, String sql, boolean parameterized, boolean needIntercept) {
        super(appender, parameterized);
        this.limitOffsetRelated = isLimitOffsetRelated(sql);
        this.needIntercept = needIntercept;
        sqlInfo = new SqlInfo();
    }

    private boolean isLimitOffsetRelated(String sql) {
        if (sql == null) {
            return false;
        }
        sql = sql.toUpperCase();
        return sql.contains(" LIMIT ") && sql.contains(" OFFSET ");
    }

    public boolean visit(SQLLimit x) {
        if (needIntercept) {
            sqlInfo.addLimit(x.getRowCount().toString());
            if (x.getOffset() != null) {
                sqlInfo.addOffset(x.getOffset().toString());
            }
        }
        if (!limitOffsetRelated) {
            return super.visit(x);
        } else {
            this.print0(this.ucase ? "LIMIT " : "limit ");
            SQLExpr rowCount = x.getRowCount();
            this.printExpr(rowCount);
            SQLExpr offset = x.getOffset();
            if (offset != null) {
                this.print0(this.ucase ? " OFFSET " : " offset ");
                this.printExpr(offset);
            }
            return false;
        }
    }

    public boolean visit(SQLBinaryOpExpr expr) {
        if (needIntercept && isTargetExpr(expr.getLeft())) {
            SQLExpr sqlExpr = expr.getRight();
            Class<?> clazz = sqlExpr.getClass();
            if (clazz == SQLVariantRefExpr.class
                    || clazz == SQLIntegerExpr.class
                    || clazz == SQLNumberExpr.class
                    || clazz == SQLCharExpr.class
                    || clazz == SQLNullExpr.class) {
                sqlInfo.addCmp(expr.getLeft().toString(), sqlExpr.toString());
            } else if (clazz == SQLBooleanExpr.class) {
                sqlInfo.addCmp(expr.getLeft().toString(), String.valueOf(((SQLBooleanExpr) sqlExpr).getValue()));
            }
        }
        return super.visit(expr);
    }

    public boolean visit(SQLUpdateSetItem expr) {
        sqlInfo.addCmp(expr.getColumn().toString(), expr.getValue().toString());
        return super.visit(expr);
    }

    public boolean visit(SQLInListExpr expr) {
        if (needIntercept && isTargetExpr(expr.getExpr())) {
            sqlInfo.addIn(expr.getExpr().toString(), String.valueOf(expr.getTargetList().size()));
        }
        return super.visit(expr);
    }

    public boolean visit(SQLBooleanExpr expr) {
        if (this.parameterized) {
            print('?');
            incrementReplaceCunt();

            if(this.parameters != null){
                ExportParameterVisitorUtils.exportParameter(this.parameters, expr);
            }
            return false;
        }
        return super.visit(expr);
    }

    public boolean visit(SQLExprTableSource x) {
        sqlInfo.setTable(x.getName().getSimpleName());
        return super.visit(x);
    }

    public boolean visit(SQLNullExpr expr) {
        if (this.parameterized) {
            print('?');
            incrementReplaceCunt();
            if(this.parameters != null){
                this.getParameters().add(null);
            }
            return false;
        }
        return super.visit(expr);
    }

    private boolean isTargetExpr(SQLExpr expr) {
        Class<?> klass = expr.getClass();
        return klass == SQLIdentifierExpr.class || klass == SQLPropertyExpr.class;
    }
}
