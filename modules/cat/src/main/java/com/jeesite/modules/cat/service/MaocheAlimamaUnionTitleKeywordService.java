package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionTitleKeywordDao;

/**
 * maoche_alimama_union_title_keywordService
 * @author YHQ
 * @version 2023-05-14
 */
@Service
public class MaocheAlimamaUnionTitleKeywordService extends CrudService<MaocheAlimamaUnionTitleKeywordDao, MaocheAlimamaUnionTitleKeywordDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheAlimamaUnionTitleKeywordDO
	 * @return
	 */
	@Override
	public MaocheAlimamaUnionTitleKeywordDO get(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO) {
		return super.get(maocheAlimamaUnionTitleKeywordDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheAlimamaUnionTitleKeywordDO 查询条件
	 * @param maocheAlimamaUnionTitleKeywordDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheAlimamaUnionTitleKeywordDO> findPage(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO) {
		return super.findPage(maocheAlimamaUnionTitleKeywordDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheAlimamaUnionTitleKeywordDO
	 * @return
	 */
	@Override
	public List<MaocheAlimamaUnionTitleKeywordDO> findList(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO) {
		return super.findList(maocheAlimamaUnionTitleKeywordDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheAlimamaUnionTitleKeywordDO
	 */
	@Override
	@Transactional
	public void save(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO) {
		super.save(maocheAlimamaUnionTitleKeywordDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheAlimamaUnionTitleKeywordDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO) {
		super.updateStatus(maocheAlimamaUnionTitleKeywordDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheAlimamaUnionTitleKeywordDO
	 */
	@Override
	@Transactional
	public void delete(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO) {
		super.delete(maocheAlimamaUnionTitleKeywordDO);
	}

	public List<MaocheAlimamaUnionTitleKeywordDO> listByItemIdSuffixs(List<String> itemIds) {
		if (CollectionUtils.isEmpty(itemIds)) {
			return new ArrayList<>();
		}
		itemIds = itemIds.stream().distinct().collect(Collectors.toList());

		List<MaocheAlimamaUnionTitleKeywordDO> words = new ArrayList<>();
		// 循环拿
		List<List<String>> partition = Lists.partition(itemIds, 20);
		for (List<String> p : partition) {
			MaocheAlimamaUnionTitleKeywordDO query = new MaocheAlimamaUnionTitleKeywordDO();
			query.setItemIdSuffix_in(p.toArray(new String[0]));
			List<MaocheAlimamaUnionTitleKeywordDO> list = dao.findList(query);
			if (CollectionUtils.isEmpty(list)) {
				continue;
			}
			words.addAll(list);
		}

		return words;
	}
	
}