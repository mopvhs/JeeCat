package com.jeesite.modules.cat.service.interceptor;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

import java.util.Map;

public class CustomSqlVisitorAdapter extends MySqlASTVisitorAdapter {
    private Map<String, String> tableMap;
    private boolean select;

    public CustomSqlVisitorAdapter(Map<String, String> tableMap, boolean select) {
        this.tableMap = tableMap;
        this.select = select;
    }

    public boolean visit(SQLExprTableSource x) {
        if (tableMap == null || tableMap.size() == 0) {
            return super.visit(x);
        }
        String name = x.getName().toString();
        String shadowName = tableMap.get(name);
        if (shadowName != null) {
            x.setExpr(shadowName);
            // todo yhq
//            ShadowCollector.onTableOperation(name, true, select);
        }
        return true;
    }
}
