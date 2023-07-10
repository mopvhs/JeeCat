package com.jeesite.modules.cat.service.cg;

import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.CatActivityEnum;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.QualityStatusEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.message.DingDingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
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

    @Resource
    private MaocheCategoryMappingService maocheCategoryMappingService;

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
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductService.listByIds(ids);

        cgUnionProductService.indexEs(productDOs, 10);

        dingDingService.sendParseDingDingMsg("自动入库操作完成，ids:{}", JsonUtils.toJSONString(ids));
    }

    public void autoGoodProductAudit() {
        // 每分钟处理50个
        int size = 50;
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("saleStatus", SaleStatusEnum.INIT.getStatus()));
        queryBuilder.must(QueryBuilders.termQuery("auditStatus", AuditStatusEnum.INIT.getStatus()));
        queryBuilder.must(QueryBuilders.termsQuery("activity", Collections.singletonList(CatActivityEnum.GOOD_PRICE.getActivity())));
        queryBuilder.must(QueryBuilders.termQuery("levelOneCategoryName", "宠物/宠物食品及用品"));
        // 进库标准：dsr 4.8，销量大于等于100，佣金3%
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("shopDsr");
        rangeQuery.gte(48000);
        queryBuilder.must(rangeQuery);
        RangeQueryBuilder volumeRange = QueryBuilders.rangeQuery("volume");
        volumeRange.gte(100);
        queryBuilder.must(volumeRange);
        RangeQueryBuilder commissionRateRange = QueryBuilders.rangeQuery("commissionRate");
        commissionRateRange.gte(300);
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
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductService.listByIds(ids);

        cgUnionProductService.indexEs(productDOs, 10);

        dingDingService.sendParseDingDingMsg("有好价自动入库操作完成，ids:{}", JsonUtils.toJSONString(ids));
    }


    public void autoNormalProductSaleAudit() {
        // 每分钟处理20个
        int size = 20;
        List<MaocheCategoryMappingDO> roots = maocheCategoryMappingService.getCategoryFromCache(0L);
        for (MaocheCategoryMappingDO mappingDO : roots) {
            List<MaocheCategoryMappingDO> subCategories = maocheCategoryMappingService.getCategoryFromCache(mappingDO.getIid());
            if (CollectionUtils.isEmpty(subCategories)) {
                continue;
            }

            BoolQueryBuilder queryBuilder = autoSaleAuditBaseQuery();
            List<String> categoryNames = subCategories.stream().map(MaocheCategoryMappingDO::getName).collect(Collectors.toList());
            queryBuilder.must(QueryBuilders.termsQuery("categoryName", categoryNames));
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
                continue;
            }
            List<CarAlimamaUnionProductIndex> documents = searchData.getDocuments();
            // 批量更新
            List<Long> ids = documents.stream().map(CarAlimamaUnionProductIndex::getId).collect(Collectors.toList());

            boolean row = maocheAlimamaUnionProductService.updateSaleAuditStatus(ids, AuditStatusEnum.PASS.getStatus(), SaleStatusEnum.ON_SHELF.getStatus());

            if (!row) {
                return;
            }

            // 更新索引
            List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductService.listByIds(ids);

            cgUnionProductService.indexEs(productDOs, 10);

            dingDingService.sendParseDingDingMsg("普通商品自动入库上架操作完成，ids:{}", JsonUtils.toJSONString(ids));
        }
    }

    private static BoolQueryBuilder autoSaleAuditBaseQuery() {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("saleStatus", SaleStatusEnum.INIT.getStatus()));
        queryBuilder.must(QueryBuilders.termQuery("auditStatus", AuditStatusEnum.INIT.getStatus()));
        queryBuilder.mustNot(QueryBuilders.termsQuery("activity", Collections.singletonList(CatActivityEnum.GOOD_PRICE.getActivity())));
        queryBuilder.must(QueryBuilders.termQuery("levelOneCategoryName", "宠物/宠物食品及用品"));
        // 进库标准：dsr 4.8，销量大于等于100
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("shopDsr");
        rangeQuery.gte(48000);
        queryBuilder.must(rangeQuery);
        RangeQueryBuilder volumeRange = QueryBuilders.rangeQuery("volume");
        volumeRange.gte(100);
        queryBuilder.must(volumeRange);

        return queryBuilder;
    }

    public void autoOfflineProduct() {
        // 每分钟处理50个
        int size = 50;
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("saleStatus", SaleStatusEnum.ON_SHELF.getStatus()));
        queryBuilder.must(QueryBuilders.termQuery("auditStatus", AuditStatusEnum.PASS.getStatus()));
        queryBuilder.must(QueryBuilders.termQuery("levelOneCategoryName", "宠物/宠物食品及用品"));
        queryBuilder.mustNot(QueryBuilders.termQuery("qualityStatus", QualityStatusEnum.GOLD.getStatus()));
        BoolQueryBuilder shouldBuilder = new BoolQueryBuilder();
        // 每次更新数据，dsr≤4.8的商品，销量低于100的，任意命中一个均自动下架
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("shopDsr");
        rangeQuery.lt(48000);
        shouldBuilder.should(rangeQuery);
        RangeQueryBuilder volumeRange = QueryBuilders.rangeQuery("volume");
        volumeRange.lt(100);
        shouldBuilder.should(volumeRange);
        queryBuilder.must(shouldBuilder);

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

        int row = maocheAlimamaUnionProductDao.updateSaleStatus(ids, SaleStatusEnum.OFF_SHELF.getStatus(), null);

        if (row <= 0) {
            return;
        }

        // 更新索引
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductService.listByIds(ids);

        cgUnionProductService.indexEs(productDOs, 10);

        dingDingService.sendParseDingDingMsg("普通商品自动下架操作完成，ids:{}", JsonUtils.toJSONString(ids));
    }
}
