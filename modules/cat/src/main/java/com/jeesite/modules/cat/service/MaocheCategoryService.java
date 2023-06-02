package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.model.CategoryTree;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheCategoryDO;
import com.jeesite.modules.cat.dao.MaocheCategoryDao;

import javax.annotation.Resource;

/**
 * maoche_categoryService
 * @author YHQ
 * @version 2023-05-24
 */
@Service
public class MaocheCategoryService extends CrudService<MaocheCategoryDao, MaocheCategoryDO> {

	@Resource
	private CacheService cacheService;
	
	/**
	 * 获取单条数据
	 * @param maocheCategoryDO
	 * @return
	 */
	@Override
	public MaocheCategoryDO get(MaocheCategoryDO maocheCategoryDO) {
		return super.get(maocheCategoryDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheCategoryDO 查询条件
	 * @param maocheCategoryDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheCategoryDO> findPage(MaocheCategoryDO maocheCategoryDO) {
		return super.findPage(maocheCategoryDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheCategoryDO
	 * @return
	 */
	@Override
	public List<MaocheCategoryDO> findList(MaocheCategoryDO maocheCategoryDO) {
		return super.findList(maocheCategoryDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheCategoryDO
	 */
	@Override
	@Transactional
	public void save(MaocheCategoryDO maocheCategoryDO) {
		super.save(maocheCategoryDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheCategoryDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheCategoryDO maocheCategoryDO) {
		super.updateStatus(maocheCategoryDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheCategoryDO
	 */
	@Override
	@Transactional
	public void delete(MaocheCategoryDO maocheCategoryDO) {
		super.delete(maocheCategoryDO);
	}

	public List<MaocheCategoryDO> listByItemIdSuffixs(List<String> itemIds) {
		if (CollectionUtils.isEmpty(itemIds)) {
			return new ArrayList<>();
		}
		itemIds = itemIds.stream().distinct().collect(Collectors.toList());

		List<MaocheCategoryDO> words = new ArrayList<>();
		// 循环拿
		List<List<String>> partition = Lists.partition(itemIds, 20);
		for (List<String> p : partition) {
			MaocheCategoryDO query = new MaocheCategoryDO();
			query.setItemIdSuffix_in(p.toArray(new String[0]));
			List<MaocheCategoryDO> list = dao.findList(query);
			if (CollectionUtils.isEmpty(list)) {
				continue;
			}
			words.addAll(list);
		}

		return words;
	}

	public List<MaocheCategoryDO> listByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return new ArrayList<>();
		}
		ids = ids.stream().distinct().collect(Collectors.toList());

		List<MaocheCategoryDO> words = new ArrayList<>();
		// 循环拿
		List<List<Long>> partition = Lists.partition(ids, 20);
		for (List<Long> p : partition) {
			MaocheCategoryDO query = new MaocheCategoryDO();
			query.setIid_in(p.toArray(new Long[0]));
			List<MaocheCategoryDO> list = dao.findList(query);
			if (CollectionUtils.isEmpty(list)) {
				continue;
			}
			words.addAll(list);
		}

		return words;
	}

	/**
	 * 获取所有的类目
	 * @return
	 */
	public List<CategoryTree> listAllCategoryFromCache() {
		String key = "cg_category_all";
		String value = cacheService.get(key);
		if (StringUtils.isNotBlank(value)) {
			return JsonUtils.toReferenceType(value, new TypeReference<List<CategoryTree>>() {
			});
		}
		List<CategoryTree> categories = listAllCategories();

		cacheService.set(key, JsonUtils.toJSONString(categories));
		cacheService.expire(key, 3600);

		return categories;
	}

	public List<CategoryTree> getCategoryTreeFromCache() {
		String key = "cg_category_tree";
		String value = cacheService.get(key);
		if (StringUtils.isNotBlank(value)) {
			return JsonUtils.toReferenceType(value, new TypeReference<List<CategoryTree>>() {
			});
		}
		List<CategoryTree> categoryTrees = buildCategoryTree();

		cacheService.set(key, JsonUtils.toJSONString(categoryTrees));
		cacheService.expire(key, 3600);

		return categoryTrees;
	}

	// 获取类目列表
	public List<CategoryTree> listAllCategories() {

		long id = 0L;
		int limit = 20;

		List<CategoryTree> categories = new ArrayList<>();

		while (true) {
			List<MaocheCategoryDO> list = dao.findAll(id, limit);
			if (CollectionUtils.isEmpty(list)) {
				break;
			}

			for (MaocheCategoryDO item : list) {
				CategoryTree category = new CategoryTree();
				category.setId(item.getIid());
				category.setName(item.getName());
				category.setParentId(item.getParentId());
				category.setLevel(item.getLevel());
				categories.add(category);
			}

			id = list.get(list.size() - 1).getIid();
			if (list.size() < limit) {
				break;
			}
		}

		return categories;
	}

	// 获取类目树
	public List<CategoryTree> buildCategoryTree() {

		long id = 0L;
		int limit = 20;

		// 查找所有的一级类目
		List<MaocheCategoryDO> roots = new ArrayList<>();
		// 查找所有的二级类目
		List<MaocheCategoryDO> seconds = new ArrayList<>();
		// 查找所有的三级类目
		List<MaocheCategoryDO> thirds = new ArrayList<>();

		while (true) {
			List<MaocheCategoryDO> list = dao.findAll(id, limit);
			if (CollectionUtils.isEmpty(list)) {
				break;
			}

			for (MaocheCategoryDO item : list) {
				if (item.getLevel() == 1) {
					roots.add(item);
				} else if (item.getLevel() == 2) {
					seconds.add(item);
				}else if (item.getLevel() == 3) {
					thirds.add(item);
				}
			}

			id = list.get(list.size() - 1).getIid();
			if (list.size() < limit) {
				break;
			}
		}

		List<CategoryTree> rootCategories = new ArrayList<>();
		Map<Long, CategoryTree> rootCategoryTreeMap = new HashMap<>();
		for (MaocheCategoryDO root : roots) {
			CategoryTree item = new CategoryTree();
			item.setId(root.getIid());
			item.setName(root.getName());
			item.setParentId(root.getParentId());
			item.setLevel(root.getLevel());
			item.setChilds(new ArrayList<>());

			rootCategoryTreeMap.put(item.getId(), item);
			rootCategories.add(item);
		}

		// 设置二级类目
		Map<Long, CategoryTree> secondCategoryTreeMap = new HashMap<>();
		for (MaocheCategoryDO second : seconds) {
			CategoryTree item = new CategoryTree();
			item.setId(second.getIid());
			item.setName(second.getName());
			item.setParentId(second.getParentId());
			item.setLevel(second.getLevel());
			item.setChilds(new ArrayList<>());

			CategoryTree root = rootCategoryTreeMap.get(second.getParentId());
			if (root != null) {
				root.getChilds().add(item);
			}

			secondCategoryTreeMap.put(item.getId(), item);
			rootCategories.add(item);
		}

		for (MaocheCategoryDO third : thirds) {
			CategoryTree item = new CategoryTree();
			item.setId(third.getIid());
			item.setName(third.getName());
			item.setParentId(third.getParentId());
			item.setLevel(third.getLevel());

			CategoryTree second = secondCategoryTreeMap.get(third.getParentId());
			if (second != null) {
				second.getChilds().add(item);
			}
		}

		return rootCategories;
	}
	
}