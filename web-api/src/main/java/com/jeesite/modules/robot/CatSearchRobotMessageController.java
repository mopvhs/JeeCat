package com.jeesite.modules.robot;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
import com.jeesite.modules.cat.model.condition.CatRobotMessageCondition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class CatSearchRobotMessageController {

    @Resource
    private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @RequestMapping(value = "cat/robot/message/search")
    public Result<ElasticSearchData<CarRobotCrawlerMessageIndex, Object>> syncMessage(@RequestBody SearchRequest<CatRobotMessageCondition> request) {

        if (request == null) {
            return Result.ERROR(500, "参数错误");
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(request.getCondition(), CatRobotMessageCondition.class);

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

        ElasticSearchData<CarRobotCrawlerMessageIndex, Object> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.CAT_ROBOT_CRAWLER_MESSAGE_INDEX,
                null,
                CatRobotHelper::convert,
                null);


//        log.info("maocheSearch response {}", JSON.toJSONString(searchData));

        return Result.OK(searchData);
    }
}
