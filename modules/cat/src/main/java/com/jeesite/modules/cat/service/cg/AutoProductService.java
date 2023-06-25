package com.jeesite.modules.cat.service.cg;

import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.message.DingDingService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AutoProductService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private DingDingService dingDingService;

    public void autoAudit() {

        // 每分钟处理50个
        int size = 50;
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("saleStatus", SaleStatusEnum.INIT.getStatus()));
        queryBuilder.must(QueryBuilders.termQuery("auditStatus", AuditStatusEnum.INIT.getStatus()));
        queryBuilder.must(QueryBuilders.termQuery("levelOneCategoryName", "宠物/宠物食品及用品"));

        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("shopDsr");
        rangeQuery.gte(48000);
        queryBuilder.must(rangeQuery);
        RangeQueryBuilder volumeRange = QueryBuilders.rangeQuery("volume");
        volumeRange.gte(100);
        queryBuilder.must(volumeRange);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0)
                .size(size)
                .query(queryBuilder);

        ElasticSearchData<CarAlimamaUnionProductIndex, Object> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.CAT_PRODUCT_INDEX,
                null,
                CatRobotHelper::convertUnionProduct,
                null);

        if (searchData == null || searchData.getTotal() <= 0) {
            return;
        }
        List<CarAlimamaUnionProductIndex> documents = searchData.getDocuments();
        // 批量更新
        List<Long> ids = documents.stream().map(CarAlimamaUnionProductIndex::getId).collect(Collectors.toList());

        boolean row = maocheAlimamaUnionProductService.updateAuditStatus(ids, AuditStatusEnum.PASS.getStatus());

        if (!row) {
            return;
        }

        // 更新索引
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductDao.listByIds(ids);

        cgUnionProductService.indexEs(productDOs, 10);

        dingDingService.sendParseDingDingMsg("自动入库操作完成，ids:{}", JsonUtils.toJSONString(ids));
    }
}
