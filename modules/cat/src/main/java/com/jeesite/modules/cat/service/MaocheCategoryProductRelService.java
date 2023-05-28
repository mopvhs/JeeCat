package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.jeesite.modules.cat.entity.MaocheCategoryDO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheCategoryProductRelDO;
import com.jeesite.modules.cat.dao.MaocheCategoryProductRelDao;

/**
 * maoche_category_product_relService
 * @author YHQ
 * @version 2023-05-24
 */
@Service
public class MaocheCategoryProductRelService extends CrudService<MaocheCategoryProductRelDao, MaocheCategoryProductRelDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheCategoryProductRelDO
	 * @return
	 */
	@Override
	public MaocheCategoryProductRelDO get(MaocheCategoryProductRelDO maocheCategoryProductRelDO) {
		return super.get(maocheCategoryProductRelDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheCategoryProductRelDO 查询条件
	 * @param maocheCategoryProductRelDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheCategoryProductRelDO> findPage(MaocheCategoryProductRelDO maocheCategoryProductRelDO) {
		return super.findPage(maocheCategoryProductRelDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheCategoryProductRelDO
	 * @return
	 */
	@Override
	public List<MaocheCategoryProductRelDO> findList(MaocheCategoryProductRelDO maocheCategoryProductRelDO) {
		return super.findList(maocheCategoryProductRelDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheCategoryProductRelDO
	 */
	@Override
	@Transactional
	public void save(MaocheCategoryProductRelDO maocheCategoryProductRelDO) {
		super.save(maocheCategoryProductRelDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheCategoryProductRelDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheCategoryProductRelDO maocheCategoryProductRelDO) {
		super.updateStatus(maocheCategoryProductRelDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheCategoryProductRelDO
	 */
	@Override
	@Transactional
	public void delete(MaocheCategoryProductRelDO maocheCategoryProductRelDO) {
		super.delete(maocheCategoryProductRelDO);
	}

	public List<MaocheCategoryProductRelDO> listByItemIdSuffixs(List<String> itemIds) {
		if (CollectionUtils.isEmpty(itemIds)) {
			return new ArrayList<>();
		}
		itemIds = itemIds.stream().distinct().collect(Collectors.toList());

		List<MaocheCategoryProductRelDO> words = new ArrayList<>();
		// 循环拿
		List<List<String>> partition = Lists.partition(itemIds, 20);
		for (List<String> p : partition) {
			MaocheCategoryProductRelDO query = new MaocheCategoryProductRelDO();
			query.setItemIdSuffix_in(p.toArray(new String[0]));
			List<MaocheCategoryProductRelDO> list = dao.findList(query);
			if (CollectionUtils.isEmpty(list)) {
				continue;
			}
			words.addAll(list);
		}

		return words;
	}
	
}