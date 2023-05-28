package com.jeesite.modules.cat.service.cg;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.modules.cat.dao.MaocheSenderTaskDao;
import com.jeesite.modules.cat.entity.MaocheSenderTaskDO;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.CatActivityEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.model.UnionProductTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@EnableScheduling
@Component
public class CgUnionProductStatisticsService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheSenderTaskDao maocheSenderTaskDao;

    // todo 任务间隔60秒
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void runJob() {
        // 判断是否

    }

    // 每日统计
    public void statistics() {
        // 时间，选品库，
        // 时间，选品库，有好价 + top2 catDsr

        /**
         * 每日群消息发布，早中晚三次，（8：00   11：00    19：30）需要有个编辑器，可以编辑群内发布文字，或根据以下内容规则自动生成。（文字模版还要修改，以下只是个例子）
         * 例：
         *
         * 今日优质商品播报：
         * * 今日新进库商品：1543件
         * * 今日“有好价”商品：241件
         * * 今日新进优品：（筛选有好价新进商品，优先旗舰店）
         * 1.网易优选猫粮八拼 有好价：19.6元 该商品低于全网同款均价13%（商品名取短标题，API中有）
         * 2.皇家猫粮幼猫，有好价：86.6元 该商品低于全网同款均价25%
         * 更多今日好价，欢迎浏览：http://xxx.xxx.com/1.html（取H5“有好价”页面”）
         */
        int passStatus = AuditStatusEnum.PASS.getStatus();
        long starTime = DateTimeUtils.earliestTimeToday(System.currentTimeMillis());

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setSaleStatus(SaleStatusEnum.ON_SHELF.getStatus());
        condition.setAuditStatus(passStatus);
        // todo yhq 需要恢复
//        condition.setGteCreateTime(starTime);

        // 聚合，获取券后价格最低的一个商品金额
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition,
                null,
                0,
                0);

        long total = searchData.getTotal();

        // 有好价
        condition.setActivity(Collections.singletonList(CatActivityEnum.GOOD_PRICE.getActivity()));
        condition.setSorts(Collections.singletonList("catDsr desc"));

        // 聚合，获取券后价格最低的一个商品金额
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> goodPriceSearchData = cgUnionProductService.searchProduct(condition,
                null,
                0,
                2);

        long goodProductTotal = goodPriceSearchData.getTotal();
        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(goodPriceSearchData);

        StringBuilder builder = new StringBuilder();

        builder.append("今日优质商品播报：\n");
        builder.append("* 今日新进库商品：").append(total).append("件\n");
        builder.append("* 今日“有好价”商品：").append(goodProductTotal).append("件\n");
//        builder.append("* 今日新进优品：（筛选有好价新进商品，优先旗舰店）\n");
        builder.append("* 今日新进优品：\n");

        int i = 1;
        for (UnionProductTO item : productTOs) {
            String advantage = "";
            if (CollectionUtils.isNotEmpty(item.getPriceAdvantage())) {
                advantage = item.getPriceAdvantage().get(0);
            }
            builder.append(i).append(".").append(item.getTitle()).append(" 有好价：").append(item.getReservePrice() / 100.0).append("元 ").append(advantage).append("\n");
        }
        builder.append("更多今日好价，欢迎浏览：\n");


        record(builder.toString());

//        System.out.println(builder.toString());
    }

    public void record(String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }
        MaocheSenderTaskDO taskDO = new MaocheSenderTaskDO();
        taskDO.setRobotId(2L);
        taskDO.setChatroomId(12L);
        taskDO.setContentType(1L);
        taskDO.setContentJson(msg);
//        taskDO.setStatus("1");
        taskDO.setNextExecuteTime(System.currentTimeMillis() / 1000);
        taskDO.setInterval(1L);
        taskDO.setCreateTime(new Date());
        taskDO.setUpdateTime(new Date());

        maocheSenderTaskDao.insert(taskDO);
    }
}
