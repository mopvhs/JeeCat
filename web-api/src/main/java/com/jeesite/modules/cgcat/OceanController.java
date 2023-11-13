package com.jeesite.modules.cgcat;

import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageProductCondition;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cgcat.dto.ProductCategoryVO;
import com.jeesite.modules.cgcat.dto.ocean.OceanMessageProductVO;
import com.jeesite.modules.cgcat.dto.ocean.OceanMessageVO;
import com.jeesite.modules.cgcat.dto.ocean.OceanMsgProductSearchRequest;
import com.jeesite.modules.cgcat.dto.ocean.OceanMsgSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class OceanController {

    @Resource
    private FlameHttpService flameHttpService;

    @Resource
    private OceanSearchService oceanSearchService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductService;

    @Resource
    private MaocheCategoryMappingService maocheCategoryMappingService;

    @RequestMapping(value = "ocean/msg/product/search")
    public Page<OceanMessageProductVO> oceanMsgProductSearch(@RequestBody OceanMsgProductSearchRequest query,
                                                             HttpServletRequest request, HttpServletResponse response) {
        Page<OceanMessageProductVO> page = new Page<>(request, response);
        if (query == null) {
            return page;
        }

        OceanMessageProductCondition condition = new OceanMessageProductCondition();
        // todo yhq 默认先写时间倒序
        condition.setSorts(Collections.singletonList("createDate desc"));
        if (StringUtils.isNotBlank(query.getKeyword())) {
            condition.setTitle(query.getKeyword());
        }
        int size = page.getPageSize();
        if (size <= 0) {
            size = 10;
        }

        int from = (page.getPageNo() - 1) * size;

        ElasticSearchData<MaocheMessageProductIndex, CatProductBucketTO> searchData = oceanSearchService.searchProduct(condition, null, null, from, size);
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return page;
        }

        List<MaocheMessageProductIndex> documents = searchData.getDocuments();
        List<OceanMessageProductVO> vos = new ArrayList<>();

        Map<Long, UnionProductTO> productMap = new HashMap<>();
        // 获取到innerId->猫车product的id
        List<Long> ids = documents.stream().filter(i -> StringUtils.isNotBlank(i.getInnerId())).map(i -> NumberUtils.toLong(i.getInnerId())).distinct().toList();
        if (CollectionUtils.isNotEmpty(ids)) {
            CatUnionProductCondition productCondition = new CatUnionProductCondition();
            productCondition.setIds(ids);
            ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> products =
                    cgUnionProductService.searchProduct(productCondition, null, from, size);
            List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(products);

            productMap = productTOs.stream().collect(Collectors.toMap(UnionProductTO::getId, i -> i));
        }


        for (MaocheMessageProductIndex index : documents) {
            OceanMessageProductVO vo = OceanMessageProductVO.toVO(index);
            if (vo == null) {
                continue;
            }

            String innerId = index.getInnerId();
            if (StringUtils.isNotBlank(innerId)) {
                UnionProductTO productTO = productMap.get(NumberUtils.toLong(innerId));
                if (productTO != null) {
                    vo.setInnerProduct(productTO);
                }
            }

            vos.add(vo);
        }

        Page<OceanMessageProductVO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), searchData.getTotal(), vos);

        return toPage;
    }


    @RequestMapping(value = "ocean/msg/search")
    public Page<OceanMessageVO> oceanMseeageSearch(@RequestBody OceanMsgSearchRequest query,
                                                             HttpServletRequest request, HttpServletResponse response) {
        Page<OceanMessageVO> page = new Page<>(request, response);

        int size = page.getPageSize();
        if (size <= 0) {
            size = 10;
        }
        int from = (page.getPageNo() - 1) * size;

        // 根据商品id查询关联的商品
        OceanMessageCondition messageCondition = new OceanMessageCondition();
        messageCondition.setMsg(query.getKeyword());
        messageCondition.setAffType("tb");

        // todo yhq 先简单实现
        String sort = "createDate desc";
        if (StringUtils.isNotBlank(query.getSort())) {
            sort = query.getSort() + " desc";
        }
        messageCondition.setSorts(Collections.singletonList(sort));
        ElasticSearchData<MaocheMessageSyncIndex, Object> searchMsg = oceanSearchService.searchMsg(messageCondition, from, size);
        if (searchMsg == null || CollectionUtils.isEmpty(searchMsg.getDocuments())) {
            return page;
        }

        List<MaocheMessageSyncIndex> documents = searchMsg.getDocuments();
        List<OceanMessageVO> vos = OceanMessageVO.toVOs(documents);
        Map<Long, OceanMessageVO> messageVOMap = vos.stream().collect(Collectors.toMap(OceanMessageVO::getId, i -> i, (a, b) -> b));

        // 获取商品信息，根据msgid查询
        List<Long> msgIds = documents.stream().map(MaocheMessageSyncIndex::getId).distinct().toList();
        if (CollectionUtils.isEmpty(msgIds)) {
            return page;
        }
        MaocheRobotCrawlerMessageProductDO queryProduct = new MaocheRobotCrawlerMessageProductDO();
        queryProduct.setMsgId_in(msgIds);
        queryProduct.setStatus("NORMAL");
        List<MaocheRobotCrawlerMessageProductDO> products = maocheRobotCrawlerMessageProductService.findList(queryProduct);
        if (CollectionUtils.isNotEmpty(products)) {
            List<Long> innerIds = products.stream().map(i -> NumberUtils.toLong(i.getInnerId())).filter(i -> i > 0).distinct().toList();
            // 查询索引
            CatUnionProductCondition productCondition = new CatUnionProductCondition();
            productCondition.setIds(innerIds);
            ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> unionProducts =
                    cgUnionProductService.searchProduct(productCondition, null, 0, innerIds.size());
            List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(unionProducts);
            Map<Long, UnionProductTO> unionProductMap = productTOs.stream().collect(Collectors.toMap(UnionProductTO::getId, i -> i, (o, n) -> n));

            Map<Long, List<MaocheRobotCrawlerMessageProductDO>> productMap = products.stream().collect(Collectors.groupingBy(MaocheRobotCrawlerMessageProductDO::getMsgId));
            for (Map.Entry<Long, List<MaocheRobotCrawlerMessageProductDO>> entry : productMap.entrySet()) {
                Long msgId = entry.getKey();
                OceanMessageVO oceanMessageVO = messageVOMap.get(msgId);

                List<MaocheRobotCrawlerMessageProductDO> productDOs = entry.getValue();
                List<UnionProductTO> productVOs = new ArrayList<>();
                for (MaocheRobotCrawlerMessageProductDO productDO : productDOs) {
                    long innerId = NumberUtils.toLong(productDO.getInnerId());
                    UnionProductTO productTO = null;
                    if (innerId > 0) {
                        productTO = unionProductMap.get(innerId);
                    } else {
                        productTO = OceanMessageVO.convertProduct(productDO);
                    }
                    if (productTO != null) {
                        productVOs.add(productTO);
                    }
                }
                oceanMessageVO.setProducts(productVOs);
            }
        }

        Page<OceanMessageVO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), searchMsg.getTotal(), vos);

        return toPage;
    }


    @RequestMapping(value = "/ocean/msg/product/category/tab")
    @ResponseBody
    public Result<ProductCategoryVO> msgProductCategoryTab(HttpServletRequest request, HttpServletResponse response) {


//        condition.setLevelOneCategoryName(null);
//        if (StringUtils.isNotBlank(condition.getPrefixSkuCompareDesc())) {
//            if (condition.getPrefixSkuCompareDesc().equals("empty")) {
//                condition.setPrefixSkuCompareDesc(null);
//            } else if (condition.getPrefixSkuCompareDesc().equals("all")) {
//                condition.setPrefixSkuCompareDesc(null);
//                condition.setHadSkuCompareDesc(true);
//            }
//        }
        OceanMessageProductCondition condition = new OceanMessageProductCondition();

        ElasticSearchData<MaocheMessageProductIndex, CatProductBucketTO> searchData = oceanSearchService.searchProduct(condition, this::buildRootCategoryAgg, CatRobotHelper::convertUnionProductAggregationMap, 0, 0);

        if (searchData == null) {
            return Result.ERROR(500, "查询异常");
        }

        List<MaocheCategoryMappingDO> roots = maocheCategoryMappingService.getCategoryFromCache(0L);
        Map<String, String> rootNameMap = new HashMap<>();
        for (MaocheCategoryMappingDO item : roots) {
            rootNameMap.put("agg_" + item.getId(), item.getName());
        }

        List<CatProductBucketTO> carProductBucketTOs = new ArrayList<>();
        Map<String, List<CatProductBucketTO>> bucketMap = searchData.getBucketMap();
        for (Map.Entry<String, List<CatProductBucketTO>> entry : bucketMap.entrySet()) {
            for (CatProductBucketTO bucket : entry.getValue()) {
                if (StringUtils.isBlank(bucket.getName())) {
                    continue;
                }
                String name = rootNameMap.get(bucket.getName());
                if (StringUtils.isBlank(name)) {
                    continue;
                }
                bucket.setName(name);

                carProductBucketTOs.add(bucket);
            }
        }
        ProductCategoryVO categoryVO = new ProductCategoryVO();
        categoryVO.setCategories(carProductBucketTOs);
        categoryVO.setTotal(searchData.getTotal());

        return Result.OK(categoryVO);
    }

    /**
     * 大类目下的小类目数据
     * @param condition
     * @return
     */
    public <T> List<AggregationBuilder> buildRootCategoryAgg(T condition) {

        List<AggregationBuilder> builders = new ArrayList<>();
        String fieldName = "category";
        String aggNameSuffix = "agg_";
        // 一级类目id
        List<Long> rootCids = maocheCategoryMappingService.getRootCids();
        for (Long cid : rootCids) {
            String name = aggNameSuffix + cid;
            // 获取所有子类目
            List<MaocheCategoryMappingDO> categories = maocheCategoryMappingService.getCategoryFromCache(cid);
            if (CollectionUtils.isEmpty(categories)) {
                continue;
            }
            List<String> cNames = categories.stream().map(MaocheCategoryMappingDO::getName).collect(Collectors.toList());
            FilterAggregationBuilder builder = AggregationBuilders
                    .filter(name, QueryBuilders.termsQuery(fieldName, cNames))
                    .subAggregation(AggregationBuilders.count(name).field("category"));

            builders.add(builder);
        }

        return builders;
    }
}
