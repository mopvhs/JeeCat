package com.jeesite.modules.cat.service.stage.cg;

import com.jeesite.modules.cat.common.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ProductEsFactory {

    private final Map<String, ProductEsStage> stageMap = new HashMap<>();

    @PostConstruct
    public void init() {
        List<ProductEsStage> beans = SpringContextUtil.getBeansOfType(ProductEsStage.class);
        if (CollectionUtils.isEmpty(beans)) {
            return;
        }
        for (ProductEsStage stage : beans) {
            List<String> sources = stage.getSources();
            if (CollectionUtils.isEmpty(sources)) {
                continue;
            }
            for (String source : sources) {
                stageMap.put(source, stage);
            }
        }
    }

    public ProductEsStage getStage(String source) {

        ProductEsStage stage = stageMap.get(source);

        if (stage == null) {
//            throw new IllegalArgumentException("获取结构化策略失败 source: " + source);
//            log.info("获取结构化策略失败 source: {}", source);
            return null;
        }

//        return (T) stage.convert(context);
        return stage;
    }
}
