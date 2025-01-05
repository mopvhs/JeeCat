package com.jeesite.modules.cgcat.brand.brandlib;


import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.text.PinyinUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.aop.MaocheBrandIndex;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.dao.MaocheBrandDao;
import com.jeesite.modules.cat.dao.MaocheBrandLibDao;
import com.jeesite.modules.cat.dao.MaocheBrandLibKeywordDao;
import com.jeesite.modules.cat.dao.MaochePushTaskDao;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.task.content.PushTaskContentDetail;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.MaocheSyncDataInfoService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibService;
import com.jeesite.modules.cat.service.cg.brand.BrandLibTaskService;
import com.jeesite.modules.cat.service.cg.inner.InnerApiService;
import com.jeesite.modules.cat.service.cg.task.dto.TaskDetail;
import com.jeesite.modules.cat.service.cg.third.DingDanXiaApiService;
import com.jeesite.modules.cat.service.cg.third.VeApiService;
import com.jeesite.modules.cat.service.cg.third.tb.TbApiService;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanStage;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.xxl.job.CgProductDeleteSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.CgProductSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.brand.BrandSyncXxlJob;
import com.jeesite.modules.cgcat.BrandRequest;
import com.jeesite.modules.cgcat.dto.BrandCreateRequest;
import com.jeesite.modules.cgcat.dto.BrandVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Resource
    private MaocheBrandDao maocheBrandDao;

    @Resource
    private MaocheBrandLibKeywordDao maocheBrandLibKeywordDao;

    @Resource
    private MaocheBrandLibDao maocheBrandLibDao;

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private BrandSyncXxlJob brandSyncXxlJob;

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

    /**
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "brand/del")
    public Result<String> deleteBrand(@RequestBody BrandRequest request) {

        MaocheBrandDO brandDO = maocheBrandDao.getById(request.getId());
        brandDO.setStatus("DELETE");
        long res = maocheBrandDao.updateStatus(brandDO);
        if (res <= 0) {
            return Result.ERROR(500, "删除失败");
        }

        elasticSearch7Service.delIndex(Collections.singletonList(brandDO.getIid()), ElasticSearchIndexEnum.MAOCHE_BRAND_INDEX);

        List<MaocheBrandLibDO> libs = maocheBrandLibDao.listByBrandIds(Collections.singletonList(request.getId()));
        // 修改状态
        for (MaocheBrandLibDO lib : libs) {
            lib.setStatus("DELETE");
            maocheBrandLibDao.updateStatus(lib);
            elasticSearch7Service.delIndex(Collections.singletonList(lib.getIid()), ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_INDEX);
        }

        List<Long> libIds = libs.stream().map(MaocheBrandLibDO::getIid).toList();
        List<MaocheBrandLibKeywordDO> keywords = maocheBrandLibKeywordDao.listByLibIds(libIds);
        // 修改状态
        for (MaocheBrandLibKeywordDO keyword : keywords) {
            keyword.setStatus("DELETE");
            maocheBrandLibKeywordDao.updateStatus(keyword);
            elasticSearch7Service.delIndex(Collections.singletonList(keyword.getIid()), ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_KEYWORD_INDEX);
        }

        return Result.OK("完成");
    }

    @RequestMapping(value = "brand/import")
    public Result<String> importBrand(@RequestParam(value = "file") MultipartFile file) {

        List<String> fileContents = getFileContents(file);
        Date date = new Date();
        for (String line : fileContents) {
            String[] split = line.split(",");
            String name = split[0];
            List<String> alias = getAlias(split);
            String icon = split[4];
            if (icon.equals("替换")) {
                icon = "";
            }

            MaocheBrandDO brand = maocheBrandDao.getByName(name);
            if (brand != null) {
                continue;
            }

            MaocheBrandDO brandDO = new MaocheBrandDO();
            brandDO.setName(name);
            brandDO.setIcon(icon);
            brandDO.setCreateTime(date);
            brandDO.setUpdateTime(date);
            brandDO.setCreateDate(date);
            brandDO.setUpdateDate(date);
            brandDO.setStatus("NORMAL");
            brandDO.setUpdateBy("admin");
            brandDO.setCreateBy("admin");
            Map<String, Object> remarks = new HashMap<>();
            remarks.put("alias", alias);
            brandDO.setRemarks(JsonUtils.toJSONString(remarks));
            // 获取首字母
            String firstSpell = PinyinUtils.getFirstSpell(name);
            if (StringUtils.isNotBlank(firstSpell)) {
                firstSpell = StringUtils.upperCase(firstSpell.substring(0, 1));
            }
            brandDO.setFirstSpell(firstSpell);
            maocheBrandDao.add(brandDO);
        }



        return Result.OK("完成");
    }

    @RequestMapping(value = "brand/create")
    public Result<String> createBrand(@RequestBody BrandCreateRequest createRequest) {

        if (createRequest == null || StringUtils.isBlank(createRequest.getBrand())) {
            return Result.ERROR(500, "品牌名不能为空");
        }

        MaocheBrandDO brand = maocheBrandDao.getByName(createRequest.getBrand());

        if (brand != null) {
            return Result.ERROR(500, "品牌已存在");
        }

        String name = createRequest.getBrand();
        String icon = createRequest.getIcon();

        Date date = new Date();
        MaocheBrandDO brandDO = new MaocheBrandDO();
        brandDO.setName(name);
        brandDO.setIcon(icon);
        brandDO.setCreateTime(date);
        brandDO.setUpdateTime(date);
        brandDO.setCreateDate(date);
        brandDO.setUpdateDate(date);
        brandDO.setStatus("NORMAL");
        brandDO.setUpdateBy("admin");
        brandDO.setCreateBy("admin");
        Map<String, Object> remarks = new HashMap<>();
        remarks.put("alias", new ArrayList<>());
        brandDO.setRemarks(JsonUtils.toJSONString(remarks));
        // 获取首字母
        String firstSpell = PinyinUtils.getFirstSpell(name);
        if (StringUtils.isNotBlank(firstSpell)) {
            firstSpell = StringUtils.upperCase(firstSpell.substring(0, 1));
        }
        brandDO.setFirstSpell(firstSpell);
        int add = maocheBrandDao.add(brandDO);
        if (add <= 0) {
            return Result.ERROR(500, "新增失败");
        }

        // 构建索引
        brandSyncXxlJob.index(Collections.singletonList(brandDO));

        return Result.OK("完成");
    }


    @Resource
    private BrandLibTaskService brandLibTaskService;

    @Resource
    private MaochePushTaskDao maochePushTaskDao;

    @RequestMapping(value = "brand/keyword/content/match")
    public Result<MaocheBrandLibKeywordDO> matchKeyword(@RequestParam String content) {

        MaocheBrandLibKeywordDO keywordDO = brandLibTaskService.matchBrandLib(content);

        return Result.OK(keywordDO);
    }

    @RequestMapping(value = "brand/keyword/content/match/check")
    public Result<String> matchKeyword() {

        // 获取全部的任务
        int num = 0;
        String id = "0";
        int limit = 100;
        boolean breakFlag = false;
        while (true) {
            List<String> ids = maochePushTaskDao.getIds(id, limit);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            id = ids.get(ids.size() - 1);
            num += ids.size();
            if (ids.size() < limit) {
                breakFlag = true;
            }
            List<MaochePushTaskDO> pushTasks = maochePushTaskDao.getByIds(ids);
            for(MaochePushTaskDO push : pushTasks) {
                String content = push.getContent();
                String checkContent = push.getTitle();

                PushTaskContentDetail detail = JsonUtils.toReferenceType(content, new TypeReference<PushTaskContentDetail>() {
                });
                if (detail != null) {
                    String detailDesc = detail.getDetail();
                    checkContent += detailDesc;
                }
                MaocheBrandLibKeywordDO keywordDO = brandLibTaskService.matchBrandLib(checkContent);
            }
            breakFlag = true;
            if (breakFlag) {
                break;
            }
        }

        return Result.OK("完成");
    }


    public static List<String> getAlias(String[] split) {
        List<String> alias = new ArrayList<>();
        String alias1 = split[1];
        String alias2 = split[2];
        String alias3 = split[3];
        if (!"替换".equals(alias1)) {
            alias.add(alias1);
        }
        if (!"替换".equals(alias2)) {
            alias.add(alias2);
        }
        if (!"替换".equals(alias3)) {
            alias.add(alias3);
        }
        return alias;
    }

    private List<String> getFileContents(MultipartFile uploadFile) {
        List<String> list = new ArrayList<>();

        BufferedReader br = null;
        InputStreamReader isr = null;
        // 从本地文件中读取信息
        try {
            // 组装数据信息
            isr = new InputStreamReader(uploadFile.getInputStream());
            br = new BufferedReader(isr);
            String readLineContent = null;

            while ((readLineContent = br.readLine()) != null) {

                list.add(readLineContent);
            }

        } catch (Exception e) {
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
            }
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (Exception e) {
            }

        }
        return list;
    }

}
