package com.jeesite.modules.cgcat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.utils.PatternUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.model.CatUserMessagePushTO;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.ProductAuditRequest;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.service.MaocheCategoryService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.CgUnionProductStatisticsService;
import com.jeesite.modules.cat.service.cg.CgUserRcmdService;
import com.jeesite.modules.cgcat.dto.HistorySearchKeywordVO;
import com.jeesite.modules.cgcat.dto.ProductCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class CgProductPushController {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private CgUnionProductStatisticsService cgUnionProductStatisticsService;

    @Resource
    private CgUserRcmdService cgUserRcmdService;

    @Resource
    private MaocheCategoryService maocheCategoryService;


    // 微信企业 @推送商品
    @RequestMapping(value = "/product/push/detail")
    @ResponseBody
    public Page<UnionProductTO> productWarehouseDetail(@RequestBody CatUserMessagePushTO messagePushTO, HttpServletRequest request, HttpServletResponse response) {

        Page<UnionProductTO> page = new Page<>(request, response);
        if (messagePushTO == null) {
            return page;
        }

        Map<String, Object> otherData = new HashMap<>();
        String res = "亲爱的小主您好，根据你的要求，共找到%d个商品，券后最低【%s】,已按猫宝分计算完毕，自动排序供您筛选，欢迎浏览以下页面：";

        String tips = null;
        // 是否命中@逻辑
        List<String> list = PatternUtils.matchMention(messagePushTO.getKeyword());
//        if (CollectionUtils.isEmpty(list)) {
//            otherData.put("tips", String.format("亲爱的小主您好，根据你的要求，共找到%d个【关键词】", 0));
//            page.setList(new ArrayList<>());
//            page.setCount(0);
//            return page;
//        }
        int pageNo = Optional.ofNullable(messagePushTO.getPageNo()).orElse(1);
        Integer pageSize = Optional.ofNullable(messagePushTO.getPageSize()).orElse(20);
        int from = (pageNo - 1) * pageSize;
        int size = pageSize;

        // 猫车分倒排
        List<String> sorts = new ArrayList<>();
        sorts.add("catDsr desc");

        String keyword = messagePushTO.getKeyword();
        for (String target : list) {
            keyword = keyword.replace(target, "");
        }
        keyword = StringUtils.trim(keyword);

        // 写redis
        cgUserRcmdService.setHistoryKeyword(messagePushTO.getOpenId(), keyword);

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setTitle(keyword);
        // todo yhq 先注释测试
        condition.setSaleStatus(SaleStatusEnum.ON_SHELF.getStatus());
        condition.setAuditStatus(AuditStatusEnum.PASS.getStatus());

        condition.setSorts(sorts);

        long cidOne = NumberUtils.toLong(messagePushTO.getCidOne());
        if (cidOne > 0) {
            condition.setCidOnes(Collections.singletonList(cidOne));
        }

        // 聚合，获取券后价格最低的一个商品金额
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition,
                cgUnionProductService::buildCatMessagePushAgg,
                from,
                size);

        boolean backup = false;
        // 首页，并且检索不出来商品的时候，检索的商品源改为选品库的
        if ((searchData == null || searchData.getTotal() == 0) && from == 0) {
            // 未人工审核的商品
            condition.setSaleStatus(SaleStatusEnum.INIT.getStatus());
            searchData = cgUnionProductService.searchProduct(condition,
                    cgUnionProductService::buildCatMessagePushAgg,
                    from,
                    size);

            backup = true;
        }
        if (searchData == null || searchData.getTotal() == 0) {
            otherData.put("tips", String.format("亲爱的小主您好，根据你的要求，共找到%d个商品", 0));
            page.setList(new ArrayList<>());
            page.setCount(0);
            page.setOtherData(otherData);
            return page;
        }

        long total = searchData.getTotal();

        long minPrice = 0;
        Map<String, List<CatProductBucketTO>> bucketMap = searchData.getBucketMap();
        if (bucketMap != null && bucketMap.get("min_coupon_price") != null) {
            CatProductBucketTO minCouponPrice = bucketMap.get("min_coupon_price").get(0);
            minPrice = Optional.ofNullable(minCouponPrice.getCount()).orElse(0L);
        }

        String matchRes = String.format(res, total, new BigDecimal(minPrice).divide(new BigDecimal("100"), 2, RoundingMode.UP).toString());
        otherData.put("tips", matchRes);

        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
        // 这样前端计算的时候 就没有下一页了
        if (backup) {
            total = productTOs.size();
        }

        Page<UnionProductTO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), total, productTOs);
        toPage.setOtherData(otherData);

        return toPage;
    }

    // 微信企业 @推送商品
    @RequestMapping(value = "/product/rcmd/list")
    @ResponseBody
    public Result<List<UnionProductTO>> rcmdProductWarehouseDetail() {


        int from = 0;
        int size = 4;

        // 猫车分倒排
        List<String> sorts = new ArrayList<>();
        sorts.add("catDsr desc");

        // 随机关键词
//        List<String> keywords = new ArrayList<>();
//        keywords.add("猫粮");
//        keywords.add("猫零食");

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setTitle("猫粮 猫零食");
        condition.setSaleStatus(SaleStatusEnum.ON_SHELF.getStatus());
        condition.setAuditStatus(AuditStatusEnum.PASS.getStatus());
        condition.setSorts(sorts);

        // 聚合，获取券后价格最低的一个商品金额
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition,
                cgUnionProductService::buildCatMessagePushAgg,
                from,
                size);
        if (searchData == null || searchData.getTotal() == 0) {
            return Result.OK(new ArrayList<>());
        }

        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);

        return Result.OK(productTOs);
    }


    // 微信企业 @推送商品
    @RequestMapping(value = "/product/statistics/detail")
    @ResponseBody
    public Result<?> statistics() {

        GetMappingsResponse indexMapping = elasticSearch7Service.getIndexMapping(ElasticSearchIndexEnum.CAT_PRODUCT_INDEX);

//        cgUnionProductStatisticsService.runJob();

        return Result.OK();
    }

    public static void main(String[] args) {
        String input = "这是一条@猫车 的测试消息。";

        String regex = "@[\\u4e00-\\u9fa5A-Za-z]+\\s";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    // 分享口令
    @RequestMapping(value = "/product/share/command/get")
    @ResponseBody
    public Result<String> getProductShareCommand(@RequestBody ProductAuditRequest request) {

        if (request == null || StringUtils.isBlank(request.getItemId())) {
            return Result.ERROR(404, "参数错误");
        }

        String apiUrl = cgUnionProductService.getEApiUrl("V73687541H40026415", request.getItemId(), "mm_30153430_909250463_109464700418");

        return Result.OK(apiUrl);
    }

    // 历史查询记录
    @RequestMapping(value = "/product/history/search/keyword")
    @ResponseBody
    public Result<HistorySearchKeywordVO> getHistorySearchKeyword(@RequestBody CatUserMessagePushTO messagePushTO) {

        if (messagePushTO == null) {
            return Result.ERROR(404, "参数错误");
        }
        if (StringUtils.isBlank(messagePushTO.getOpenId())) {
            return Result.ERROR(404, "用户不存在");
        }

        HistorySearchKeywordVO keywordVO = new HistorySearchKeywordVO();
        List<String> historyKeywords = cgUserRcmdService.getHistoryKeywords(messagePushTO.getOpenId());
        keywordVO.setKeywords(historyKeywords);

        return Result.OK(keywordVO);
    }

    // 自定义的类目
    @RequestMapping(value = "/product/custom/category")
    @ResponseBody
    public Result<ProductCategoryVO> customCategory() {

        List<CategoryTree> categoryTreeFromCache = maocheCategoryService.getCategoryTreeFromCache();

        List<Long> rootCids = new ArrayList<>();
        rootCids.add(6L);
        rootCids.add(1L);
        rootCids.add(7L);
        rootCids.add(8L);

        ProductCategoryVO response = new ProductCategoryVO();
        List<CatProductBucketTO> categories = new ArrayList<>();
        // 先后台写死顺序
        for (CategoryTree cat : categoryTreeFromCache) {
            if (rootCids.contains(cat.getId())) {
                CatProductBucketTO item = new CatProductBucketTO();
                item.setKey(String.valueOf(cat.getId()));
                item.setName(cat.getName());
                categories.add(item);
            }
        }

        CatProductBucketTO allItem = new CatProductBucketTO();
        allItem.setKey("0");
        allItem.setName("全部");
        categories.add(0, allItem);

        response.setCategories(categories);

        return Result.OK(response);
    }


}
