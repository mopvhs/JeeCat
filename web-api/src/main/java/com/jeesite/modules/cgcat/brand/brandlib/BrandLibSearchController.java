package com.jeesite.modules.cgcat.brand.brandlib;


import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.aop.MaocheBrandIndex;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.brandlib.BrandLibIndex;
import com.jeesite.modules.cat.service.cg.brand.dto.BrandDTO;
import com.jeesite.modules.cat.service.cg.brandlib.BrandLibBizService;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandCondition;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibBrandCondition;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibCondition;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibInfoDTO;
import com.jeesite.modules.cat.service.es.BrandEsService;
import com.jeesite.modules.cat.service.es.BrandLibEsService;
import com.jeesite.modules.cat.xxl.job.brand.BrandLibSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.brand.BrandSyncXxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 品牌库
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}")
public class BrandLibSearchController {

    @Resource
    private BrandLibBizService brandLibBizService;

    @Resource
    private BrandSyncXxlJob brandSyncXxlJob;

    @Resource
    private BrandLibSyncXxlJob brandLibSyncXxlJob;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private BrandLibEsService brandLibEsService;

    @Resource
    private BrandEsService brandEsService;

    /**
     * 品牌列表
     * @param query
     * @return
     */
    @RequestMapping(value = "/api/brand/lib/brand")
    public Result<List<MaocheBrandIndex>> getBrand(@RequestBody BrandLibBrandCondition condition) {
        if (condition == null) {
            return Result.ERROR(500, "参数错误");
        }

        String firstSpell = condition.getFirstSpell();
        condition.setFirstSpell(firstSpell);

        SearchSourceBuilder source = new SearchSourceBuilder();
        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, BrandLibBrandCondition.class);
        source.from(0);
        source.size(10000);
        source.query(boolBuilder);

        ElasticSearchData<MaocheBrandIndex, Object> searchData = elasticSearch7Service.search(source,
                ElasticSearchIndexEnum.MAOCHE_BRAND_INDEX,
                CatRobotHelper::convertMaocheBrand, null);

        if (searchData == null) {
            return Result.ERROR(500, "搜索异常");
        }

        List<MaocheBrandIndex> documents = searchData.getDocuments();


        return Result.OK(documents);
    }

    // 品牌库-新
    @RequestMapping(value = "/api/brand/lib/get")
    @ResponseBody
    public Page<BrandLibInfoDTO> getBrandLib(BrandLibCondition condition, HttpServletRequest request, HttpServletResponse response) {

        Page<BrandLibInfoDTO> page = new Page<>(request, response);
        if (condition == null) {
            return page;
        }
        
        if (condition.getBrandId() == null || condition.getBrandId() <= 0) {
            return page;
        }

        List<String> sorts = Optional.ofNullable(condition.getSorts()).orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(sorts)) {
            sorts.add("updateTime desc");
            condition.setSorts(sorts);
        }

        int from = (page.getPageNo() - 1) * page.getPageSize();
        int size = page.getPageSize();
        // BrandLibTO
        ElasticSearchData<BrandLibIndex, CatProductBucketTO> searchData = brandLibEsService.searchBrandLib(condition, null, null, from, size);
        if (searchData == null) {
            return page;
        }
        long total = searchData.getTotal();
        List<BrandLibInfoDTO> resources = brandLibBizService.listBrandLibs(searchData);

        Page<BrandLibInfoDTO> toPage = new Page<>(page.getPageNo() + 1, page.getPageSize(), total, resources);

        return toPage;
    }

    private void productNameQuery(BrandLibCondition condition, BoolQueryBuilder builder) {
        String name = condition.getName();
        if (StringUtils.isEmpty(name)) {
            return;
        }
        BoolQueryBuilder should = new BoolQueryBuilder();

        should.should(QueryBuilders.matchPhraseQuery("productName", name).slop(100));
        should.should(QueryBuilders.matchPhraseQuery("aliasNames", name).slop(100));

        builder.must(should);
    }


    // 品牌
    @RequestMapping(value = "/api/brand/get")
    @ResponseBody
    public List<BrandDTO> getBrand(@RequestBody BrandCondition condition) {

        // BrandLibTO
//        ElasticSearchData<MaocheBrandIndex, Object> searchData = brandEsService.searchBrand(condition, null, null, 0, 10000);
//        if (searchData == null) {
//            return new ArrayList<>();
//        }

        BrandLibCondition lib  = new BrandLibCondition();

        lib.setFirstSpell(condition.getFirstSpell());
        lib.setName(condition.getName());

        ElasticSearchData<BrandLibIndex, CatProductBucketTO> searchData = brandLibEsService.searchBrandLib(lib, null, this::productNameQuery, 0, 10000);
        if (searchData == null) {
            return new ArrayList<>();
        }
        // 结构化
        List<BrandLibIndex> documents = searchData.getDocuments();
        if (CollectionUtils.isEmpty(documents)) {
            return new ArrayList<>();
        }

        BrandCondition brand = new BrandCondition();
        brand.setIds(documents.stream().map(BrandLibIndex::getBrandId).collect(Collectors.toList()));
        ElasticSearchData<MaocheBrandIndex, Object> brandList = brandEsService.searchBrand(brand, null, null, 0, 10000);

        List<BrandDTO> convert = BrandDTO.convert(brandList.getDocuments());
        return convert;
    }
}
