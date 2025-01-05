package com.jeesite.modules.cgcat.miniprogram;

import cn.hutool.core.map.MapUtil;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheSubscribeDao;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheSubscribeDO;
import com.jeesite.modules.cat.entity.MaocheTagDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.subscribe.SubscribeStatusEnum;
import com.jeesite.modules.cat.enums.subscribe.SubscribeTypeEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.BrandLibKeywordIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.condition.BrandLibKeywordCondition;
import com.jeesite.modules.cat.service.MaocheBrandMapper;
import com.jeesite.modules.cat.service.MaocheTagService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cgcat.dto.CategoryVO;
import com.jeesite.modules.cgcat.dto.subscribe.LibInfoVO;
import com.jeesite.modules.cgcat.dto.subscribe.LibKeywordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}")
public class ApiLibKeywordSearchController {

    // 猫粮 猫砂 猫用品 驱虫保健
    private static List<String> aggsName = new ArrayList<>();
    static {
        aggsName.add("猫粮");
        aggsName.add("猫砂");
        aggsName.add("猫用品");
        aggsName.add("驱虫保健");
    }

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaocheBrandMapper maocheBrandMapper;

    @Resource
    private MaocheTagService maocheTagService;

    @Resource
    private MaocheSubscribeDao maocheSubscribeDao;

    @RequestMapping(value = "/api/maoche/mini/program/lib/keyword/search")
    @ResponseBody
    public Result<LibInfoVO> searchKeyword(BrandLibKeywordCondition condition) {

        if (condition == null || StringUtils.isBlank(condition.getKeyword())) {
            return Result.ERROR(500, "参数错误");
        }

        List<String> sorts = Optional.ofNullable(condition.getSorts()).orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(sorts)) {
            sorts.add("subscribeCount desc");
            condition.setSorts(sorts);
        }
        int from = condition.getPage() - 1;
        int size = condition.getPageSize();

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, BrandLibKeywordCondition.class);
        brandLibKeywordQuery(condition, boolBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ES搜索条件
        searchSourceBuilder.query(boolBuilder);
        List<CategoryVO> categories = new ArrayList<>();

        // todo 按照类目聚合(只有首页做)
        if (from == 0) {
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(1);
            ElasticSearchData<BrandLibKeywordIndex, CatProductBucketTO> categoryAggData = elasticSearch7Service.search(searchSourceBuilder,
                    ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_KEYWORD_INDEX,
                    buildCategoryAgg(condition),
                    OceanSearchService::convertBrandLibKeywordIndex,
                    CatRobotHelper::convertUnionProductAggregationMap);

            if (categoryAggData != null && MapUtil.isNotEmpty(categoryAggData.getBucketMap())) {
                List<CatProductBucketTO> aggs = categoryAggData.getBucketMap().get("levelOneCategoryName");
                Map<String, CatProductBucketTO> bucket = aggs.stream().collect(Collectors.toMap(CatProductBucketTO::getName, Function.identity(), (o1, o2) -> o1));
                for (String name : aggsName) {
                    CatProductBucketTO bucketTO = bucket.get(name);
                    Long count = 0L;
                    if (bucketTO != null) {
                        count = bucketTO.getCount();
                    }
                    CategoryVO c =  new CategoryVO();
                    c.setTitle(name);
                    c.setCount(count);
                    categories.add(c);
                }
            }
        }

        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        ElasticSearchData<BrandLibKeywordIndex, Object> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_KEYWORD_INDEX,
                null,
                OceanSearchService::convertBrandLibKeywordIndex,
                null);

        LibInfoVO vo = new LibInfoVO();
        vo.setTotal(0);
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return Result.OK(vo);
        }
        List<Long> tagIds = new ArrayList<>();
        List<Long> brandIds = new ArrayList<>();
        List<String> keywordIds = new ArrayList<>();
        for (BrandLibKeywordIndex index : searchData.getDocuments()) {
            keywordIds.add(String.valueOf(index.getId()));
            brandIds.add(index.getBrandId());
            if (CollectionUtils.isEmpty(index.getAliasNames()) && CollectionUtils.isNotEmpty(index.getTags())) {
                tagIds.addAll(index.getTags());
            }
        }
        brandIds = brandIds.stream().distinct().collect(Collectors.toList());
        tagIds = tagIds.stream().distinct().collect(Collectors.toList());
        // 查询品牌信息
        List<MaocheBrandDO> brands = maocheBrandMapper.listByIds(brandIds);
        Map<Long, MaocheBrandDO> brandMap = brands.stream().collect(Collectors.toMap(MaocheBrandDO::getIid, Function.identity(), (o1, o2) -> o1));
        // 获取标签
        List<MaocheTagDO> tags = maocheTagService.listByIds(tagIds);
        Map<Long, MaocheTagDO> tagMap = tags.stream().collect(Collectors.toMap(MaocheTagDO::getIid, Function.identity(), (o1, o2) -> o1));

        List<LibKeywordVO> keywords = LibKeywordVO.toVO(searchData.getDocuments(), brandMap, tagMap);
        if (CollectionUtils.isEmpty(keywords)) {
            return Result.OK(vo);
        }

        List<MaocheSubscribeDO> subscribes = maocheSubscribeDao.listUserSubscribe(condition.getUserId(), keywordIds, SubscribeTypeEnum.BRAND_LIB_KEYWORD.getType());
        Map<String, MaocheSubscribeDO> isSubscribeMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(subscribes)) {
            isSubscribeMap = subscribes.stream().filter(i -> SubscribeStatusEnum.SUBSCRIBE.name().equalsIgnoreCase(i.getStatus())).collect(Collectors.toMap(MaocheSubscribeDO::getSubscribeId, Function.identity(), (o1, o2) -> o1));
        }
        for (LibKeywordVO keyword : keywords) {
            keyword.setSubscribed(isSubscribeMap.containsKey(String.valueOf(keyword.getId())));
        }
        vo.setCategories(categories);
        vo.setKeywords(keywords);
        vo.setTotal(searchData.getTotal());

        return Result.OK(vo);
    }

    public void brandLibKeywordQuery(BrandLibKeywordCondition condition, BoolQueryBuilder builder) {
        String keyword = condition.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            return;
        }
        BoolQueryBuilder should = new BoolQueryBuilder();
        should.should(QueryBuilders.matchPhraseQuery("keyword", keyword).slop(100));
        should.should(QueryBuilders.matchPhraseQuery("brandName", keyword).slop(100));
        should.should(QueryBuilders.matchPhraseQuery("aliasNames", keyword).slop(100));

        builder.must(should);
    }

    /**
     * 大类目下的小类目数据
     * @param condition
     * @return
     */
    public AggregationBuilder buildCategoryAgg(BrandLibKeywordCondition condition) {
        String fieldName = "levelOneCategoryName";
        TermsAggregationBuilder categoryAgg = AggregationBuilders.terms(fieldName).field(fieldName).size(1000);

        return categoryAgg;
    }
}
