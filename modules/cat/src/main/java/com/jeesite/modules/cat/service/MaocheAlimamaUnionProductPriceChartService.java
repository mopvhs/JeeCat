package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductPriceChartDO;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductPriceChartDao;

import javax.annotation.Resource;

/**
 * maoche_alimama_union_product_price_chartService
 * @author YHQ
 * @version 2023-07-15
 */
@Slf4j
@Service
public class MaocheAlimamaUnionProductPriceChartService extends CrudService<MaocheAlimamaUnionProductPriceChartDao, MaocheAlimamaUnionProductPriceChartDO> {

	/**
	 * 获取单条数据
	 * @param maocheAlimamaUnionProductPriceChartDO
	 * @return
	 */
	@Override
	public MaocheAlimamaUnionProductPriceChartDO get(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO) {
		return super.get(maocheAlimamaUnionProductPriceChartDO);
	}

	/**
	 * 查询分页数据
	 * @param maocheAlimamaUnionProductPriceChartDO 查询条件
	 * @param maocheAlimamaUnionProductPriceChartDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheAlimamaUnionProductPriceChartDO> findPage(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO) {
		return super.findPage(maocheAlimamaUnionProductPriceChartDO);
	}

	/**
	 * 查询列表数据
	 * @param maocheAlimamaUnionProductPriceChartDO
	 * @return
	 */
	@Override
	public List<MaocheAlimamaUnionProductPriceChartDO> findList(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO) {
		return super.findList(maocheAlimamaUnionProductPriceChartDO);
	}

	/**
	 * 保存数据（插入或更新）
	 * @param maocheAlimamaUnionProductPriceChartDO
	 */
	@Override
	@Transactional
	public void save(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO) {
		super.save(maocheAlimamaUnionProductPriceChartDO);
	}

	/**
	 * 更新状态
	 * @param maocheAlimamaUnionProductPriceChartDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO) {
		super.updateStatus(maocheAlimamaUnionProductPriceChartDO);
	}

	/**
	 * 删除数据
	 * @param maocheAlimamaUnionProductPriceChartDO
	 */
	@Override
	@Transactional
	public void delete(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO) {
		super.delete(maocheAlimamaUnionProductPriceChartDO);
	}


	public List<MaocheAlimamaUnionProductPriceChartDO> listByIids(List<String> iids) {
		if (CollectionUtils.isEmpty(iids)) {
			return new ArrayList<>();
		}
		iids = iids.stream().distinct().collect(Collectors.toList());

		List<MaocheAlimamaUnionProductPriceChartDO> words = new ArrayList<>();
		// 循环拿
		List<List<String>> partition = Lists.partition(iids, 20);
		for (List<String> p : partition) {
			MaocheAlimamaUnionProductPriceChartDO query = new MaocheAlimamaUnionProductPriceChartDO();
			query.setIId_in(p.toArray(new String[0]));
			List<MaocheAlimamaUnionProductPriceChartDO> list = dao.findList(query);
			if (CollectionUtils.isEmpty(list)) {
				continue;
			}

			words.addAll(list);
		}

		return words;
	}

	public List<MaocheAlimamaUnionProductPriceChartDO> listLatestChartPrices(List<String> iids) {
		if (CollectionUtils.isEmpty(iids)) {
			return new ArrayList<>();
		}
		iids = iids.stream().distinct().collect(Collectors.toList());

		List<MaocheAlimamaUnionProductPriceChartDO> words = new ArrayList<>();
		// 循环拿
		List<List<String>> partition = Lists.partition(iids, 20);
		for (List<String> p : partition) {
			try {
				List<MaocheAlimamaUnionProductPriceChartDO> list = dao.listLatestChartPrices(p);
				if (CollectionUtils.isEmpty(list)) {
					continue;
				}
				words.addAll(list);
			} catch (Exception e) {
				log.error("listLatestChartPrices 异常 iids:{}", JsonUtils.toJSONString(p), e);
			}

		}

		return words;
	}
}