package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheProductV2DO;
import com.jeesite.modules.cat.dao.MaocheProductV2Dao;

/**
 * maoche_product_v2Service
 * @author YHQ
 * @version 2024-01-02
 */
@Service
public class MaocheProductV2Service extends CrudService<MaocheProductV2Dao, MaocheProductV2DO> {
	
	/**
	 * 获取单条数据
	 * @param maocheProductV2DO
	 * @return
	 */
	@Override
	public MaocheProductV2DO get(MaocheProductV2DO maocheProductV2DO) {
		return super.get(maocheProductV2DO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheProductV2DO 查询条件
	 * @param maocheProductV2DO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheProductV2DO> findPage(MaocheProductV2DO maocheProductV2DO) {
		return super.findPage(maocheProductV2DO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheProductV2DO
	 * @return
	 */
	@Override
	public List<MaocheProductV2DO> findList(MaocheProductV2DO maocheProductV2DO) {
		return super.findList(maocheProductV2DO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheProductV2DO
	 */
	@Override
	@Transactional
	public void save(MaocheProductV2DO maocheProductV2DO) {
		super.save(maocheProductV2DO);
	}
	
	/**
	 * 更新状态
	 * @param maocheProductV2DO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheProductV2DO maocheProductV2DO) {
		super.updateStatus(maocheProductV2DO);
	}
	
	/**
	 * 删除数据
	 * @param maocheProductV2DO
	 */
	@Override
	@Transactional
	public void delete(MaocheProductV2DO maocheProductV2DO) {
		super.delete(maocheProductV2DO);
	}



	public List<MaocheProductV2DO> listByProductIds(List<Long> productIds, String status) {
		if (CollectionUtils.isEmpty(productIds)) {
			return new ArrayList<>();
		}

		productIds = productIds.stream().distinct().collect(Collectors.toList());

		MaocheProductV2DO query = new MaocheProductV2DO();
		query.setProductId_in(productIds);
		query.setStatus(status);

		return findList(query);
	}
	
}