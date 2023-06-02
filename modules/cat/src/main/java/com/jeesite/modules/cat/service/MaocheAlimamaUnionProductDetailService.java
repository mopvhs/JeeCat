package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.entity.MaocheCategoryProductRelDO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDetailDao;

/**
 * maoche_alimama_union_product_detailService
 * @author YHQ
 * @version 2023-05-28
 */
@Service
public class MaocheAlimamaUnionProductDetailService extends CrudService<MaocheAlimamaUnionProductDetailDao, MaocheAlimamaUnionProductDetailDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheAlimamaUnionProductDetailDO
	 * @return
	 */
	@Override
	public MaocheAlimamaUnionProductDetailDO get(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO) {
		return super.get(maocheAlimamaUnionProductDetailDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheAlimamaUnionProductDetailDO 查询条件
	 * @param maocheAlimamaUnionProductDetailDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheAlimamaUnionProductDetailDO> findPage(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO) {
		return super.findPage(maocheAlimamaUnionProductDetailDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheAlimamaUnionProductDetailDO
	 * @return
	 */
	@Override
	public List<MaocheAlimamaUnionProductDetailDO> findList(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO) {
		return super.findList(maocheAlimamaUnionProductDetailDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheAlimamaUnionProductDetailDO
	 */
	@Override
	@Transactional
	public void save(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO) {
		super.save(maocheAlimamaUnionProductDetailDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheAlimamaUnionProductDetailDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO) {
		super.updateStatus(maocheAlimamaUnionProductDetailDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheAlimamaUnionProductDetailDO
	 */
	@Override
	@Transactional
	public void delete(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO) {
		super.delete(maocheAlimamaUnionProductDetailDO);
	}

	public List<MaocheAlimamaUnionProductDetailDO> listByItemIdSuffixs(List<String> itemIds) {
		if (CollectionUtils.isEmpty(itemIds)) {
			return new ArrayList<>();
		}
		itemIds = itemIds.stream().distinct().collect(Collectors.toList());

		List<MaocheAlimamaUnionProductDetailDO> words = new ArrayList<>();
		// 循环拿
		List<List<String>> partition = Lists.partition(itemIds, 20);
		for (List<String> p : partition) {
			MaocheAlimamaUnionProductDetailDO query = new MaocheAlimamaUnionProductDetailDO();
			query.setItemIdSuffix_in(p.toArray(new String[0]));
			List<MaocheAlimamaUnionProductDetailDO> list = dao.findList(query);
			if (CollectionUtils.isEmpty(list)) {
				continue;
			}
			words.addAll(list);
		}

		return words;
	}

	public MaocheAlimamaUnionProductDetailDO getByItemIdSuffix(String itemIdSuffix) {
		if (StringUtils.isBlank(itemIdSuffix)) {
			return null;
		}

		MaocheAlimamaUnionProductDetailDO query = new MaocheAlimamaUnionProductDetailDO();
		query.setItemIdSuffix(itemIdSuffix);

		return dao.getByEntity(query);
	}
}