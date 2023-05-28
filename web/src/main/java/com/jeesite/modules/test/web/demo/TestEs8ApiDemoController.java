///**
// * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
// * No deletion without permission, or be held responsible to law.
// */
//package com.jeesite.modules.test.web.demo;
//
//import co.elastic.clients.elasticsearch._types.SortOptions;
//import co.elastic.clients.elasticsearch._types.SortOrder;
//import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
//import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
//import co.elastic.clients.elasticsearch.core.IndexResponse;
//import co.elastic.clients.util.ObjectBuilder;
//import com.alibaba.fastjson.JSON;
//import com.jeesite.common.web.BaseController;
//import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
//import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
//import com.jeesite.modules.cat.es.config.es8.SearchService;
//import com.jeesite.modules.cat.helper.CatEsHelper;
//import com.jeesite.modules.cat.model.CarRobotCrawlerMessageIndex;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Function;
//
//
///**
// * 测试树表Controller
// * @author ThinkGem
// * @version 2018-04-22
// */
//@Slf4j
//@Controller
//@RequestMapping(value = "${adminPath}/api/")
//public class TestEsApiDemoController extends BaseController {
//
//	@Resource
//	private SearchService searchService;
//
//	@Resource
//	private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;
//
//
//	@RequestMapping(value = "test/index/maoche/total/message")
//	public void indexMaocheTotalMessage() throws IOException {
//		long id = 0L;
//		int limit = 1000;
//		List<MaocheRobotCrawlerMessageDO> all = new ArrayList<>();
//		while (true) {
//			List<MaocheRobotCrawlerMessageDO> list = maocheRobotCrawlerMessageDao.findAll(id, limit);
//			if (CollectionUtils.isEmpty(list)) {
//				break;
//			}
//			all.addAll(list);
//			id = list.get(list.size() - 1).getIid();
//			if (list.size() < limit) {
//				break;
//			}
//		}
//		for (MaocheRobotCrawlerMessageDO item : all) {
//			try {
//				CarRobotCrawlerMessageIndex catIndex = CatEsHelper.buildCatIndex(item);
//				IndexResponse response = searchService.index("maoche_robot_crawler_message_index", String.valueOf(catIndex.getId()), catIndex);
//			} catch (Exception e) {
//				log.error("index error item:{} ", JSON.toJSONString(item), e);
//			}
//		}
//	}
//
//	@RequestMapping(value = "test/maoche/search")
//	public void maocheSearch() throws IOException {
//
//		Query toQuery = MatchQuery.of(r -> r
//				.field("msg")
//				.query("猫砂")
//		)._toQuery();
//
//		Query query2 = MatchQuery.of(r -> r
//				.field("fromType")
//				.query("2")
//		)._toQuery();
//
//		List<Query> musts = new ArrayList<>();
//		musts.add(toQuery);
//		musts.add(query2);
//
//		Function<BoolQuery.Builder, ObjectBuilder<BoolQuery>> boolQuery = b -> b.must(toQuery);
//
//		Function<Query.Builder, ObjectBuilder<Query>> query = builder -> builder
//				.bool(b -> b
//						.must(musts)
//						)
//				;
//		Function<SortOptions.Builder, ObjectBuilder<SortOptions>> sort = builder -> builder
//				.field(f -> f.field("updateTime").order(SortOrder.Desc));
//
//		List<CarRobotCrawlerMessageIndex> search = searchService.search("maoche_robot_crawler_message_index",
//				CarRobotCrawlerMessageIndex.class,
//				query,
//				sort,
//				1,
//				10,
//				this::convert
//		);
//
//
//		for (CarRobotCrawlerMessageIndex item : search) {
//			System.out.println(JSON.toJSONString(item));
//		}
//
//
//	}
//
//	public CarRobotCrawlerMessageIndex convert(CarRobotCrawlerMessageIndex index) {
//
////		MaocheRobotCrawlerMessageDO target = new MaocheRobotCrawlerMessageDO();
////		BeanUtils.copyProperties(index, target);
//
//		return index;
//	}
//
//
//}