package com.jeesite.modules.cat.xxl.job.brand;

import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.text.PinyinUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheBrandLibDao;
import com.jeesite.modules.cat.dao.MaocheBrandLibKeywordDao;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.model.BrandLibKeywordIndex;
import com.jeesite.modules.cat.model.brandlib.BrandLibIndex;
import com.jeesite.modules.cat.service.MaocheBrandLibMapper;
import com.jeesite.modules.cat.service.MaocheBrandMapper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Resource
    private MaocheBrandLibDao maocheBrandLibDao;

    @Resource
    private MaocheBrandLibMapper maocheBrandLibMapper;

    @Resource
    private MaocheBrandLibKeywordDao maocheBrandLibKeywordDao;

    @Override
    @XxlJob("brandSyncXxlJob")
    public void execute() throws Exception {
        MaocheBrandDO query = new MaocheBrandDO();
        query.setStatus("NORMAL");
        List<MaocheBrandDO> list = maocheBrandMapper.findList(query);

        List<List<MaocheBrandDO>> partition = Lists.partition(list, 10);
        for (List<MaocheBrandDO> p : partition) {
            index(p);
        }
    }

    public void index(List<MaocheBrandDO> brands) {
        List<Long> brandIds = brands.stream().map(MaocheBrandDO::getIid).collect(Collectors.toList());
        // 判断品牌库表是否存在，不存在则创建
        List<MaocheBrandLibDO> libs = maocheBrandLibDao.listByBrandIds(brandIds);
        // 不存在的 libs，需要自动创建
        if (libs == null) {
            libs = new ArrayList<>();
        }
        Map<Long, MaocheBrandLibDO> libMap = libs.stream().collect(Collectors.toMap(MaocheBrandLibDO::getBrandId, Function.identity(), (o1, o2) -> o1));
        // 找到不存在的，创建
        libs.addAll(createLibs(brands, libMap));

        // 品牌的索引
        indexBrands(brands);
        // 品牌库索引
        indexBrandLibs(libs, brands);
    }

    private void indexBrands(List<MaocheBrandDO> brands) {

        List<Map<String, Object>> data = new ArrayList<>();

        for (MaocheBrandDO brandDO : brands) {
            Map<String, Object> item = new HashMap<>();
            // 获取首字母
            String firstSpell = PinyinUtils.getFirstSpell(brandDO.getName());
            if (StringUtils.isNotBlank(firstSpell)) {
                item.put("firstSpell", StringUtils.upperCase(firstSpell.substring(0, 1)));
            }
            item.put("id", brandDO.getIid());
            item.put("brand", brandDO.getName());

            data.add(item);
        }

        elasticSearch7Service.index(data, ElasticSearchIndexEnum.MAOCHE_BRAND_INDEX, "id", 10);
    }

    private void indexBrandLibs(List<MaocheBrandLibDO> libs, List<MaocheBrandDO> brands) {

        Map<Long, MaocheBrandDO> brandMap = brands.stream().collect(Collectors.toMap(MaocheBrandDO::getIid, Function.identity(), (o1, o2) -> o1));

        for (MaocheBrandLibDO lib : libs) {
            // 获取关键词列表
            MaocheBrandLibKeywordDO keywordDO = new MaocheBrandLibKeywordDO();
            keywordDO.setStatus("NORMAL");
            keywordDO.setBrandLibId(lib.getIid());
            List<MaocheBrandLibKeywordDO> keywords = maocheBrandLibKeywordDao.findList(keywordDO);
            MaocheBrandDO brandDO = brandMap.get(lib.getBrandId());

            Map<String, Object> libMap = BrandLibIndex.toIndexMap(lib, brandDO, keywords);
            if (MapUtils.isEmpty(libMap)) {
                continue;
            }

            elasticSearch7Service.index(libMap, ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_INDEX, lib.getId());
            // 同步索引
            if (CollectionUtils.isNotEmpty(keywords)) {
                // todo 订阅数 历史车单 最近发车
                for (MaocheBrandLibKeywordDO keyword : keywords) {
                    Map<String, Object> keywordMap = BrandLibKeywordIndex.toIndexMap(lib, keyword);
                    elasticSearch7Service.index(keywordMap, ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_KEYWORD_INDEX, keyword.getId());
                }
            }
        }
    }

    private List<MaocheBrandLibDO> createLibs(List<MaocheBrandDO> brands, Map<Long, MaocheBrandLibDO> libMap) {
        if (CollectionUtils.isEmpty(brands)) {
            return new ArrayList<>();
        }
        Date date = new Date();

        List<MaocheBrandLibDO> libs = new ArrayList<>();
        for (MaocheBrandDO brandDO : brands) {
            MaocheBrandLibDO libDO = libMap.get(brandDO.getIid());
            if (libDO != null) {
                continue;
            }

            libDO = maocheBrandLibMapper.addByBrand(brandDO);
            if (libDO != null) {
                libs.add(libDO);
            }
        }

        return libs;
    }
}
