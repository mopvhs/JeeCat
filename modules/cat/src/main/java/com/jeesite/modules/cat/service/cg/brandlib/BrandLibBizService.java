package com.jeesite.modules.cat.service.cg.brandlib;

import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheBrandLibDao;
import com.jeesite.modules.cat.dao.MaocheBrandLibKeywordDao;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibCreateRequest;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibKeywordCreateRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
public class BrandLibBizService {

    @Resource
    private MaocheBrandLibDao maocheBrandLibDao;

    @Resource
    private MaocheBrandLibKeywordDao maocheBrandLibKeywordDao;

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

        entity = new MaocheBrandLibDO();
        entity.setBrandId(request.getBrandId());
        // 待定
        entity.setProductName("");
        entity.setAliasNames(JsonUtils.toJSONString(request.getAliasNames()));

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

        return Result.ERROR(500, "新增失败");
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
}
