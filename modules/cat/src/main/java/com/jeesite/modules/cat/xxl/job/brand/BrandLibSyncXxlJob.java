package com.jeesite.modules.cat.xxl.job.brand;

import com.google.common.collect.Lists;
import com.jeesite.modules.cat.dao.MaocheBrandLibDao;
import com.jeesite.modules.cat.dao.MaocheBrandLibKeywordDao;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.model.BrandLibIndex;
import com.jeesite.modules.cat.model.BrandLibKeywordIndex;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 品牌库索引
 */
@Slf4j
@Component
public class BrandLibSyncXxlJob extends IJobHandler {


    @Resource
    private MaocheBrandLibDao maocheBrandLibDao;

    @Resource
    private MaocheBrandLibKeywordDao maocheBrandLibKeywordDao;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Override
    @XxlJob("brandLibSyncXxlJob")
    public void execute() throws Exception {

        MaocheBrandLibDO query = new MaocheBrandLibDO();
        query.setStatus("NORMAL");
        List<MaocheBrandLibDO> list = maocheBrandLibDao.findList(query);

        List<List<MaocheBrandLibDO>> partition = Lists.partition(list, 10);

        for (List<MaocheBrandLibDO> libs :partition) {
            for (MaocheBrandLibDO lib : libs) {
                // 获取关键词列表
                MaocheBrandLibKeywordDO keywordDO = new MaocheBrandLibKeywordDO();
                keywordDO.setStatus("NORMAL");
                keywordDO.setBrandLibId(lib.getIid());
                List<MaocheBrandLibKeywordDO> keywords = maocheBrandLibKeywordDao.findList(keywordDO);

                Map<String, Object> libMap = BrandLibIndex.toIndexMap(lib);
                if (MapUtils.isEmpty(libMap)) {
                    continue;
                }
                elasticSearch7Service.index(libMap, ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_INDEX, lib.getId());
                // 同步索引
                if (CollectionUtils.isNotEmpty(keywords)) {
                    // todo 订阅数 历史车单 最近发车
                    for (MaocheBrandLibKeywordDO keyword : keywords) {
                        Map<String, Object> keywordMap = BrandLibKeywordIndex.toIndexMap(keyword);
                        elasticSearch7Service.index(keywordMap, ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_KEYWORD_INDEX, keyword.getId());
                    }
                }
            }
        }
    }
}
