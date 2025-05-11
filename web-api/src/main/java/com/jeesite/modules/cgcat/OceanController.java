package com.jeesite.modules.cgcat;

import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.BrandLibCondition;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;
import com.jeesite.modules.cat.dao.WxChatDao;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.entity.WxChatDO;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.BrandLibTO;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.MaocheBrandLibraryIndex;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageProductCondition;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibConvertService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.cg.search.BrandLibSearchService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cgcat.dto.ProductCategoryVO;
import com.jeesite.modules.cgcat.dto.ocean.OceanMessageVO;
import com.jeesite.modules.cgcat.dto.ocean.OceanMsgSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class OceanController {

//    @Resource
//    private FlameHttpService flameHttpService;

    @Resource
    private OceanSearchService oceanSearchService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductService;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private MaocheCategoryMappingService maocheCategoryMappingService;

    @Resource
    private BrandLibConvertService brandLibConvertService;

    @Resource
    private BrandLibSearchService brandLibSearchService;

    @Resource
    private BrandLibService brandLibService;

    @Resource
    private WxChatDao wxChatDao;

    @Resource
    private MaocheRobotCrawlerMessageSyncDao maocheRobotCrawlerMessageSyncDao;

    @Resource
    private MaocheRobotCrawlerMessageDao maocheRobotCrawlerMessageDao;

//    @RequestMapping(value = "ocean/msg/product/search")
//    public Page<OceanMessageProductVO> oceanMsgProductSearch(@RequestBody OceanMsgProductSearchRequest query,
//                                                             HttpServletRequest request, HttpServletResponse response) {
//        Page<OceanMessageProductVO> page = new Page<>(request, response);
//        if (query == null) {
//            return page;
//        }
//
//        OceanMessageProductCondition condition = new OceanMessageProductCondition();
//        // todo yhq 默认先写时间倒序
//        condition.setSorts(Collections.singletonList("createDate desc"));
//        if (StringUtils.isNotBlank(query.getKeyword())) {
//            condition.setTitle(query.getKeyword());
//        }
//        int size = page.getPageSize();
//        if (size <= 0) {
//            size = 10;
//        }
//
//        int from = (page.getPageNo() - 1) * size;
//
//        ElasticSearchData<MaocheMessageProductIndex, CatProductBucketTO> searchData = oceanSearchService.searchProduct(condition, null, null, from, size);
//        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
//            return page;
//        }
//
//        List<MaocheMessageProductIndex> documents = searchData.getDocuments();
//        List<OceanMessageProductVO> vos = new ArrayList<>();
//
//        Map<Long, UnionProductTO> productMap = new HashMap<>();
//        // 获取到innerId->猫车product的id
//        List<Long> ids = documents.stream().filter(i -> StringUtils.isNotBlank(i.getInnerId())).map(i -> NumberUtils.toLong(i.getInnerId())).distinct().toList();
//        if (CollectionUtils.isNotEmpty(ids)) {
//            CatUnionProductCondition productCondition = new CatUnionProductCondition();
//            productCondition.setIds(ids);
//            ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> products =
//                    cgUnionProductService.searchProduct(productCondition, null, from, size);
//            List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(products);
//
//            productMap = productTOs.stream().collect(Collectors.toMap(UnionProductTO::getId, i -> i));
//        }
//
//
//        for (MaocheMessageProductIndex index : documents) {
//            OceanMessageProductVO vo = OceanMessageProductVO.toVO(index);
//            if (vo == null) {
//                continue;
//            }
//
//            String innerId = index.getInnerId();
//            if (StringUtils.isNotBlank(innerId)) {
//                UnionProductTO productTO = productMap.get(NumberUtils.toLong(innerId));
//                if (productTO != null) {
//                    vo.setInnerProduct(productTO);
//                }
//            }
//
//            vos.add(vo);
//        }
//
//        Page<OceanMessageProductVO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), searchData.getTotal(), vos);
//
//        return toPage;
//    }


    /**
     * 公海搜索
     * @param query
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "ocean/msg/search")
    public Page<OceanMessageVO> oceanMessageSearch(@RequestBody OceanMsgSearchRequest query,
                                                             HttpServletRequest request, HttpServletResponse response) {
        Page<OceanMessageVO> page = new Page<>(request, response);

        int size = page.getPageSize();
        if (size <= 0) {
            size = 10;
        }
        int from = (page.getPageNo() - 1) * size;

        List<String> keywords = new ArrayList<>();
        BrandLibTO brandLibTO = null;
        // 获取品牌库
        Long brandLibId = query.getBrandLibId();
        if (brandLibId != null && brandLibId > 0) {
            // 获取所有的5星规则
            BrandLibCondition condition = new BrandLibCondition();
            condition.setId(brandLibId);
            ElasticSearchData<MaocheBrandLibraryIndex, CatProductBucketTO> search = brandLibSearchService.search(condition, null, null, 0, 1);
            if (search != null && CollectionUtils.isNotEmpty(search.getDocuments())) {
                MaocheBrandLibraryIndex brandLibraryIndex = search.getDocuments().get(0);
                if (brandLibraryIndex != null && CollectionUtils.isNotEmpty(brandLibraryIndex.getKeyword())) {
                    keywords = brandLibraryIndex.getKeyword();
                }

                brandLibTO = brandLibConvertService.convertBrandLibTO(brandLibraryIndex);

            }
        }

        // 获取3天前的开始时间
        long startTime = DateUtils.getOfDayFirst(DateUtils.addDays(new Date(), -3)).getTime();

        // 根据商品id查询关联的商品
        OceanMessageCondition messageCondition = new OceanMessageCondition();
        long syncMsgId = NumberUtils.toLong(query.getKeyword());
        if (syncMsgId > 0) {
            messageCondition.setId(syncMsgId);
            // 找ai相关的车单
            MaocheRobotCrawlerMessageSyncDO syncDO = maocheRobotCrawlerMessageSyncDao.getById(syncMsgId);
            if (syncDO == null) {
                return page;
            }
            MaocheRobotCrawlerMessageDO robotMsg = maocheRobotCrawlerMessageDao.getById(syncDO.getRobotMsgId());
            if (robotMsg == null) {
                return page;
            }
            Long relationId = robotMsg.getRelationId();
            if (relationId != null) {
                // 查询
                List<MaocheRobotCrawlerMessageDO> robotMsgs = maocheRobotCrawlerMessageDao.listByRelationId(relationId);

                // 获取到全部的机器人消息id
                if (CollectionUtils.isNotEmpty(robotMsgs)) {
                    List<Long> robotIds = robotMsgs.stream().map(MaocheRobotCrawlerMessageDO::getIid).toList();
                    messageCondition.setId(null);
                    messageCondition.setRobotMsgIds(robotIds);
                }
            }

        } else if (StringUtils.isNotBlank(query.getKeyword())) {
            keywords.add(query.getKeyword());
        }

        if (StringUtils.isNotBlank(query.getStatus())) {

            OceanStatusEnum status = OceanStatusEnum.getByStatus(query.getStatus());
            if (status != null) {
                if (status.getGroup().equals("ocean")) {
                    messageCondition.setStatus(query.getStatus());
                } else if (status.getGroup().equals("ai_ocean")) {
                    messageCondition.setOceanStatus(Collections.singletonList(query.getStatus()));
                }
            }

        } else {
            List<String> oceanStatus = OceanStatusEnum.listGroup("ai_ocean");
            messageCondition.setOceanStatus(oceanStatus);
        }
        messageCondition.setKeywords(keywords);

        if (StringUtils.isNotBlank(query.getCategoryName())) {
            messageCondition.setCategoryNames(Collections.singletonList(query.getCategoryName()));
        }

        String sort = "wxTime desc";
        if (StringUtils.isNotBlank(query.getSort())) {
            if ("newProduct".equals(query.getSort())) {
                // 今天开始时间
                startTime = DateUtils.getOfDayFirst(new Date()).getTime();
                messageCondition.setNewProduct(1L);
            }
        }

        messageCondition.setGteCreateDate(startTime);
        messageCondition.setSorts(Collections.singletonList(sort));
        ElasticSearchData<MaocheMessageSyncIndex, CatProductBucketTO> searchMsg = oceanSearchService.searchMsg(
                messageCondition,
                OceanController::getCategoryNameAgg,
                CatRobotHelper::convertUnionProductAggregationMap,
                brandLibService::brandLibOceanQuery,
                from, size);
        if (searchMsg == null || CollectionUtils.isEmpty(searchMsg.getDocuments())) {
            return page;
        }

        List<MaocheMessageSyncIndex> documents = searchMsg.getDocuments();
        List<Long> ids = documents.stream().map(MaocheMessageSyncIndex::getId).distinct().toList();
        // 获取群id
        List<String> chatWxIds = documents.stream().map(MaocheMessageSyncIndex::getRobotChatId).filter(Objects::nonNull).distinct().toList();
        // 查机器人表
        Map<String, WxChatDO> chatMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(chatWxIds)) {
            List<WxChatDO> chats = wxChatDao.listByWxChatIds(chatWxIds);
            chatMap = chats.stream().collect(Collectors.toMap(WxChatDO::getChatWxId, Function.identity(), (o, n) -> n));
        }

        // 根据id查询同步的消息
        List<MaocheRobotCrawlerMessageSyncDO> messageSyncDOs = maocheRobotCrawlerMessageSyncService.listByIds(ids);
        Map<Long, MaocheRobotCrawlerMessageSyncDO> syncDOMap = messageSyncDOs.stream().collect(Collectors.toMap(MaocheRobotCrawlerMessageSyncDO::getUiid, Function.identity(), (o, n) -> n));

        List<OceanMessageVO> vos = OceanMessageVO.toVOs(documents);

        for (OceanMessageVO vo : vos) {
            String chatWxId = vo.getChatWxId();
            if (StringUtils.isNotBlank(chatWxId) && chatMap.get(chatWxId) != null) {
                String name = chatMap.get(chatWxId).getName();
                vo.setChatName(name);
            }

            String aiStatus = OceanStatusEnum.getStatusDesc(vo.getAiStatus());
            String status = OceanStatusEnum.getStatusDesc(vo.getStatus());

            vo.setStatusDesc(String.format("AI:[%s] NOR:[%s]", aiStatus, status));
        }

        // 对url加html <a>标签
        OceanMessageVO.replaceUrl2Html(vos, syncDOMap);

        Map<Long, OceanMessageVO> messageVOMap = vos.stream().collect(Collectors.toMap(OceanMessageVO::getId, i -> i, (a, b) -> b));

        // 获取商品信息，根据msgid查询
        List<Long> msgIds = documents.stream().map(MaocheMessageSyncIndex::getId).distinct().toList();
        if (CollectionUtils.isEmpty(msgIds)) {
            return page;
        }
        long start = System.currentTimeMillis();

        OceanMessageProductCondition msgProductCondition = new OceanMessageProductCondition();
        msgProductCondition.setMsgIds(msgIds);
        ElasticSearchData<MaocheMessageProductIndex, CatProductBucketTO> msgProductData = oceanSearchService.searchProduct(msgProductCondition, null, null, 0, 1000);

        log.info("查询消息商品耗时：{}", System.currentTimeMillis() - start);
        if (msgProductData != null && CollectionUtils.isNotEmpty(msgProductData.getDocuments())) {
            List<MaocheMessageProductIndex> msgProducts = msgProductData.getDocuments();
            List<Long> innerIds = msgProducts.stream().map(i -> NumberUtils.toLong(i.getInnerId())).filter(i -> i > 0).distinct().toList();
            // 查询索引
            CatUnionProductCondition productCondition = new CatUnionProductCondition();
            productCondition.setIds(innerIds);
            ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> unionProducts =
                    cgUnionProductService.searchProduct(productCondition, null, 0, innerIds.size());
            List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(unionProducts);
            Map<Long, UnionProductTO> unionProductMap = productTOs.stream().collect(Collectors.toMap(UnionProductTO::getId, i -> i, (o, n) -> n));

            Map<Long, List<MaocheMessageProductIndex>> productMap = msgProducts.stream().collect(Collectors.groupingBy(MaocheMessageProductIndex::getMsgId));
            for (Map.Entry<Long, List<MaocheMessageProductIndex>> entry : productMap.entrySet()) {
                Long msgId = entry.getKey();
                OceanMessageVO oceanMessageVO = messageVOMap.get(msgId);

                List<MaocheMessageProductIndex> productIndies = entry.getValue();
                List<UnionProductTO> productVOs = new ArrayList<>();
                for (MaocheMessageProductIndex productIndex : productIndies) {
                    long innerId = NumberUtils.toLong(productIndex.getInnerId());
                    UnionProductTO productTO = null;
                    if (innerId > 0) {
                        productTO = unionProductMap.get(innerId);
                    }
                    if (productTO == null) {
                        productTO = OceanMessageVO.convertProduct(productIndex);
                    }
                    if (productTO != null) {
                        productVOs.add(productTO);
                    }
                }
                oceanMessageVO.setProducts(productVOs);
            }
        }

        Page<OceanMessageVO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), searchMsg.getTotal(), vos);
        // name = "猫全价膨化粮"
        //count = {Long@20509} 1078
        Map<String, List<CatProductBucketTO>> bucketMap = searchMsg.getBucketMap();
        toPage.addOtherData("categoryName", bucketMap.get("categoryName"));
        toPage.addOtherData("brandLib", brandLibTO);
        return toPage;
    }


    public static List<AggregationBuilder> getCategoryNameAgg(OceanMessageCondition condition) {
        List<AggregationBuilder> aggregations = new ArrayList<>();
        TermsAggregationBuilder categoryAgg = AggregationBuilders.terms("categoryName").field("categoryNames").size(1000);
        aggregations.add(categoryAgg);
        return aggregations;
    }


    @RequestMapping(value = "/ocean/msg/product/category/tab")
    @ResponseBody
    public Result<ProductCategoryVO> msgProductCategoryTab(HttpServletRequest request, HttpServletResponse response) {

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
