package com.jeesite.modules.cgcat;

import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionTitleKeywordDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.entity.MaocheCategoryProductRelDO;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.CatActivityEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatAggHelper;
import com.jeesite.modules.cat.helper.CategoryHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.ProductAuditRequest;
import com.jeesite.modules.cat.model.ProductCategoryModel;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.model.keytitle.UnionProductTagModel;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheCategoryProductRelService;
import com.jeesite.modules.cat.service.MaocheCategoryService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cgcat.dto.ProductCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/backend/api/")
public class CgProductController {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private MaocheAlimamaUnionTitleKeywordDao maocheAlimamaUnionTitleKeywordDao;

    @Resource
    private MaocheCategoryService maocheCategoryService;

    @Resource
    private MaocheCategoryProductRelService maocheCategoryProductRelService;


    // 商品库
    @RequestMapping(value = "/product/warehouse/detail")
    @ResponseBody
    public Page<UnionProductTO> productWarehouseDetail(CatUnionProductCondition condition, HttpServletRequest request, HttpServletResponse response) {

        Page<UnionProductTO> page = new Page<>(request, response);
        if (condition == null) {
            return page;
        }

        List<String> sorts = Optional.ofNullable(condition.getSorts()).orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(sorts)) {
            sorts.add("volume desc");
        }

