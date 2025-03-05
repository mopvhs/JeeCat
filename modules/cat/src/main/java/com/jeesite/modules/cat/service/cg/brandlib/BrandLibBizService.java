package com.jeesite.modules.cat.service.cg.brandlib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheBrandDao;
import com.jeesite.modules.cat.dao.MaocheBrandLibDao;
import com.jeesite.modules.cat.dao.MaocheBrandLibKeywordDao;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.entity.MaocheTagDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CategoryHelper;
import com.jeesite.modules.cat.model.BrandLibKeywordIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.brandlib.BrandLibIndex;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaocheTagService;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibCreateRequest;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibDTO;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibInfoDTO;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibKeywordCreateRequest;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibKeywordDTO;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibKeywordUpdateDTO;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibUpdateDTO;
import com.jeesite.modules.cat.service.cg.brandlib.dto.LibCategoryDetail;
import com.jeesite.modules.cat.service.cg.brandlib.dto.TagDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BrandLibBizService {

    @Resource
    private MaocheBrandLibDao maocheBrandLibDao;

    @Resource
    private MaocheBrandLibKeywordDao maocheBrandLibKeywordDao;
    @Resource
    private MaocheBrandDao maocheBrandDao;
    @Resource
    private MaocheTagService maocheTagService;
    @Resource
    private MaocheCategoryMappingService maocheCategoryMappingService;
    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    public Result<MaocheBrandLibDO> createBrandLib(BrandLibCreateRequest request) {

        if (request == null) {
            return Result.ERROR(500, "参数异常");
        }

        MaocheBrandLibDO where = new MaocheBrandLibDO();
        where.setBrandId(request.getBrandId());
        MaocheBrandLibDO entity = maocheBrandLibDao.getByEntity(where);
        if (entity != null) {
            return Result.OK(entity);
        }

        Date date = new Date();

        MaocheBrandDO brandDO = maocheBrandDao.getById(request.getBrandId());
        if (brandDO == null) {
            return Result.ERROR(500, "品牌不存在");
        }

        entity = new MaocheBrandLibDO();
        entity.setBrandId(request.getBrandId());
        // 待定
        entity.setProductName(brandDO.getName());
        entity.setAliasNames(JsonUtils.toJSONString(request.getAliasNames()));
        entity.setBlacklist(BooleanUtils.isTrue(request.getBlacklist()) ? "true" : "false");
        entity.setCreateBy("admin");
        entity.setUpdateBy("admin");
        entity.setUpdateDate(date);
        entity.setCreateDate(date);
        entity.setStatus("NORMAL");
        entity.setRemarks("");

        long insert = maocheBrandLibDao.add(entity);
        if (insert > 0) {
            return Result.OK(entity);
        }

        indexBrandLib(entity);

        return Result.ERROR(500, "新增失败");
    }

    public void indexBrandLib(MaocheBrandLibDO lib) {
        if (lib == null) {
            return;
        }
        Long brandId = lib.getBrandId();
        MaocheBrandDO brandDO = maocheBrandDao.getById(brandId);

        List<MaocheBrandLibKeywordDO> keywords = maocheBrandLibKeywordDao.listByLibIds(Collections.singletonList(lib.getIid()));

        Map<String, Object> libMap = BrandLibIndex.toIndexMap(lib, brandDO, keywords);
        if (MapUtils.isEmpty(libMap)) {
            return;
        }

        elasticSearch7Service.index(libMap, ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_INDEX, lib.getId());
    }

    public Result<MaocheBrandLibKeywordDO> createBrandLibKeyword(BrandLibKeywordCreateRequest request) {

        if (request == null) {
            return Result.ERROR(500, "参数异常");
        }

        MaocheBrandLibDO where = new MaocheBrandLibDO();
        where.setId(String.valueOf(request.getBrandLibId()));
        MaocheBrandLibDO libDO = maocheBrandLibDao.getByEntity(where);
        if (libDO == null) {
            return Result.ERROR(404, "品牌库不存在");
        }

        // 获取当前品牌的关键词列表
        List<MaocheBrandLibKeywordDO> keywords = maocheBrandLibKeywordDao.listByLibIds(Collections.singletonList(request.getBrandLibId()));
        if (CollectionUtils.isNotEmpty(keywords)) {
            for(MaocheBrandLibKeywordDO item : keywords) {
                if (item.getKeyword().equals(request.getKeyword())) {
                    return Result.ERROR(500, "不可以创建重复关键词");
                }
            }
        }

        Date date = new Date();

        MaocheBrandLibKeywordDO entity = new MaocheBrandLibKeywordDO();
        entity.setCategoryName(request.getCategoryName());
        entity.setLevelOneCategoryName(request.getLevelOneCategoryName());
        entity.setKeyword(request.getKeyword());
        entity.setBrandLibId(request.getBrandLibId());
        entity.setAliasNames(JsonUtils.toJSONString(request.getAliasNames()));
        if (request.getTagIds() != null) {
            entity.setTags(JsonUtils.toJSONString(request.getTagIds()));
        }
        if (request.getSpecialTagIds() != null) {
            entity.setSpecialTags(JsonUtils.toJSONString(request.getSpecialTagIds()));
        }

        entity.setCreateBy("admin");
        entity.setUpdateBy("admin");
        entity.setUpdateDate(date);
        entity.setCreateDate(date);
        entity.setStatus("NORMAL");
        entity.setRemarks("");

        long insert = maocheBrandLibKeywordDao.add(entity);
        if (insert > 0) {
            return Result.OK(entity);
        }

        return Result.ERROR(500, "新增失败");
    }

    public List<BrandLibInfoDTO> listBrandLibs(ElasticSearchData<BrandLibIndex, CatProductBucketTO> searchData) {

        if (searchData == null) {
            return new ArrayList<>();
        }

        List<BrandLibIndex> documents = searchData.getDocuments();
        // 查询品牌信息和关键词，数据补全
        List<Long> ids = documents.stream().map(BrandLibIndex::getId).toList();
        // 品牌id
        List<Long> brandIds = documents.stream().map(BrandLibIndex::getBrandId).toList();
        if (CollectionUtils.isEmpty(brandIds)) {
            return new ArrayList<>();
        }
        List<MaocheBrandDO> brands = maocheBrandDao.listByIds(brandIds);
        if (CollectionUtils.isEmpty(brands)) {
            return new ArrayList<>();
        }

        // 获取类目树
        Map<String, List<CategoryTree>> categoryMap = maocheCategoryMappingService.getCategoryMap();

        // 获取品牌库
        List<MaocheBrandLibDO> libs = maocheBrandLibDao.listByIds(ids);
        Map<Long, MaocheBrandLibDO> libMap = libs.stream().collect(Collectors.toMap(MaocheBrandLibDO::getIid, Function.identity(), (o1, o2) -> o1));
        List<Long> tagIds = new ArrayList<>();
        Map<Long, MaocheBrandDO> brandMap = brands.stream().collect(Collectors.toMap(MaocheBrandDO::getIid, Function.identity(), (o1, o2) -> o1));
        // 获取相关关键词
        List<MaocheBrandLibKeywordDO> libKeywords = maocheBrandLibKeywordDao.listByLibIds(ids);
        // 获取标签信息
        // 分桶
        Map<Long, List<MaocheBrandLibKeywordDO>> brandLibKeywordGroup = new HashMap<>();
        for (MaocheBrandLibKeywordDO keywordDO : libKeywords) {
            if (keywordDO == null) {
                continue;
            }

            tagIds.addAll(getTagIds(keywordDO.getTags()));

            List<MaocheBrandLibKeywordDO> items = brandLibKeywordGroup.get(keywordDO.getBrandLibId());
            if (CollectionUtils.isEmpty(items)) {
                items = new ArrayList<>();
            }
            items.add(keywordDO);

            brandLibKeywordGroup.put(keywordDO.getBrandLibId(), items);
        }

        tagIds = tagIds.stream().distinct().collect(Collectors.toList());

        // 查询标签
        List<MaocheTagDO> tagDOs = maocheTagService.listByIds(tagIds);
        Map<Long, MaocheTagDO> tagMap = tagDOs.stream().collect(Collectors.toMap(MaocheTagDO::getIid, Function.identity(), (o1, o2) -> o1));

        List<BrandLibInfoDTO> infoDTOs = new ArrayList<>();
        for (BrandLibIndex index : documents) {
            if (index == null) {
                continue;
            }
            MaocheBrandDO brandDO = brandMap.get(index.getBrandId());
            if (brandDO == null) {
                continue;
            }

            MaocheBrandLibDO libDO = libMap.get(index.getId());
            if (libDO == null) {
                continue;
            }

            BrandLibDTO lib = new BrandLibDTO();
            List<BrandLibKeywordDTO> keywords = new ArrayList<>();
            List<MaocheBrandLibKeywordDO> keywordDOs = brandLibKeywordGroup.get(libDO.getIid());

            lib.setId(index.getId());
            lib.setIcon(brandDO.getIcon());
            lib.setAliasNames(index.getAliasNames());
            lib.setProductName(index.getProductName());
            lib.setBlacklist("true".equals(libDO.getBlacklist()));

            List<LibCategoryDetail> baseCategories = getBaseCategories();
            Map<Long, LibCategoryDetail> baseCategoryMap = baseCategories.stream().collect(Collectors.toMap(LibCategoryDetail::getId, Function.identity(), (o1, o2) -> o1));
            // size 需要写入
            BrandLibInfoDTO infoDTO = new BrandLibInfoDTO();
            if (CollectionUtils.isNotEmpty(keywordDOs)) {
                for (MaocheBrandLibKeywordDO keywordDO : keywordDOs) {
                    BrandLibKeywordDTO keywordDTO = BrandLibKeywordDTO.convert(keywordDO);
                    if (keywordDTO == null) {
                        continue;
                    }
                    // 设置标签
                    List<TagDTO> tags = getTags(keywordDO.getTags(), tagMap);
                    keywordDTO.setTags(tags);

                    String categoryName = keywordDTO.getCategoryName();
                    // 如果有多个，那就先放多个
                    fillLibCategory(baseCategoryMap, categoryName, categoryMap);

                    keywords.add(keywordDTO);
                }
            }

            infoDTO.setBrandLib(lib);
            infoDTO.setKeywords(keywords);
            infoDTO.setCategories(baseCategories);

            infoDTOs.add(infoDTO);
        }


        return infoDTOs;
    }

    private static void fillLibCategory(Map<Long, LibCategoryDetail> baseCategoryMap, String categoryName, Map<String, List<CategoryTree>> categoryMap) {
        if (MapUtils.isEmpty(baseCategoryMap) || StringUtils.isBlank(categoryName) || MapUtils.isEmpty(categoryMap)) {
            return;
        }

        List<CategoryTree> list = categoryMap.get(categoryName);
        // 如果为空，返回
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        LibCategoryDetail root = null;
        // 获取父类目的id
        Long parentId = list.get(0).getParentId();
        // 说明是父类目
        if (parentId == 0) {
            root = baseCategoryMap.get(list.get(0).getId());
            // 父类目 数量++就行
            if (root == null) {
                return;
            }
            root.setSite(root.getSite() + 1);
        } else {
            root = baseCategoryMap.get(parentId);
            // 父类目 数量++就行
            if (root == null) {
                return;
            }

            // 加到子类目中去
            CategoryTree tree = root.getTree();

            List<CategoryTree> childs = tree.getChilds();
            if (CollectionUtils.isEmpty(childs)) {
                childs = new ArrayList<>();
            }
            childs.addAll(list);
            tree.setChilds(childs);
        }
    }

    private static List<TagDTO> getTags(String tags, Map<Long, MaocheTagDO> tagMap) {
        if (MapUtils.isEmpty(tagMap) || StringUtils.isBlank(tags)) {
            return new ArrayList<>();
        }
        List<TagDTO> dtos = new ArrayList<>();
        List<Long> tagIds = getTagIds(tags);
        for (Long id : tagIds) {
            MaocheTagDO tagDO = tagMap.get(id);
            if (tagDO == null) {
                continue;
            }
            TagDTO tagDTO = new TagDTO();

            tagDTO.setId(id);
            tagDTO.setName(tagDO.getTagName());
            dtos.add(tagDTO);
        }

        return dtos;

    }

    private static List<Long> getTagIds(String tags) {
        if (StringUtils.isBlank(tags)) {
            return new ArrayList<>();
        }
        return JsonUtils.toReferenceType(tags, new TypeReference<List<Long>>() {
        });
    }

    public static List<LibCategoryDetail> getBaseCategories() {
        List<LibCategoryDetail> details = new ArrayList<>();

        details.add(LibCategoryDetail.convert(CategoryHelper.CatRootCategoryEnum.CAT_FOOD.buildRoot()));
        details.add(LibCategoryDetail.convert(CategoryHelper.CatRootCategoryEnum.CAT_LITTER.buildRoot()));
        details.add(LibCategoryDetail.convert(CategoryHelper.CatRootCategoryEnum.CAT_SUPPLIES.buildRoot()));
        details.add(LibCategoryDetail.convert(CategoryHelper.CatRootCategoryEnum.CAT_HEALTH_CARE.buildRoot()));

        return details;
    }

    public boolean updateBrandLib(BrandLibUpdateDTO updateDTO) {

        Long libId = updateDTO.getId();

        MaocheBrandLibDO where = new MaocheBrandLibDO();
        where.setIid(libId);
        MaocheBrandLibDO entity = maocheBrandLibDao.getByEntity(where);
        if (entity == null) {
            return false;
        }

        MaocheBrandLibDO update = new MaocheBrandLibDO();
        update.setIid(libId);
        if (CollectionUtils.isNotEmpty(updateDTO.getAliasNames())) {
            update.setAliasNames(JsonUtils.toJSONString(updateDTO.getAliasNames()));
        }
        if (updateDTO.getBlacklist() != null) {
            update.setBlacklist(BooleanUtils.isTrue(updateDTO.getBlacklist()) ? "true" : "false");
        }
        int row = maocheBrandLibDao.updateById(update);

        boolean res = row > 0;

        if (res) {
            // 再查一次
            entity = maocheBrandLibDao.getByEntity(where);
            indexBrandLib(entity);
        }
        return res;
    }

    public boolean updateBrandLibKeyword(BrandLibKeywordUpdateDTO updateDTO) {

        Long keywordId = updateDTO.getId();
        MaocheBrandLibKeywordDO where = new MaocheBrandLibKeywordDO();
        where.setId(String.valueOf(keywordId));
        MaocheBrandLibKeywordDO entity = maocheBrandLibKeywordDao.getByEntity(where);
        if (entity == null) {
            return false;
        }

        // 获取品牌库信息
        MaocheBrandLibDO lib = maocheBrandLibDao.getById(entity.getBrandLibId());

        if (CollectionUtils.isNotEmpty(updateDTO.getAliasNames())) {
            entity.setAliasNames(JsonUtils.toJSONString(updateDTO.getAliasNames()));
        }
        if (StringUtils.isNotBlank(entity.getStatus())) {
            entity.setStatus(updateDTO.getStatus());
        }

        if (CollectionUtils.isNotEmpty(updateDTO.getTagIds())) {
            entity.setTags(JsonUtils.toJSONString(updateDTO.getTagIds()));
        }
        if (CollectionUtils.isNotEmpty(updateDTO.getSpecialTagIds())) {
            entity.setSpecialTags(JsonUtils.toJSONString(updateDTO.getSpecialTagIds()));
        }

        long row = maocheBrandLibKeywordDao.updateByEntity(entity, where);
        boolean res = row > 0;
        if (res) {
            // 再查一次
            Map<String, Object> keywordMap = BrandLibKeywordIndex.toIndexMap(lib, entity);
            elasticSearch7Service.index(keywordMap, ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_KEYWORD_INDEX, entity.getId());
        }
        return res;
    }
}
