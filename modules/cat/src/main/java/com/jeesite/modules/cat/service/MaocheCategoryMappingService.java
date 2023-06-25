package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.cache.CacheService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.dao.MaocheCategoryMappingDao;

import javax.annotation.Resource;

/**
 * maoche_category_mappingService
 * @author YHQ
 * @version 2023-06-19
 */
@Service
public class MaocheCategoryMappingService extends CrudService<MaocheCategoryMappingDao, MaocheCategoryMappingDO> {

	@Resource
	private CacheService cacheService;

	/**
	 * 获取单条数据
	 * @param maocheCategoryMappingDO
	 * @return
	 */
	@Override
	public MaocheCategoryMappingDO get(MaocheCategoryMappingDO maocheCategoryMappingDO) {
		return super.get(maocheCategoryMappingDO);
	}

	/**
	 * 查询分页数据
	 * @param maocheCategoryMappingDO 查询条件
	 * @param maocheCategoryMappingDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheCategoryMappingDO> findPage(MaocheCategoryMappingDO maocheCategoryMappingDO) {
		return super.findPage(maocheCategoryMappingDO);
	}

	/**
	 * 查询列表数据
	 * @param maocheCategoryMappingDO
	 * @return
	 */
	@Override
	public List<MaocheCategoryMappingDO> findList(MaocheCategoryMappingDO maocheCategoryMappingDO) {
		return super.findList(maocheCategoryMappingDO);
	}

	/**
	 * 保存数据（插入或更新）
	 * @param maocheCategoryMappingDO
	 */
	@Override
	@Transactional
	public void save(MaocheCategoryMappingDO maocheCategoryMappingDO) {
		super.save(maocheCategoryMappingDO);
	}

	/**
	 * 更新状态
	 * @param maocheCategoryMappingDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheCategoryMappingDO maocheCategoryMappingDO) {
		super.updateStatus(maocheCategoryMappingDO);
	}

	/**
	 * 删除数据
	 * @param maocheCategoryMappingDO
	 */
	@Override
	@Transactional
	public void delete(MaocheCategoryMappingDO maocheCategoryMappingDO) {
		super.delete(maocheCategoryMappingDO);
	}

	public List<MaocheCategoryMappingDO> listByParentId(Long parentId) {
		if (parentId == null) {
			return new ArrayList<>();
		}

		MaocheCategoryMappingDO query = new MaocheCategoryMappingDO();
		query.setParentId(parentId);

		return dao.findList(query);
	}


//	public List<MaocheCategoryMappingDO> listByIds(List<Long> ids) {
//		if (CollectionUtils.isEmpty(ids)) {
//			return new ArrayList<>();
//		}
//
//		MaocheCategoryMappingDO query = new MaocheCategoryMappingDO();
//		query.setId_in(ids.toArray(new ));
//
//		return dao.findList(query);
//	}

	public List<MaocheCategoryMappingDO> getCategoryFromCache(Long parentId) {
		String key = "maoche_category_mapping_" + parentId;
		String value = cacheService.get(key);
		if (StringUtils.isNotBlank(value)) {
			return JsonUtils.toReferenceType(value, new TypeReference<List<MaocheCategoryMappingDO>>() {
			});
		}

		List<MaocheCategoryMappingDO> mappingDOS = listByParentId(parentId);
		if (CollectionUtils.isNotEmpty(mappingDOS)) {
			cacheService.set(key, JsonUtils.toJSONString(mappingDOS));
			cacheService.expire(key, 3600);
		}

		return mappingDOS;
	}

	/**
	 * 通过一级类目的名称获得下面子类目的所有名称
	 * @param name
	 * @return
	 */
	public List<String> getRelationRootName(String name) {
		if (StringUtils.isBlank(name)) {
			return new ArrayList<>();
		}

		MaocheCategoryMappingDO category = null;
		List<MaocheCategoryMappingDO> categoryFromCache = getCategoryFromCache(0L);
		for (MaocheCategoryMappingDO mappingDO : categoryFromCache) {
			if (mappingDO.getName().equals(name)) {
				category = mappingDO;
				break;
			}
		}
		if (category == null) {
			return new ArrayList<>();
		}
		Long id = category.getIid();
		List<MaocheCategoryMappingDO> subCategories = getCategoryFromCache(id);

		return subCategories.stream().map(MaocheCategoryMappingDO::getName).collect(Collectors.toList());
	}

	public List<Long> getRootCids() {
		List<Long> rootCids = new ArrayList<>();
		rootCids.add(50L);
		rootCids.add(23L);
		rootCids.add(25L);
		rootCids.add(1L);

		return rootCids;
	}
}