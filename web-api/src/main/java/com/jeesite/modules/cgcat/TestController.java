package com.jeesite.modules.cgcat;

import com.google.common.collect.Lists;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.config.Global;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.text.PinyinUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.utils.excel.ExcelImport;
import com.jeesite.common.utils.excel.v2.ExcelUtils;
import com.jeesite.common.validator.ValidatorUtils;
import com.jeesite.common.web.Result;
import com.jeesite.common.web.http.HttpClientUtils;
import com.jeesite.modules.cat.dao.MaocheBrandDao;
import com.jeesite.modules.cat.dao.MaocheBrandLibDao;
import com.jeesite.modules.cat.dao.MaocheBrandLibKeywordDao;
import com.jeesite.modules.cat.dao.MaocheTagDao;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.entity.MaocheTagDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.MaocheBrandLibMapper;
import com.jeesite.modules.cat.service.OkHttpService;
import com.jeesite.modules.cat.service.cg.brandlib.BrandLibBizService;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibKeywordCreateRequest;
import com.jeesite.modules.cat.xxl.job.CgProductSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.ocean.AIOceanXxlJob;
import com.jeesite.modules.cat.xxl.job.task.SyncOceanSimilarXxlJob;
import com.jeesite.modules.cgcat.dto.BrandLibImportDTO;
import com.jeesite.modules.cgcat.dto.PushTaskRuleKeywordRequest;
import com.jeesite.modules.sys.entity.Office;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class TestController {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private SyncOceanSimilarXxlJob syncOceanSimilarXxlJob;

    @Resource
    private CgProductSyncXxlJob cgProductSyncXxlJob;

    @Resource
    private MaocheBrandDao maocheBrandDao;

    @Resource
    private MaocheBrandLibMapper maocheBrandLibMapper;

    @Resource
    private MaocheBrandLibKeywordDao maocheBrandLibKeywordDao;

    @Resource
    private BrandLibBizService brandLibBizService;

    @Resource
    private CgPushTaskRuleController cgPushTaskRuleController;

    @Resource
    private MaocheTagDao maocheTagDao;

    @RequestMapping(value = "/test/es/maoche_message_sync_index/similar/update")
    public String update(@Param("iids") String iids) {

        if (iids.equals("0")) {
            try {
                syncOceanSimilarXxlJob.execute();
            } catch (Exception e) {

            }

            return "最近n条更新完成";
        }

        List<Map<String, Object>> data = new ArrayList<>();
        String[] split = iids.split(",");
        for (String p : split) {
            Map<String, Object> messageSyncIndex = new HashMap<>();
            messageSyncIndex.put("id", NumberUtils.toLong(p));
            messageSyncIndex.put("status", "SIMILAR");
            data.add(messageSyncIndex);
        }

        elasticSearch7Service.update(data, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);

        return "success";
    }

    @RequestMapping(value = "/test/es/product/all/index")
    public String productIndex() throws Exception {


        cgProductSyncXxlJob.execute();
        return "success";
    }

    @RequestMapping(value = "/test/jsoup")
    public String jsoup(String url) throws Exception {
        Document document = Jsoup.connect(url).get();
        // 使用Jsoup连接到网页
        Document doc = Jsoup.connect(url).get();
        // 获取HTML内容
        String html = doc.html();

        String s1 = OkHttpService.doGetHtmlWithProxy(url);

        Map<String, String> data = new HashMap<>();
        data.put("url", html);
        String s = FlameHttpService.doPost("https://wx.mtxtool.com/cat_url_decrypt", JsonUtils.toJSONString(data));

        return s;
    }

    @Resource
    private AIOceanXxlJob aiOceanXxlJob;

    @RequestMapping(value = "/test/aiOceanXxlJob")
    public String aiOceanXxlJob() throws Exception {
        aiOceanXxlJob.execute();
        return "执行完成";
    }

    @RequestMapping(value = "/test/import/excel")
    public String importExcel(@RequestParam(value = "file")MultipartFile file) throws IOException {

        int num = 0;
        int failureNum = 0;
        int total = 0;
        try {
            List<BrandLibImportDTO> read = ExcelUtils.read(file, BrandLibImportDTO.class);
            total = read.size();

            Map<String, Map<String, List<String>>> libKeyTagMap = new HashMap<>();
            Map<String, String> englishMap = new HashMap<>();
            Map<String, MaocheTagDO> tagDOMap = new HashMap<>();

            Set<String> brandCns = new HashSet<>();
            for (BrandLibImportDTO dto : read) {
                String brandCn = dto.getBrandCn();
                String brandEn = dto.getBrandEn();
                if (StringUtils.isBlank(brandCn) && StringUtils.isBlank(brandEn)) {
//                    log.info("importExcel brandCn is null dto {} \n", JsonUtils.toJSONString(read));
                    continue;
                }

                if (StringUtils.isBlank(dto.getProductName())) {
//                    log.info("importExcel keyword is null dto {} \n", JsonUtils.toJSONString(read));
                    continue;
                }

                List<String> tags = new ArrayList<>();
                if (StringUtils.isNotBlank(dto.getKeywords())) {
                    String[] tagArr = dto.getKeywords().split(",");
                    // 把品牌和关键词关联
                    tags.addAll(Arrays.stream(tagArr).toList());
                }

                String brandName = Optional.ofNullable(brandCn).orElse(brandEn);
                Map<String, List<String>> keywordTagMap = libKeyTagMap.get(brandName);
                if (MapUtils.isEmpty(keywordTagMap)) {
                    keywordTagMap = new HashMap<>();
                }

                keywordTagMap.put(dto.getProductName(), tags);
                brandName = StringUtils.lowerCase(brandName);
                libKeyTagMap.put(brandName, keywordTagMap);

                // 如果英文名称存在，进行关联
                if (StringUtils.isNotBlank(dto.getBrandEn())) {
                    englishMap.put(brandName, dto.getBrandEn());
                }
                brandCns.add(brandName);
            }

            List<List<String>> list = Lists.partition(new ArrayList<>(brandCns), 10);
            // 10个10个品牌处理
            for (List<String> brandNames : list) {
                List<MaocheBrandDO> brands = maocheBrandDao.listByNames(brandNames);
                Map<String, MaocheBrandDO> brandMap = brands.stream().collect(Collectors.toMap(i -> StringUtils.lowerCase(i.getName()), Function.identity(), (o1, o2) -> o1));

                for (String brandName : brandNames) {
                    try {
                        MaocheBrandDO brandDO = brandMap.get(brandName);
                        MaocheBrandLibDO libDO = null;
                        // 如果不存在，需要创建
                        if (brandDO == null) {
                            brandDO = new MaocheBrandDO();

                            brandDO.setName(brandName);
                            brandDO.setIcon("");
                            brandDO.setCreateTime(new Date());
                            brandDO.setUpdateTime(new Date());
                            brandDO.setCreateDate(new Date());
                            brandDO.setUpdateDate(new Date());
                            brandDO.setStatus("NORMAL");
                            brandDO.setUpdateBy("admin");
                            brandDO.setCreateBy("admin");
                            Map<String, Object> remarks = new HashMap<>();
                            remarks.put("alias", "");
                            brandDO.setRemarks(JsonUtils.toJSONString(remarks));
                            // 获取首字母
                            String firstSpell = PinyinUtils.getFirstSpell(brandName);
                            if (StringUtils.isNotBlank(firstSpell)) {
                                firstSpell = StringUtils.upperCase(firstSpell.substring(0, 1));
                            }
                            brandDO.setFirstSpell(firstSpell);

                            int add = maocheBrandDao.add(brandDO);
                            if (add > 0) {
                                // 写入brandlib
                                libDO = maocheBrandLibMapper.addByBrand(brandDO);
                            }
                        } else {
                            libDO = maocheBrandLibMapper.getByBrandId(brandDO.getIid());
                        }

                        if (libDO == null) {
                            libDO = maocheBrandLibMapper.addByBrand(brandDO);
                        }

                        // 获取关键词
                        Map<String, List<String>> listMap = libKeyTagMap.get(brandName);
                        Set<String> keywords = listMap.keySet();

                        // 获取所有的标签，判断是否存在
                        for (Map.Entry<String, List<String>> entry : listMap.entrySet()) {
                            List<String> value = entry.getValue();
                            if (CollectionUtils.isEmpty(value)) {
                                continue;
                            }
                            // 查询标签，不存在则创建
                            List<MaocheTagDO> dos = maocheTagDao.listByNames(value);
                            // 如果不存在的，则新增
                            Map<String, MaocheTagDO> doMap = dos.stream().collect(Collectors.toMap(MaocheTagDO::getTagName, Function.identity(), (o1, o2) -> o1));
                            if (MapUtils.isNotEmpty(doMap)) {
                                tagDOMap.putAll(doMap);
                            }
                            for (String tag : value) {
                                MaocheTagDO tagDO = tagDOMap.get(tag);
                                if (tagDO != null) {
                                    continue;
                                }
                                tagDO = new MaocheTagDO();
                                tagDO.setLevel(1L);
                                tagDO.setParentId(0L);
                                tagDO.setTagName(tag);
                                tagDO.setCreateBy("admin");
                                tagDO.setUpdateBy("admin");
                                tagDO.setUpdateDate(new Date());
                                tagDO.setCreateDate(new Date());
                                tagDO.setRemarks("");
                                tagDO.setStatus("NORMAL");
                                // 新增
                                int add = maocheTagDao.add(tagDO);
                                if (add > 0) {
                                    tagDOMap.put(tagDO.getTagName(), tagDO);
                                }
                            }
                        }

                        // 如果不存在，则创建，存在则忽略
                        List<MaocheBrandLibKeywordDO> keywordDOs = maocheBrandLibKeywordDao.listByKeywords(libDO.getIid(), new ArrayList<>(keywords));

                        addIfAbsent(libDO, keywordDOs, keywords, listMap, tagDOMap);
                    } catch (Exception e) {
                        log.error(" error brandName:{}", brandName, e);
                    }
                }

            }

        } catch (Exception e) {
            throw e;
        }

        return "处理完成，成功：" + num + " 失败：" + failureNum + " 总量：" + total;
    }

    /**
     *
     * @param libDO
     * @param keywordDOs
     * @param keywords
     * @param libTagRelMap 关键词和标签
     */
    private void addIfAbsent(MaocheBrandLibDO libDO, List<MaocheBrandLibKeywordDO> keywordDOs, Set<String> keywords, Map<String, List<String>> libTagRelMap, Map<String, MaocheTagDO> tagDOMap) {
        keywordDOs = Optional.ofNullable(keywordDOs).orElse(new ArrayList<>());

        Map<String, MaocheBrandLibKeywordDO> map = keywordDOs.stream().collect(Collectors.toMap(MaocheBrandLibKeywordDO::getKeyword, Function.identity(), (o1, o2) -> o1));

        for (String keyword : keywords) {
            MaocheBrandLibKeywordDO keywordDO = map.get(keyword);
            if (keywordDO != null) {
                continue;
            }

            List<String> tags = libTagRelMap.get(keyword);
            List<Long> tagIds = new ArrayList<>();

            BrandLibKeywordCreateRequest request = new BrandLibKeywordCreateRequest();
            request.setKeyword(keyword);
            request.setBrandLibId(libDO.getIid());
            if (CollectionUtils.isNotEmpty(tags)) {
                for (String tag : tags) {
                    MaocheTagDO tagDO = tagDOMap.get(tag);
                    if (tagDO == null) {
                        continue;
                    }
                    tagIds.add(tagDO.getIid());
                }
            }

            request.setTagIds(tagIds);

            PushTaskRuleKeywordRequest categoryReq = new PushTaskRuleKeywordRequest();
            categoryReq.setKeywords(Collections.singletonList(keyword));
            // 查询关键词的类目
            Result<Map<String, Object>> res = cgPushTaskRuleController.getKeywordCategory(categoryReq);
            if (res.isSuccess()) {
                Map<String, Object> result = res.getResult();
                request.setCategoryName(String.valueOf(result.get("categoryName")));
                request.setLevelOneCategoryName(String.valueOf(result.get("levelOneCategoryName")));
            }

            // 新增
            brandLibBizService.createBrandLibKeyword(request);
        }
    }


}
