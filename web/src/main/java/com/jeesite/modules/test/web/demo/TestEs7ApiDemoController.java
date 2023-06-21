package com.jeesite.modules.test.web.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatEsHelper;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class TestEs7ApiDemoController {

//    CAT_ROBOT_CRAWLER_MESSAGE_INDEX

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;

	@RequestMapping(value = "test/index/maoche/total/message")
    public String syncAll() {
        long id = 0L;
		int limit = 1000;
		List<MaocheRobotCrawlerMessageDO> all = new ArrayList<>();
		while (true) {
			List<MaocheRobotCrawlerMessageDO> list = maocheRobotCrawlerMessageDao.findAll(id, limit);
			if (CollectionUtils.isEmpty(list)) {
				break;
			}
			all.addAll(list);
			id = list.get(list.size() - 1).getIid();
			if (list.size() < limit) {
				break;
			}
		}
		for (MaocheRobotCrawlerMessageDO item : all) {
			try {
				CarRobotCrawlerMessageIndex catIndex = CatEsHelper.buildCatIndex(item);
                Map<String, Object> data = JSONObject.parseObject(JSON.toJSONString(catIndex), Map.class);
				elasticSearch7Service.index(data, ElasticSearchIndexEnum.CAT_ROBOT_CRAWLER_MESSAGE_INDEX, String.valueOf(catIndex.getId()));
			} catch (Exception e) {
				log.error("index error item:{} ", JSON.toJSONString(item), e);
			}
		}
        return "完成";
    }

    @RequestMapping(value = "test/maoche/search")
	public void maocheSearch() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // ES搜索条件
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();
        boolBuilder.must(QueryBuilders.matchQuery("msg", "猫砂"));
        boolBuilder.must(QueryBuilders.termQuery("fromType", "2"));
        searchSourceBuilder.sort("updateTime", SortOrder.DESC);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        searchSourceBuilder.query(boolBuilder);

        ElasticSearchData<CarRobotCrawlerMessageIndex, Object> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.CAT_ROBOT_CRAWLER_MESSAGE_INDEX,
				null,
				CatRobotHelper::convert,
				null);


//        log.info("maocheSearch response {}", JSON.toJSONString(searchData));
	}

}
