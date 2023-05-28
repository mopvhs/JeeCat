package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionGoodPriceDO;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionGoodPriceDao;

/**
 * maoche_alimama_union_good_priceService
 * @author YHQ
 * @version 2023-05-14
 */
@Service
public class MaocheAlimamaUnionGoodPriceService extends CrudService<MaocheAlimamaUnionGoodPriceDao, MaocheAlimamaUnionGoodPriceDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheAlimamaUnionGoodPriceDO
	 * @return
	 */
	@Override
	public MaocheAlimamaUnionGoodPriceDO get(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO) {
		return super.get(maocheAlimamaUnionGoodPriceDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheAlimamaUnionGoodPriceDO 查询条件
	 * @param maocheAlimamaUnionGoodPriceDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheAlimamaUnionGoodPriceDO> findPage(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO) {
		return super.findPage(maocheAlimamaUnionGoodPriceDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheAlimamaUnionGoodPriceDO
	 * @return
	 */
	@Override
	public List<MaocheAlimamaUnionGoodPriceDO> findList(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO) {
		return super.findList(maocheAlimamaUnionGoodPriceDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheAlimamaUnionGoodPriceDO
	 */
	@Override
	@Transactional
	public void save(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO) {
		super.save(maocheAlimamaUnionGoodPriceDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheAlimamaUnionGoodPriceDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO) {
		super.updateStatus(maocheAlimamaUnionGoodPriceDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheAlimamaUnionGoodPriceDO
	 */
	@Override
	@Transactional
	public void delete(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO) {
		super.delete(maocheAlimamaUnionGoodPriceDO);
	}

	public List<MaocheAlimamaUnionGoodPriceDO> listByItemIdSuffixs(List<String> itemIds) {
		if (CollectionUtils.isEmpty(itemIds)) {
			return new ArrayList<>();
		}
		itemIds = itemIds.stream().distinct().collect(Collectors.toList());

		List<MaocheAlimamaUnionGoodPriceDO> words = new ArrayList<>();
		// 循环拿
		List<List<String>> partition = Lists.partition(itemIds, 20);
		for (List<String> p : partition) {
			MaocheAlimamaUnionGoodPriceDO query = new MaocheAlimamaUnionGoodPriceDO();
			query.setItemIdSuffix_in(p.toArray(new String[0]));
			List<MaocheAlimamaUnionGoodPriceDO> list = dao.findList(query);
			if (CollectionUtils.isEmpty(list)) {
				continue;
			}
			words.addAll(list);
		}

		return words;
	}
}