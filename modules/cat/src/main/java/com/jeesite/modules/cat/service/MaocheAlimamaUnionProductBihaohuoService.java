package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductPriceChartDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductBihaohuoDO;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductBihaohuoDao;

/**
 * maoche_alimama_union_product_bihaohuoService
 * @author YHQ
 * @version 2023-07-22
 */
@Slf4j
@Service
public class MaocheAlimamaUnionProductBihaohuoService extends CrudService<MaocheAlimamaUnionProductBihaohuoDao, MaocheAlimamaUnionProductBihaohuoDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheAlimamaUnionProductBihaohuoDO
	 * @return
	 */
	@Override
	public MaocheAlimamaUnionProductBihaohuoDO get(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO) {
		return super.get(maocheAlimamaUnionProductBihaohuoDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheAlimamaUnionProductBihaohuoDO 查询条件
	 * @param maocheAlimamaUnionProductBihaohuoDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheAlimamaUnionProductBihaohuoDO> findPage(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO) {
		return super.findPage(maocheAlimamaUnionProductBihaohuoDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheAlimamaUnionProductBihaohuoDO
	 * @return
	 */
	@Override
	public List<MaocheAlimamaUnionProductBihaohuoDO> findList(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO) {
		return super.findList(maocheAlimamaUnionProductBihaohuoDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheAlimamaUnionProductBihaohuoDO
	 */
	@Override
	@Transactional
	public void save(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO) {
		super.save(maocheAlimamaUnionProductBihaohuoDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheAlimamaUnionProductBihaohuoDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO) {
		super.updateStatus(maocheAlimamaUnionProductBihaohuoDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheAlimamaUnionProductBihaohuoDO
	 */
	@Override
	@Transactional
	public void delete(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO) {
		super.delete(maocheAlimamaUnionProductBihaohuoDO);
	}

	public List<MaocheAlimamaUnionProductBihaohuoDO> listLatestChartPrices(List<String> iids) {
		if (CollectionUtils.isEmpty(iids)) {
			return new ArrayList<>();
		}
		iids = iids.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

		List<MaocheAlimamaUnionProductBihaohuoDO> words = new ArrayList<>();
		// 循环拿
		List<List<String>> partition = Lists.partition(iids, 20);
		for (List<String> p : partition) {
			try {
				List<Long> ids = dao.listLatestChartPricesId(p);

				if (CollectionUtils.isEmpty(ids)) {
					continue;
				}
				MaocheAlimamaUnionProductBihaohuoDO query = new MaocheAlimamaUnionProductBihaohuoDO();
				query.setUiid_in(ids);
				List<MaocheAlimamaUnionProductBihaohuoDO> list = dao.findList(query);

				words.addAll(list);
			} catch (Exception e) {
				log.error("listLatestChartPrices 异常 iids:{}", JsonUtils.toJSONString(p), e);
			}

		}

		return words;
	}
	
}