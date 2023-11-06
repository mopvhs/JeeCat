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
import com.jeesite.modules.cat.entity.MaocheCategoryDO;
import com.jeesite.modules.cat.model.CategoryTree;
import com.jeesite.modules.cat.model.TagTree;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheTagDO;
import com.jeesite.modules.cat.dao.MaocheTagDao;

import javax.annotation.Resource;

/**
 * maoche_tagService
 * @author YHQ
 * @version 2023-10-29
 */
@Service
public class MaocheTagService extends CrudService<MaocheTagDao, MaocheTagDO> {

	@Resource
	private CacheService cacheService;

	private static final String CACHE_KEY = "cg_tag_tree";
	
	/**
	 * 获取单条数据
	 * @param maocheTagDO
	 * @return
	 */
	@Override
	public MaocheTagDO get(MaocheTagDO maocheTagDO) {
		return super.get(maocheTagDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheTagDO 查询条件
	 * @param maocheTagDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheTagDO> findPage(MaocheTagDO maocheTagDO) {
		return super.findPage(maocheTagDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheTagDO
	 * @return
	 */
	@Override
	public List<MaocheTagDO> findList(MaocheTagDO maocheTagDO) {
		return super.findList(maocheTagDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheTagDO
	 */
	@Override
	@Transactional
	public void save(MaocheTagDO maocheTagDO) {
		dao.add(maocheTagDO);
	}

	public void add(MaocheTagDO maocheTagDO) {
		dao.add(maocheTagDO);
	}


	
	/**
	 * 更新状态
	 * @param maocheTagDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheTagDO maocheTagDO) {
		super.updateStatus(maocheTagDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheTagDO
	 */
	@Override
	@Transactional
	public void delete(MaocheTagDO maocheTagDO) {
		super.delete(maocheTagDO);
	}


	public List<MaocheTagDO> listByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return new ArrayList<>();
		}
		ids = ids.stream().distinct().collect(Collectors.toList());

		List<MaocheTagDO> words = new ArrayList<>();
		// 循环拿
		List<List<Long>> partition = Lists.partition(ids, 20);
		for (List<Long> p : partition) {
			MaocheTagDO query = new MaocheTagDO();
			query.setIid_in(p.toArray(new Long[0]));
			List<MaocheTagDO> list = dao.findList(query);
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
	public List<TagTree> listAllCategoryFromCache() {
		String value = cacheService.get(CACHE_KEY);
		if (StringUtils.isNotBlank(value)) {
			return JsonUtils.toReferenceType(value, new TypeReference<List<TagTree>>() {
			});
		}
		List<TagTree> categories = listTags();

		cacheService.set(CACHE_KEY, JsonUtils.toJSONString(categories));
		cacheService.expire(CACHE_KEY, 3600);

		return categories;
	}

	public boolean deleteCache() {
		return cacheService.delete(CACHE_KEY);
	}

	public List<TagTree> getCategoryTreeFromCache() {
		String key = "cg_tag_tree";
		String value = cacheService.get(key);
		if (StringUtils.isNotBlank(value)) {
			return JsonUtils.toReferenceType(value, new TypeReference<List<TagTree>>() {
			});
		}
		List<TagTree> categoryTrees = buildTagTree();

		cacheService.set(key, JsonUtils.toJSONString(categoryTrees));
		cacheService.expire(key, 3600);

		return categoryTrees;
	}

	// 获取类目列表
	public List<TagTree> listTags() {

		long id = 0L;
		int limit = 20;

		List<TagTree> categories = new ArrayList<>();

		while (true) {
			List<MaocheTagDO> list = dao.findAll(id, limit);
			if (CollectionUtils.isEmpty(list)) {
				break;
			}

			for (MaocheTagDO item : list) {
				TagTree category = new TagTree();
				category.setId(item.getIid());
				category.setName(item.getTagName());
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
	public List<TagTree> buildTagTree() {

		long id = 0L;
		int limit = 20;

		// 查找所有的一级类目
		List<MaocheTagDO> roots = new ArrayList<>();
		// 查找所有的二级类目
		List<MaocheTagDO> seconds = new ArrayList<>();
		// 查找所有的三级类目
		List<MaocheTagDO> thirds = new ArrayList<>();

		while (true) {
			List<MaocheTagDO> list = dao.findAll(id, limit);
			if (CollectionUtils.isEmpty(list)) {
				break;
			}

			for (MaocheTagDO item : list) {
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

		List<TagTree> rootCategories = new ArrayList<>();
		Map<Long, TagTree> rootCategoryTreeMap = new HashMap<>();
		for (MaocheTagDO root : roots) {
			TagTree item = new TagTree();
			item.setId(root.getIid());
			item.setName(root.getTagName());
			item.setParentId(root.getParentId());
			item.setLevel(root.getLevel());
			item.setChilds(new ArrayList<>());

			rootCategoryTreeMap.put(item.getId(), item);
			rootCategories.add(item);
		}

		// 设置二级类目
		Map<Long, TagTree> secondCategoryTreeMap = new HashMap<>();
		for (MaocheTagDO second : seconds) {
			TagTree item = new TagTree();
			item.setId(second.getIid());
			item.setName(second.getTagName());
			item.setParentId(second.getParentId());
			item.setLevel(second.getLevel());
			item.setChilds(new ArrayList<>());

			TagTree root = rootCategoryTreeMap.get(second.getParentId());
			if (root != null) {
				root.getChilds().add(item);
			}

			secondCategoryTreeMap.put(item.getId(), item);
			rootCategories.add(item);
		}

		for (MaocheTagDO third : thirds) {
			TagTree item = new TagTree();
			item.setId(third.getIid());
			item.setName(third.getTagName());
			item.setParentId(third.getParentId());
			item.setLevel(third.getLevel());

			TagTree second = secondCategoryTreeMap.get(third.getParentId());
			if (second != null) {
				second.getChilds().add(item);
			}
		}

		return rootCategories;
	}
	
}