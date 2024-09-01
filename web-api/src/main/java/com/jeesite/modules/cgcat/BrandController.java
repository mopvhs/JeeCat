package com.jeesite.modules.cgcat;


import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.aop.MaocheBrandIndex;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.MaocheSyncDataInfoService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibService;
import com.jeesite.modules.cat.service.cg.inner.InnerApiService;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
import com.jeesite.modules.cat.service.cg.third.VeApiService;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanStage;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.xxl.job.CgProductDeleteSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.CgProductSyncXxlJob;
import com.jeesite.modules.cgcat.dto.BrandVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class BrandController {

    @Resource
    private CommandService commandService;

    @Resource
    private CgProductDeleteSyncXxlJob cgProductDeleteSyncXxlJob;

    @Resource
    private CgProductSyncXxlJob cgProductSyncXxlJob;

    @Resource
    private CacheService cacheService;

    @Resource
    private InnerApiService innerApiService;

    @Resource
    private MaocheSyncDataInfoService maocheSyncDataInfoService;

    @Resource
    private MaocheRobotCrawlerMessageService maocheRobotCrawlerMessageService;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductService;

    @Resource
    private TbApiService tbApiService;

    @Resource
    private DingDanXiaApiService dingDanXiaApiService;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private OceanStage tbOceanStage;

    @Resource
    private OceanStage jdOceanStage;

    @Resource
    private VeApiService veApiService;


    @Resource
    private BrandLibService brandLibService;

    /**
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "brand/suggest")
    public Result<BrandVO> brandSuggest(@RequestBody BrandRequest request) {

        BrandVO brandVO = new BrandVO();
        brandVO.setBrands(new ArrayList<>());
        brandVO.setTotal(0);
        brandVO.setHasMore(false);

        if (request == null || StringUtils.isBlank(request.getKeyword())) {
            return Result.OK(brandVO);
        }
        int size = request.getSize();
        if (size <= 0) {
            size = 20;
        }
        size = 50;

        int page = request.getPage();
        if (page <= 0) {
            page = 1;
        }
        int from = 0;
        if (request.getPage() >= 1) {
            from = (page - 1) * size;
        }

        ElasticSearchData<MaocheBrandIndex, Object> searchData = brandLibService.suggestBrands(request.getKeyword(), request.getFirstSpell(), from, size);
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return Result.OK(brandVO);
        }

        long total = searchData.getTotal();
        boolean hasMore = total > (from + size);
        brandVO.setBrands(searchData.getDocuments().stream().filter(Objects::nonNull).map(brandIndex -> {
            BrandVO.Brand brand = new BrandVO.Brand();
            brand.setId(brandIndex.getId());
            brand.setBrand(brandIndex.getBrand());
            return brand;
        }).collect(Collectors.toList()));
        brandVO.setHasMore(hasMore);
        brandVO.setTotal(total);

        return Result.OK(brandVO);
    }

}