        int from = (page.getPageNo() - 1) * page.getPageSize();
        int size = page.getPageSize();
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition, null, from, size);
        if (searchData == null) {
            return page;
        }
        long total = searchData.getTotal();
        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
        Page<UnionProductTO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), total, productTOs);

        return toPage;
    }

    // 选品库
    @RequestMapping(value = "/product/selection/detail")
    @ResponseBody
    public Page<UnionProductTO> productSelectionDetail(CatUnionProductCondition condition, HttpServletRequest request, HttpServletResponse response) {

        Page<UnionProductTO> page = new Page<>(request, response);
        if (condition == null) {
            return page;
        }

        List<String> sorts = Optional.ofNullable(condition.getSorts()).orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(sorts)) {
            sorts.add("catDsr desc");
        }

        // 选品库都是审核通过的商品
        condition.setAuditStatus(AuditStatusEnum.PASS.getStatus());

        int from = (page.getPageNo() - 1) * page.getPageSize();
        int size = page.getPageSize();
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(
                condition,
                null,
                from,
                size);
        if (searchData == null) {
            return page;
        }
        long total = searchData.getTotal();
        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
        Page<UnionProductTO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), total, productTOs);

        return toPage;
    }

    // 选品库顶部tab
    @RequestMapping(value = "/product/selection/tab")
    @ResponseBody
    public Result<ProductCategoryVO> productSelectionDetail() {

        CatUnionProductCondition condition = new CatUnionProductCondition();
        // 选品库都是审核通过的商品
        condition.setAuditStatus(AuditStatusEnum.PASS.getStatus());

        int from = 0;
        int size = 0;
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(
                condition,
                CatAggHelper::buildSelectionTabAgg,
                from,
                size);
        if (searchData == null) {
            return Result.ERROR(500, "查询异常");
        }

        Map<String, List<CatProductBucketTO>> bucketMap = searchData.getBucketMap();
        if (MapUtils.isEmpty(bucketMap) || CollectionUtils.isEmpty(bucketMap.get("groupBySaleStatus"))) {
            return Result.ERROR(404, "资源不存在");
        }
        List<CatProductBucketTO> groupBySaleStatus = bucketMap.get("groupBySaleStatus");
        ProductCategoryVO tab = new ProductCategoryVO();
        List<ProductCategoryVO> tabList = new ArrayList<>();
        // 全部数量
        long count = 0;
        for (CatProductBucketTO bucket : groupBySaleStatus) {
            long status = NumberUtils.toLong(bucket.getName());
            bucket.setKey(bucket.getName());

            count += bucket.getCount();
            if (SaleStatusEnum.INIT.getStatus().equals(status)) {
                bucket.setName("待上架");
            } else if (SaleStatusEnum.ON_SHELF.getStatus().equals(status)) {
                bucket.setName("上架中");
            } else if (SaleStatusEnum.OFF_SHELF.getStatus().equals(status)) {
                bucket.setName("已下架");
            }
        }
        // 全部
        CatProductBucketTO all = new CatProductBucketTO();
        all.setKey(null);
        all.setName("全部");
        all.setCount(count);

        groupBySaleStatus.add(0, all);

        tab.setCategories(groupBySaleStatus);

        return Result.OK(tab);
    }

    // 有好价
    @RequestMapping(value = "/product/good/warehouse/detail")
    @ResponseBody
    public Page<UnionProductTO> productGoodWarehouseDetail(CatUnionProductCondition condition, HttpServletRequest request, HttpServletResponse response) {

        Page<UnionProductTO> page = new Page<>(request, response);
        if (condition == null) {
            return page;
        }

        // https://rp.mockplus.cn/run/eSnLjDgFoGgdM/ZaPWdd3ge7M-?cps=expand&rps=expand&nav=1&ha=0&la=0&fc=0&out=1&rt=1
        // 入库的商品不在有好价页面显示
        condition.setNotAuditStatus(AuditStatusEnum.PASS.getStatus()
        );
        condition.setActivity(Collections.singletonList(CatActivityEnum.GOOD_PRICE.getActivity()));

        List<String> sorts = Optional.ofNullable(condition.getSorts()).orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(sorts)) {
            sorts.add("volume desc");
        }

        int from = (page.getPageNo() - 1) * page.getPageSize();
        int size = page.getPageSize();
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition, null, from, size);
        if (searchData == null) {
            return page;
        }
        long total = searchData.getTotal();
        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
        Page<UnionProductTO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), total, productTOs);

        return toPage;
    }

    // 商品topN类目
    @RequestMapping(value = "/product/top/category")
    @ResponseBody
    public Result<ProductCategoryVO> topCategory(CatUnionProductCondition condition, HttpServletRequest request, HttpServletResponse response) {

        if (condition == null) {
            return Result.ERROR(500, "参数错误");
        }

        condition.setCategoryName(null);
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition, cgUnionProductService::buildWarehouseAgg, 0, 0);
        if (searchData == null) {
            return Result.ERROR(500, "查询异常");
        }

        List<CatProductBucketTO> carProductBucketTOs = new ArrayList<>();
        Map<String, List<CatProductBucketTO>> bucketMap = searchData.getBucketMap();
        for (Map.Entry<String, List<CatProductBucketTO>> entry : bucketMap.entrySet()) {
            carProductBucketTOs.addAll(entry.getValue());
        }
        ProductCategoryVO categoryVO = new ProductCategoryVO();
        categoryVO.setCategories(carProductBucketTOs);
        categoryVO.setTotal(searchData.getTotal());
        return Result.OK(categoryVO);
    }

    // 审核状态变更
    @RequestMapping(value = "/product/audit/status/change")
    @ResponseBody
    public Result<Object> changeAuditStatus(@RequestBody ProductAuditRequest request) {

        if (request == null || CollectionUtils.isEmpty(request.getIds()) || request.getAuditStatus() == null) {
            return Result.ERROR(404, "参数错误");
        }

        // 批量更新
        List<Long> ids = request.getIds().stream().distinct().collect(Collectors.toList());

        boolean row = maocheAlimamaUnionProductService.updateAuditStatus(ids, request.getAuditStatus());

        if (!row) {
            return Result.ERROR(500, "更新失败");
        }

        // 更新索引
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductDao.listByIds(ids);

        cgUnionProductService.indexEs(productDOs, 10);

        return Result.OK("入库完成");
    }

    // 审核状态变更
    @RequestMapping(value = "/product/sale/status/change")
    @ResponseBody
    public Result<Object> changeSaleStatus(@RequestBody ProductAuditRequest request) {

        if (request == null || CollectionUtils.isEmpty(request.getIds()) || request.getSaleStatus() == null) {
            return Result.ERROR(404, "参数错误");
        }

        // 批量更新
        List<Long> ids = request.getIds().stream().distinct().collect(Collectors.toList());

        Date onShelfDate = null;
        if (request.getSaleStatus().equals(SaleStatusEnum.ON_SHELF.getStatus())) {
            onShelfDate = new Timestamp(System.currentTimeMillis());
        }

        String time = onShelfDate != null ? onShelfDate.toString() : null;
        int auditStatus = maocheAlimamaUnionProductDao.updateSaleStatus(ids, request.getSaleStatus(), time);

        if (auditStatus <= 0) {
            return Result.ERROR(500, "更新失败");
        }

        // 更新索引
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductDao.listByIds(ids);

        cgUnionProductService.indexEs(productDOs, 10);

        return Result.OK("入库完成");
    }


    // 审核状态变更
    @RequestMapping(value = "/product/tag/update")
    @ResponseBody
    public Result<Object> updateProductTag(@RequestBody ProductAuditRequest request) {

        if (request == null || request.getId() == null || request.getId() <= 0 || request.getTag() == null) {
            return Result.ERROR(500, "参数错误");
        }

        MaocheAlimamaUnionProductDO where = new MaocheAlimamaUnionProductDO();
        where.setId(String.valueOf(request.getId()));
        // 更新索引
        MaocheAlimamaUnionProductDO productDO = maocheAlimamaUnionProductDao.getByEntity(where);
        if (productDO == null) {
            return Result.ERROR(404, "资源不存在");
        }
        // 适配格式
        UnionProductTagModel tagModel = new UnionProductTagModel(request.getTag());

        // 判断标签表是否存在
        MaocheAlimamaUnionTitleKeywordDO query = new MaocheAlimamaUnionTitleKeywordDO();
        query.setItemId(productDO.getItemId());
        MaocheAlimamaUnionTitleKeywordDO entity = maocheAlimamaUnionTitleKeywordDao.getByEntity(query);
        if (entity == null) {
//            MaocheAlimamaUnionTitleKeywordDO add = new MaocheAlimamaUnionTitleKeywordDO();
            return Result.ERROR(404, "标签更新失败，提词资源不存在");
        } else {
            // 更新标签
            int row = maocheAlimamaUnionTitleKeywordDao.updateTag(entity.getIid(), JsonUtils.toJSONString(tagModel));
            if (row <= 0) {
                return Result.ERROR(400, "标签更新失败");
            }

            cgUnionProductService.indexEs(List.of(productDO), 1);
        }

        return Result.OK("入库完成");
    }


    // 审核状态变更
    @RequestMapping(value = "/product/category/tree")
    @ResponseBody
    public Result<Object> categories() {

        List<CategoryTree> categoryTrees = maocheCategoryService.listAllCategoryFromCache();

        List<String> itemIds = Collections.singletonList("ejQK2wSGoORnm67h93");
        List<MaocheCategoryProductRelDO> maocheCategoryProductRelDOS = maocheCategoryProductRelService.listByItemIdSuffixs(itemIds);
        ProductCategoryModel productCategory = CategoryHelper.getProductCategory(maocheCategoryProductRelDOS, categoryTrees);

        return Result.OK(JsonUtils.toJSONString(productCategory));
    }

}