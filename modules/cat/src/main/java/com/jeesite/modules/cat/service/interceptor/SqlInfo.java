package com.jeesite.modules.cat.service.interceptor;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SqlInfo {
    public static final int SQL_SELECT = 1;
    public static final int SQL_UPDATE = 2;
    public static final int SQL_INSERT = 3;
    public static final int SQL_DELETE = 4;

    @AllArgsConstructor
    @Data
    public static class Item {
        private String name;
        private String value;
        private Object realValue;

        public boolean isWild() {
            return "?".equals(value);
        }
    }

    private List<Item> itemList = new ArrayList<>();
    private String table;

    public void setTable(String table) {
        if (table == null) {
            return;
        }
        this.table = table;
    }

    public void addCmp(String name, String value) {
        itemList.add(new Item(name, value, value));
    }

    public void addLimit(String value) {
        itemList.add(new Item("limit", value, value));
    }

    public void addOffset(String value) {
        itemList.add(new Item("offset", value, value));
    }

    public void addIn(String name, String value) {
        itemList.add(new Item(name, value, value));
    }
}
