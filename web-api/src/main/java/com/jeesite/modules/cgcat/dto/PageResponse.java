package com.jeesite.modules.cgcat.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private long total;

    private List<T> items;
}
