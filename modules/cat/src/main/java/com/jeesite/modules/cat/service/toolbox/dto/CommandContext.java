package com.jeesite.modules.cat.service.toolbox.dto;

import com.jeesite.common.collect.MapUtils;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CommandContext implements Serializable {

    @Serial
    private static final long serialVersionUID = -1130638599413741241L;

    // 关联 id
    private String relationId;

    private String content;

    private String resContent;

    private List<String> errors;

    public Map<String, ShortUrlDetail> shortUrlDetailMap;

    public CommandContext(String content) {
        this.content = content;
    }

    public CommandContext() {
    }

    public List<ShortUrlDetail> listShortDetails() {
        if (MapUtils.isEmpty(shortUrlDetailMap)) {
            return new ArrayList<>();
        }
        List<ShortUrlDetail> items = new ArrayList<>();
        for (Map.Entry<String, ShortUrlDetail> entry : shortUrlDetailMap.entrySet()) {
            if (entry == null) {
                continue;
            }

            items.add(entry.getValue());
        }

        return items;
    }

    public void addErrors(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }

        errors.add(error);
    }

    public Map<String, ShortUrlDetail> getShortUrlDetailMap() {
        if (shortUrlDetailMap == null) {
            shortUrlDetailMap = new HashMap<>();
        }
        return shortUrlDetailMap;
    }
}
