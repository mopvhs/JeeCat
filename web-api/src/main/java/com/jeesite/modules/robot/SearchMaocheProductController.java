package com.jeesite.modules.robot;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
import com.jeesite.modules.cat.model.CatRobotMessageCondition;
import com.jeesite.modules.cat.model.MaocheProductCondition;
import com.jeesite.modules.cat.model.MaocheProductIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class SearchMaocheProductController {

    @Resource
    private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @RequestMapping(value = "maoche/product/search")
    public Result<ElasticSearchData<MaocheProductIndex, Object>> syncMessage(@RequestBody SearchRequest<MaocheProductCondition> request) {

        if (request == null) {
            return Result.ERROR(500, "参数错误");
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(request.getCondition(), this::maocheProductShouldQuery, MaocheProductCondition.class);

        List<String> sorts = request.getSorts();
        if (CollectionUtils.isNotEmpty(sorts)) {
            for (String sort : sorts) {
                if (StringUtils.isBlank(sort)) {
                    continue;
                }
                String[] sortArr = sort.split(" ");
                String name = sortArr[0];
                SortOrder sortOrder = SortOrder.DESC;
                if (sortArr.length > 1) {
                    String order = sortArr[1];
                    switch (order) {
                        case "asc":
                            sortOrder = SortOrder.ASC;
                            break;
                    }
                }
                searchSourceBuilder.sort(name, sortOrder);
            }
        }
        // ES搜索条件
        searchSourceBuilder.from(request.getFrom());
        searchSourceBuilder.size(request.getSize());
        searchSourceBuilder.query(boolBuilder);

        ElasticSearchData<MaocheProductIndex, Object> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.MAOCHE_PRODUCT_INDEX,
                null,
                CatRobotHelper::convertMaocheProduct,
                null);

        return Result.OK(searchData);
    }

    public BoolQueryBuilder maocheProductShouldQuery(MaocheProductCondition condition, BoolQueryBuilder boolBuilder) {
        if (condition == null || StringUtils.isBlank(condition.getContentNew()) || boolBuilder == null) {
            return null;
        }

        BoolQueryBuilder should = new BoolQueryBuilder();

        should.should(new MultiMatchQueryBuilder(condition.getContentNew(), "contentNew").boost(2.0f));

        should.should(new WildcardQueryBuilder("contentNew", "*" + condition.getContentNew() +  "*"));

        should.should(new FuzzyQueryBuilder("contentNew", condition.getContentNew()));

//        should.should(new MatchPhrasePrefixQueryBuilder("contentNew", condition.getContentNew()));
        should.should(new MatchPhraseQueryBuilder("contentNew", condition.getContentNew()).slop(50));

        boolBuilder.must(should);

        return boolBuilder;
    }
}
