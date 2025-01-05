package com.jeesite.modules.cat.service.es;

import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.utils.PinYinUtils;
import com.jeesite.modules.cat.aop.MaocheBrandIndex;
import com.jeesite.modules.cat.entity.MaochePushTaskRuleDO;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.QualityStatusEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.MaocheBrandLibraryIndex;
import com.jeesite.modules.cat.model.brandlib.BrandLibIndex;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.service.MaochePushTaskRuleService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibService;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibCondition;
import com.jeesite.modules.cat.service.es.common.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.jeesite.modules.cat.enums.ElasticSearchIndexEnum.MAOCHE_BRAND_LIBRARY_INDEX;

@Slf4j
@Component
public class BrandLibEsService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaochePushTaskRuleService maochePushTaskRuleService;

    @Resource
    private BrandLibService brandLibService;

    @Resource
    private SearchService searchService;

    public void indexEs(Long id) {

        MaochePushTaskRuleDO query = new MaochePushTaskRuleDO();
        query.setId(String.valueOf(id));
        MaochePushTaskRuleDO ruleDO = maochePushTaskRuleService.get(query);
        if (ruleDO == null) {
            log.error("ruleDO is null, id: {}", id);
            return;
        }

        doIndexEs(ruleDO);
    }

    public void delete(Long id) {

        elasticSearch7Service.delIndex(Collections.singletonList(id), MAOCHE_BRAND_LIBRARY_INDEX);
    }

    public void doIndexEs(MaochePushTaskRuleDO ruleDO) {
        if (ruleDO == null) {
            log.error("ruleDO is null, ruleDO: {}", ruleDO);
            return;
        }

        Map<String, Object> objectMap = buildIndex(ruleDO);

        elasticSearch7Service.index(objectMap, MAOCHE_BRAND_LIBRARY_INDEX, ruleDO.getId());
    }

    public void update(Long id, Map<String, Object> params) {
        if (id == null || id <= 0 || MapUtil.isEmpty(params)) {
            log.error("id or params is null, id: {}, params: {}", id, params);
            return;
        }

        params.put("id", id);
        elasticSearch7Service.update(Collections.singletonList(params), MAOCHE_BRAND_LIBRARY_INDEX, "id", 10);
    }


    private Map<String, Object> buildIndex(MaochePushTaskRuleDO ruleDO) {
        if (ruleDO == null || StringUtils.isBlank(ruleDO.getId())) {
            return null;
        }

        MaocheBrandLibraryIndex index = new MaocheBrandLibraryIndex();
        Long id = ruleDO.getUiid();

        index.setId(id);
        index.setBrand(ruleDO.getBrand());
        index.setKeyword(JsonUtils.toReferenceType(ruleDO.getKeyword(), new TypeReference<List<String>>() {
        }));
        index.setStatus(ruleDO.getStatus());
        index.setCreateTime(ruleDO.getCreateDate().getTime());
        index.setStar(ruleDO.getStar());
        index.setPolling(ruleDO.getPolling());
        index.setEnglishBrand(ruleDO.getEnglishBrand());
        index.setLevelOneCategoryName(ruleDO.getLevelOneCategoryName());
        index.setCategoryName(ruleDO.getCategoryName());
        index.setProductName(ruleDO.getProductName());
        index.setDescription(ruleDO.getDescription());
        // 获取品牌的拼音
        String brandInitial = PinYinUtils.getInitial(ruleDO.getBrand());
        index.setBrandInitial(brandInitial);

        // 获取数量
        long keywordsProductCnt = brandLibService.getKeywordsProductCnt(index.getKeyword());
        long keywordsOceanCnt = brandLibService.getKeywordsOceanCnt(index.getKeyword());
        long historyPushCnt = brandLibService.getPushTaskCnt(id, null);
        long pushDailyInc = brandLibService.getPushTaskCnt(id, DateTimeUtils.getDay(new Date()));

        // 推送时间查询
        Long lastPushTime = brandLibService.getLastPushTime(id);
        Long nextPushTime = brandLibService.getNextPushTime(id);

        List<Long> tags = new ArrayList<>();
        if (StringUtils.isNotBlank(ruleDO.getTag())) {
            tags = JsonUtils.toReferenceType(ruleDO.getTag(), new TypeReference<List<Long>>() {
            });
        }
        index.setTags(tags);

        // 群今日新增 - 外部发单数量
        index.setGroupDailyInc(keywordsProductCnt);
        // 库今日新增 - 此关键词新抓取到的商品
        index.setProductDailyInc(keywordsOceanCnt);
        // 历史任务推送次数
        index.setHistoryPushCnt(historyPushCnt);
        // 今日新增 - 任务推送次数
        index.setPushDailyInc(pushDailyInc);
        // 下次推送时间
        index.setNextPushTime(nextPushTime);
        // 上次推送时间
        index.setLastPushTime(lastPushTime);
        index.setSpecifications(ruleDO.getSpecifications());

        return JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
        });
    }

    public ElasticSearchData<BrandLibIndex, CatProductBucketTO> searchBrandLib(BrandLibCondition condition,
                                                                               Function<BrandLibCondition, List<AggregationBuilder>> aggregation,
                                                                               BiConsumer<BrandLibCondition, BoolQueryBuilder> customBoolQueryBuilder,
                                                                               int from,
                                                                               int size) {

        SearchSourceBuilder source = searchService.searchSource(condition,
                aggregation,
                null,
                customBoolQueryBuilder,
                BrandLibCondition.class,
                from,
                size);

        return elasticSearch7Service.search(source, ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_INDEX, null, CatRobotHelper::convertMaocheBrandLib, null);
    }
}
