package com.jeesite.modules.cat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.service.CrudService;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.dao.MaocheBrandLibDao;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YHQ
 * @version 2024-08-19
 */
@Slf4j
@Service
public class MaocheBrandLibMapper extends CrudService<MaocheBrandLibDao, MaocheBrandLibDO> {

    public MaocheBrandLibDO addByBrand(MaocheBrandDO brandDO) {

        Date date = new Date();

        try {
            MaocheBrandLibDO entity = new MaocheBrandLibDO();
            entity.setBrandId(brandDO.getIid());
            entity.setProductName(brandDO.getName());
            entity.setAliasNames("");
            entity.setBlacklist("false");
            entity.setCreateBy("admin");
            entity.setUpdateBy("admin");
            entity.setUpdateDate(date);
            entity.setCreateDate(date);
            entity.setStatus("NORMAL");
            entity.setRemarks("");

            String remarks = brandDO.getRemarks();
            Map<String, Object> remarksMap = JsonUtils.toReferenceType(remarks, new TypeReference<HashMap<String, Object>>() {
            });
            if (MapUtils.isNotEmpty(remarksMap)) {
                Object o = remarksMap.get("alias");
                if (o instanceof List) {
                    entity.setAliasNames(JsonUtils.toJSONString(o));
                }
            }

            int add = dao.add(entity);
            if (add > 0) {
                return entity;
            }
        } catch (Exception e) {
            log.error("创建品牌库失败, brandDO:{}", JsonUtils.toJSONString(brandDO), e);
        }
        return null;
    }

    public MaocheBrandLibDO getById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalStateException("参数错误");
        }
        // 获取品牌库信息
        MaocheBrandLibDO lib = dao.getById(id);

        return lib;
    }

    public List<MaocheBrandLibDO> listByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalStateException("参数错误");
        }
        // 获取品牌库信息
        return dao.listByIds(ids);
    }

}