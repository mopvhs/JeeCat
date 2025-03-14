package com.jeesite.modules.cgcat;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.utils.PatternUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductPriceChartDao;
import com.jeesite.modules.cat.entity.CsOpLogDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductPriceChartDO;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.entity.MaocheProductV2DO;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.CatActivityEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.PriceHelper;
import com.jeesite.modules.cat.helper.dataoke.DaTaoKeResponseHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatNineProductTO;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.model.CatUserMessagePushTO;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.ProductAuditRequest;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.model.dataoke.DaTaoKeResponse;
import com.jeesite.modules.cat.service.CsOpLogService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaocheCategoryService;
import com.jeesite.modules.cat.service.MaocheProductV2Service;
import com.jeesite.modules.cat.service.cg.AutoProductService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.CgUnionProductStatisticsService;
import com.jeesite.modules.cat.service.cg.CgUserRcmdService;
import com.jeesite.modules.cat.service.cg.DaTaoKeApiService;
import com.jeesite.modules.cat.service.cg.inner.InnerApiService;
import com.jeesite.modules.cat.service.cg.third.VeApiService;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.cg.third.tb.dto.CommandResponseV2;
import com.jeesite.modules.cat.service.cg.third.tb.dto.GeneralConvertResp;
import com.jeesite.modules.cat.service.helper.ProductSearchHelper;
import com.jeesite.modules.cat.service.message.DingDingService;
import com.jeesite.modules.cgcat.dto.HistorySearchKeywordVO;
import com.jeesite.modules.cgcat.dto.ProductCategoryVO;
import com.jeesite.modules.cgcat.dto.ProductRobotResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Resource
    private MaocheCategoryMappingService maocheCategoryMappingService;

    @Resource
    private AutoProductService autoProductService;

    @Resource
    private VeApiService veApiService;

    @Resource
    private CsOpLogService csOpLogService;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private InnerApiService innerApiService;

    @Resource
    private TbApiService tbApiService;

    @Resource
    private MaocheProductV2Service maocheProductV2Service;

    // 微信企业 @推送商品
    @RequestMapping(value = "/product/rcmd/robot/at")
    @ResponseBody
    public Result<ProductRobotResponse> atRobotRcmdProduct(@RequestBody CatUserMessagePushTO messagePushTO) {

        ProductRobotResponse response = new ProductRobotResponse();
        String url = "https://cat.zhizher.com/cat-sass-mobile/#/";
        response.setTitle("姨姨，撒都没找到！");
        response.setContent("您可以更换更准确的关键词或者@猫车小助理协助\n");
        response.setImg("https://mmbiz.qpic.cn/sz_mmbiz_png/y7ibJn5iaZcWBicu2ewoJaiazq2q7ot0szXMAw3JaQlBFH3QPk2oicR5SdlVNbwlkGbZ6ooatibEuOWgjQzSGWvTFusA/640?wx_fmt=png");
        response.setTargetUrl(url);
        if (messagePushTO == null) {
            return Result.OK(response);
        }

        // 是否命中@逻辑
        List<String> list = PatternUtils.matchMention(messagePushTO.getKeyword());
        String keyword = messagePushTO.getKeyword();
        for (String target : list) {
            keyword = keyword.replace(target, "");
        }
        keyword = StringUtils.trim(keyword);

        // 是否命中9.9
        if ("9.9".equalsIgnoreCase(keyword)) {
            List<Long> nineNumList = cgUnionProductStatisticsService.getNineNum();
            if (CollectionUtils.isNotEmpty(nineNumList) && nineNumList.size() >= 2) {
                long totalNum = nineNumList.get(0);
                long todayNum = nineNumList.get(1);
                String title = "猫车® 今日9.9精选" + totalNum + "件，新增" + todayNum + "件";
                if (totalNum > 0) {
                    response.setTitle(title);
                    response.setContent("精选店铺评分4.8分以上猫咪优质好物，帮您省心买\n");
                    response.setImg("https://mmbiz.qpic.cn/sz_mmbiz_png/y7ibJn5iaZcWBicu2ewoJaiazq2q7ot0szXMAw3JaQlBFH3QPk2oicR5SdlVNbwlkGbZ6ooatibEuOWgjQzSGWvTFusA/640?wx_fmt=png");
                    response.setTargetUrl("https://cat.zhizher.com/cat-sass-mobile/#/pages/sys/goods/index");
                    return Result.OK(response);
                }
            }
        }

        try {
            url += "pages/sys/search_result/index?keyword=" + URLEncoder.encode(keyword, "UTF-8");
            response.setTargetUrl(url);
        } catch (Exception e) {

        }

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setTitle(keyword);
        condition.setSaleStatus(SaleStatusEnum.ON_SHELF.getStatus());
        condition.setAuditStatus(AuditStatusEnum.PASS.getStatus());
//        condition.setHadRates(true);
        SearchSourceBuilder source = cgUnionProductService.searchSource(condition, cgUnionProductService::buildCatRobotPushAgg, null, null, 0, 1);

        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(source);

        boolean backup = false;
        // 首页，并且检索不出来商品的时候，检索的商品源改为选品库的
        if ((searchData == null || searchData.getTotal() == 0)) {
            // 未人工审核的商品
            condition.setSaleStatus(SaleStatusEnum.INIT.getStatus());
            source = cgUnionProductService.searchSource(condition, cgUnionProductService::buildCatRobotPushAgg, null, null, 0, 1);
            searchData = cgUnionProductService.search(source);
            backup = true;
        }
        if (searchData == null || searchData.getTotal() == 0) {
            return Result.OK(response);
        }

        long total = searchData.getTotal();
        if (backup) {
            total = Math.min(total, 20);
        }

        Map<String, List<CatProductBucketTO>> bucketMap = searchData.getBucketMap();
        // 获取券后价最低的金额
        long minPrice = 0;
        long maxCouponPrice = 0;
        if (bucketMap != null) {

            // 获取券后价最低的金额
            if (CollectionUtils.isNotEmpty(bucketMap.get("min_coupon_price"))) {
                CatProductBucketTO minCouponPrice = bucketMap.get("min_coupon_price").get(0);
                minPrice = Optional.ofNullable(minCouponPrice.getCount()).orElse(0L);
            }

            // 优惠券最大的金额
            if (CollectionUtils.isNotEmpty(bucketMap.get("max_coupon"))) {
                CatProductBucketTO minCouponPrice = bucketMap.get("max_coupon").get(0);
                maxCouponPrice = Optional.ofNullable(minCouponPrice.getCount()).orElse(0L);
            }
        }

        // 帮您找到猫砂盆 1502件
        String titleFormat = "%s %d件来了";
        String contentFormat = "券后最低%s元，最多省%s元，欢迎浏览";
        String title = keyword;
        if (StringUtils.length(title) > 4) {
            title = keyword.substring(0, 4) + "...";
        }

        String content = String.format(contentFormat,
                PriceHelper.formatPrice(minPrice, ".00", ""),
                PriceHelper.formatPrice(maxCouponPrice, ".00", ""));

        response.setTitle(String.format(titleFormat, title, total));
        response.setContent(content);

        return Result.OK(response);
    }

    // 微信企业
    @RequestMapping(value = "/product/push/detail")
    @ResponseBody
    public Page<UnionProductTO> productWarehouseDetail(@RequestBody CatUserMessagePushTO messagePushTO, HttpServletRequest request, HttpServletResponse response) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Page<UnionProductTO> page = new Page<>(request, response);
        if (messagePushTO == null) {
            return page;
        }

        // 是否命中@逻辑
        List<String> list = PatternUtils.matchMention(messagePushTO.getKeyword());
        int pageNo = Optional.ofNullable(messagePushTO.getPageNo()).orElse(1);
        int pageSize = 10;
        int from = (pageNo - 1) * pageSize;
        int size = pageSize;

        // 猫车分倒排
        List<String> sorts = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(messagePushTO.getSorts())) {
            sorts.addAll(messagePushTO.getSorts());
        }
        sorts.add("catDsr desc");

        String keyword = messagePushTO.getKeyword();
        for (String target : list) {
            keyword = keyword.replace(target, "");
        }
        keyword = StringUtils.trim(keyword);

        // 写redis
        cgUserRcmdService.setHistoryKeyword(messagePushTO.getOpenId(), keyword);

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setId(messagePushTO.getId());
        condition.setTitle(keyword);
        condition.setSaleStatus(SaleStatusEnum.ON_SHELF.getStatus());
        condition.setAuditStatus(AuditStatusEnum.PASS.getStatus());
        condition.setSorts(sorts);

        if (messagePushTO.getOnlyCoupon() != null && messagePushTO.getOnlyCoupon().equals(1)) {
            condition.setGteCoupon(1L);
//            condition.setGteCouponRemainCount(1L);
        }

        if (messagePushTO.getOnlyPriceChart() != null && messagePushTO.getOnlyPriceChart().equals(1)) {
            condition.setPriceChart(1L);
//            condition.setGtePriceChartSyncTime(DateTimeUtils.earliestTimeToday(System.currentTimeMillis()) - 86400000L);
        }

        long cidOne = NumberUtils.toLong(messagePushTO.getCidOne());
        List<String> categoryNames = new ArrayList<>();
        if (cidOne > 0) {
            // 获取到类目映射
            List<MaocheCategoryMappingDO> categories = maocheCategoryMappingService.listByParentId(cidOne);
            if (CollectionUtils.isNotEmpty(categories)) {
                categoryNames.addAll(categories.stream().map(MaocheCategoryMappingDO::getName).toList());
            }

            condition.setCategoryNames(categoryNames);
        }

        long esStart = System.currentTimeMillis();
        SearchSourceBuilder source = cgUnionProductService.searchSource(condition, null, cgUnionProductService::commonSort, null, from, size);
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(source);
        long esSearchEnd = System.currentTimeMillis();
        long esSearchTime = esSearchEnd - esStart;

        long backupTime = 0;
        boolean backup = false;
        // 首页，并且检索不出来商品的时候，检索的商品源改为选品库的
        if ((searchData == null || searchData.getTotal() == 0) && from == 0) {
            // 未人工审核的商品
            condition.setSaleStatus(SaleStatusEnum.INIT.getStatus());
            source = cgUnionProductService.searchSource(condition, null, cgUnionProductService::commonSort, null, from, 20);
            searchData = cgUnionProductService.search(source);

            backupTime = System.currentTimeMillis() - esSearchEnd;

            backup = true;
        }
        if (searchData == null || searchData.getTotal() == 0) {
            page.setList(new ArrayList<>());
            page.setCount(0);
            return page;
        }

        long total = searchData.getTotal();

        long convertTime = System.currentTimeMillis();
        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
        long convertTakeTime = System.currentTimeMillis() - convertTime;

        // 过滤无效额商品
        if (CollectionUtils.isNotEmpty(productTOs)) {
            productTOs = productTOs.stream().filter(i -> StringUtils.isNotBlank(i.getImgUrl())).collect(Collectors.toList());
        }

        // 这样前端计算的时候 就没有下一页了
        if (backup) {
            total = productTOs.size();
        }

        Page<UnionProductTO> toPage = new Page<>(page.getPageNo() + 1, pageSize, total, productTOs);

        stopWatch.stop();
        long time = stopWatch.getTime();
        if (time >= 1000) {
            log.info("productWarehouseDetail time:{}," +
                    " esSearchTime:{}ms," +
                    " backupTime:{}ms," +
                    " convertTakeTime:{} ms", stopWatch.toString(), esSearchTime, backupTime, convertTakeTime);
        }

        return toPage;
    }

    // 有好价h5
    @RequestMapping(value = "/product/good/list")
    @ResponseBody
    public Page<UnionProductTO> listGoodProducts(@RequestBody CatUserMessagePushTO messagePushTO, HttpServletRequest request, HttpServletResponse response) {

        Page<UnionProductTO> page = new Page<>(request, response);
        if (messagePushTO == null || StringUtils.isBlank(messagePushTO.getCidOne())) {
            return page;
        }

        List<MaocheCategoryMappingDO> subCategories = maocheCategoryMappingService.getCategoryFromCache(NumberUtils.toLong(messagePushTO.getCidOne()));
        if (CollectionUtils.isEmpty(subCategories)) {
            return page;
        }

        // 猫车分倒排
        List<String> sorts = new ArrayList<>();
        sorts.add("catDsr desc");

        CatUnionProductCondition condition = new CatUnionProductCondition();
        List<String> activity = Collections.singletonList(CatActivityEnum.GOOD_PRICE.getActivity());

        List<String> categoryNames = subCategories.stream().map(MaocheCategoryMappingDO::getName).collect(Collectors.toList());
        condition.setCategoryNames(categoryNames);
        condition.setActivity(activity);
        condition.setGteShopDsr(48000);
        // 5%
        condition.setGteCommissionRate(500L);
        condition.setGteCouponRemainCount(1L);
        condition.setHadRates(true);

        Long nDay = 86400L * 3L * 1000L;
        condition.setGteUpdateTime(System.currentTimeMillis() - nDay);

        condition.setAuditStatus(AuditStatusEnum.PASS.getStatus());
        condition.setSorts(sorts);

        // 是否命中@逻辑
        List<String> list = PatternUtils.matchMention(messagePushTO.getKeyword());
        int pageNo = Optional.ofNullable(messagePushTO.getPageNo()).orElse(1);
        int pageSize = 10;
        int from = (pageNo - 1) * pageSize;
        int size = pageSize;

        SearchSourceBuilder source = cgUnionProductService.searchSource(condition, null, cgUnionProductService::commonSort, null, from, size);
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(source);

        if (searchData == null || searchData.getTotal() == 0) {
            page.setList(new ArrayList<>());
            page.setCount(0);
            return page;
        }

        long total = searchData.getTotal();
        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
        // 过滤无效额商品
        if (CollectionUtils.isNotEmpty(productTOs)) {
            productTOs = productTOs.stream().filter(i -> StringUtils.isNotBlank(i.getImgUrl())).collect(Collectors.toList());
        }

        Page<UnionProductTO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), total, productTOs);

        return toPage;
    }

    // 微信企业 @推送商品
    @RequestMapping(value = "/product/rcmd/list")
    @ResponseBody
    public Result<List<UnionProductTO>> rcmdProductWarehouseDetail() {


        int from = 0;
        int size = 4;

        try {
            // 猫车分倒排
            List<String> sorts = new ArrayList<>();
            sorts.add("catDsr desc");

            // 随机关键词
            List<String> keywords = new ArrayList<>();
            keywords.add("猫粮");
            keywords.add("猫零食");
            Random random = new Random();

            CatUnionProductCondition condition = new CatUnionProductCondition();
            condition.setTitle(keywords.get(random.nextInt(2)));
            condition.setSaleStatus(SaleStatusEnum.ON_SHELF.getStatus());
            condition.setAuditStatus(AuditStatusEnum.PASS.getStatus());
            condition.setHadRates(true);
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
        } catch (Exception e) {
            DingDingService.sendParseDingDingMsg("H5 推荐商品异常，{}", e.getMessage());
            log.error("rcmdProductWarehouseDetail exception ", e);
        }

        return Result.ERROR(500, "服务器错误");
    }


    // 微信企业 @推送商品
    @RequestMapping(value = "/product/statistics/detail")
    @ResponseBody
    public String statistics() {


        cgUnionProductStatisticsService.statistics();

        return "完成";
    }

    // 分享口令
    @RequestMapping(value = "/product/share/command/get")
    @ResponseBody
    public Result<String> getProductShareCommand(@RequestBody ProductAuditRequest request) {

        if (request == null || StringUtils.isBlank(request.getItemId())) {
            return Result.ERROR(404, "参数错误");
        }
//        String vekey = "V73687541H40026415";
//        String pid = "mm_30153430_909250463_109464700418";
//        Result<String> eApiUrl = cgUnionProductService.getEApiUrl(vekey, request.getItemId(), pid);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("detail", 2);
        objectMap.put("deepcoupon", 1);
        objectMap.put("couponId", 1);
//        Result<CommandResponseV2> commonCommand = tbApiService.getCommonCommand(request.getItemId(), objectMap);

        Result<GeneralConvertResp> commonCommand = tbApiService.generalConvert(request.getItemId(), objectMap);

        if (!Result.isOK(commonCommand) || commonCommand.getResult() == null || StringUtils.isBlank(commonCommand.getResult().getTbkPwd())) {
            String errorMsg = "哎呀，来晚了，宝贝卖完啦！";

            String message = commonCommand.getMessage();
            // 产品或活动转链失败，请检查参数。高佣转链提示 :  该宝贝已下架或非淘客宝贝
            if (message.contains("该商品已下架或非淘宝联盟") || message.contains("该宝贝已下架或非淘客宝贝")) {
                // 判断是否是库内商品，是的话直接下架
                // 判断是否是商品的itemId
                String[] split = StringUtils.split(request.getItemId(), "-");
                if (split.length == 2) {
                    // 插叙是否再在库内
                    String itemId = split[1];
                    MaocheProductV2DO unionProductDO = maocheProductV2Service.getProduct(itemId, "NORMAL");
                    if (unionProductDO != null) {
                        // 执行下架
                        List<Long> productIds = Collections.singletonList(unionProductDO.getProductId());
                        int auditStatus = maocheAlimamaUnionProductDao.updateSaleStatus(productIds,
                                SaleStatusEnum.AUTO_OFF_SHELF.getStatus(),
                                null);

                        if (auditStatus > 0) {
                            // 重新查一次数据库
                            List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductService.listByIds(productIds);
                            cgUnionProductService.indexEs(productDOs, 10);
                        }
                        String msgFormat = "{} \n 口令获取商品失效，自动下架：{}, 下架结果：{}";
                        DingDingService.sendParseDingDingMsg(msgFormat, 1, JsonUtils.toJSONString(request), JSONUtil.toJsonStr(commonCommand), auditStatus);
                    }
                }
            }

            return Result.ERROR(500, errorMsg);
        }

        GeneralConvertResp result = commonCommand.getResult();
        return Result.OK(result.getTbkPwd());
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

        ProductCategoryVO response = new ProductCategoryVO();
        List<CatProductBucketTO> categories = new ArrayList<>();

        CatProductBucketTO allItem = new CatProductBucketTO();
        allItem.setKey("0");
        allItem.setName("全部");
        categories.add(0, allItem);

        List<Long> rootCids = maocheCategoryMappingService.getRootCids();
        // 先后台写死顺序
        List<MaocheCategoryMappingDO> categoryMappingDOs = maocheCategoryMappingService.listByParentId(0L);
        if (CollectionUtils.isNotEmpty(categoryMappingDOs)) {
            Map<Long, MaocheCategoryMappingDO> doMap = categoryMappingDOs.stream().collect(Collectors.toMap(MaocheCategoryMappingDO::getIid, Function.identity(), (o1, o2) -> o1));
            for (Long id : rootCids) {
                MaocheCategoryMappingDO maocheCategoryMappingDO = doMap.get(id);
                if (maocheCategoryMappingDO == null) {
                    continue;
                }
                CatProductBucketTO item = new CatProductBucketTO();
                item.setKey(maocheCategoryMappingDO.getId());
                item.setName(maocheCategoryMappingDO.getName());
                categories.add(item);
            }
        }

        response.setCategories(categories);
        return Result.OK(response);
    }

    @Resource
    private DaTaoKeApiService daTaoKeApiService;

    @RequestMapping(value = "/product/custom/getHistoryLowPriceList")
    @ResponseBody
    public Result<?> getHistoryLowPriceList(int pageId, int pageSize, String cids) {

        DaTaoKeResponse<Object> historyLowPriceList = daTaoKeApiService.getHistoryLowPriceList(
                "647802ed3a2b4",
                "v1.0.0",
                pageSize,
                pageId,
                cids,
                "0"
        );

        List<Long> historyLowPriceIds = DaTaoKeResponseHelper.getHistoryLowPriceIds(JsonUtils.toJSONString(historyLowPriceList.getData()));

        return Result.OK(historyLowPriceIds);
    }


    @RequestMapping(value = "/product/test/category")
    @ResponseBody
    public Result<?> testCategory() {

        CatUnionProductCondition condition = new CatUnionProductCondition();

        SearchSourceBuilder source = cgUnionProductService.searchSource(condition, cgUnionProductService::buildRootCategoryAgg, null, null, 0, 0);
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(source);

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



    @RequestMapping(value = "/product/test/autoGoodProductAudit")
    @ResponseBody
    public String autoGoodProductAudit() {

        autoProductService.autoGoodProductAudit();

        return "";
    }

    @RequestMapping(value = "/product/test/nightRcmd")
    @ResponseBody
    public String nightRcmd() {

        cgUnionProductStatisticsService.nineRcmd();

        return "";
    }

    @RequestMapping(value = "/product/test/autoOfflineProduct")
    @ResponseBody
    public String autoOfflineProduct() {

        autoProductService.autoOfflineProduct();

        return "";
    }

    @RequestMapping(value = "/product/test/autoNormalProductSaleAudit")
    @ResponseBody
    public String autoNormalProductSaleAudit() {

        autoProductService.autoNormalProductSaleAudit();

        return "";
    }

    // 9.9
    @RequestMapping(value = "/product/nine/list")
    @ResponseBody
    public Page<UnionProductTO> listNineProducts(@RequestBody CatNineProductTO search, HttpServletRequest request, HttpServletResponse response) {

        Page<UnionProductTO> page = new Page<>(request, response);
        if (search == null) {
            return page;
        }

        CatUnionProductCondition condition = ProductSearchHelper.buildNineSearchCondition();
        List<String> sorts = new ArrayList<>(Collections.singletonList("promotionPrice asc"));
        sorts.add("catDsr desc");
        condition.setSorts(sorts);

        int pageNo = Optional.ofNullable(search.getPageNo()).orElse(1);
        int pageSize = 10;
        int from = (pageNo - 1) * pageSize;
        int size = pageSize;

        long cidOne = NumberUtils.toLong(search.getCidOne());
        List<String> categoryNames = new ArrayList<>();
        if (cidOne > 0) {
            // 获取到类目映射
            List<MaocheCategoryMappingDO> categories = maocheCategoryMappingService.listByParentId(cidOne);
            if (CollectionUtils.isNotEmpty(categories)) {
                categoryNames.addAll(categories.stream().map(MaocheCategoryMappingDO::getName).toList());
            }

            condition.setCategoryNames(categoryNames);
        }

        SearchSourceBuilder source = cgUnionProductService.searchSource(condition, null, cgUnionProductService::commonSort, null, from, size);
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(source);
        if (searchData == null || searchData.getTotal() == 0) {
            page.setList(new ArrayList<>());
            page.setCount(0);
            return page;
        }

        long total = searchData.getTotal();
        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
        // 过滤无效额商品
        if (CollectionUtils.isNotEmpty(productTOs)) {
            productTOs = productTOs.stream().filter(i -> StringUtils.isNotBlank(i.getImgUrl())).collect(Collectors.toList());
        }

        Page<UnionProductTO> toPage = new Page<>(page.getPageNo() + 1, pageSize, total, productTOs);

        return toPage;
    }

    @Resource
    private MaocheAlimamaUnionProductPriceChartDao maocheAlimamaUnionProductPriceChartDao;

    @RequestMapping(value = "/product/price/chart/test")
    @ResponseBody
    public Result<?> testPriceTest() {

        List<MaocheAlimamaUnionProductPriceChartDO> allGroupByIid = maocheAlimamaUnionProductPriceChartDao.getAllGroupByIid();

        List<String> iids = allGroupByIid.stream().map(MaocheAlimamaUnionProductPriceChartDO::getIid).collect(Collectors.toList());


        List<List<String>> partition = Lists.partition(iids, 20);
        for (List<String> p : partition) {
            List<MaocheAlimamaUnionProductDO> list = maocheAlimamaUnionProductDao.listByIids(p);
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            List<Long> ids = list.stream().map(MaocheAlimamaUnionProductDO::getUiid).collect(Collectors.toList());
            Map<Long, String> idMap = list.stream().collect(Collectors.toMap(MaocheAlimamaUnionProductDO::getUiid, MaocheAlimamaUnionProductDO::getIid, (o1, o2) -> o1));

            CatUnionProductCondition condition = new CatUnionProductCondition();
            condition.setIds(ids);
            condition.setPriceChart(1L);

            SearchSourceBuilder source = cgUnionProductService.searchSource(condition, null, null, null, 0, 0);
            ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.search(source);
            if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
                log.info("testPriceTest p:{}", JsonUtils.toJSONString(p));
                break;
            }

            if (searchData.getDocuments().size() < p.size()) {
                for (CarAlimamaUnionProductIndex index : searchData.getDocuments()) {
                    idMap.put(index.getId(), null);
                }
                List<String> a = new ArrayList<>(idMap.values());
                a.remove(null);

                log.info("testPriceTest id:{}", StringUtils.join(a, ","));
            }
        }


        return Result.OK("完成");
    }



}
