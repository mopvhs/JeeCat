package com.jeesite.modules.cat.xxl.job.brand;

import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.text.PinyinUtils;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.MaocheBrandMapper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步品牌信息到索引
 */
@Slf4j
@Component
public class BrandSyncXxlJob extends IJobHandler {

    @Resource
    private MaocheBrandMapper maocheBrandMapper;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Override
    @XxlJob("brandSyncXxlJob")
    public void execute() throws Exception {
        MaocheBrandDO query = new MaocheBrandDO();
        query.setStatus("NORMAL");
        List<MaocheBrandDO> list = maocheBrandMapper.findList(query);

        List<List<MaocheBrandDO>> partition = Lists.partition(list, 10);
        for (List<MaocheBrandDO> p : partition) {
            List<Map<String, Object>> data = new ArrayList<>();

            for (MaocheBrandDO brandDO : p) {
                Map<String, Object> item = new HashMap<>();
                // 获取首字母
                String firstSpell = PinyinUtils.getFirstSpell(brandDO.getName());
                if (StringUtils.isNotBlank(firstSpell)) {
                    item.put("firstSpell", firstSpell.substring(0, 1));
                }
                item.put("id", brandDO.getIid());
                item.put("brand", brandDO.getName());

                data.add(item);
            }

            elasticSearch7Service.index(data, ElasticSearchIndexEnum.MAOCHE_BRAND_INDEX, "id", 10);
        }
    }
}
