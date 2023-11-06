package com.jeesite.modules.cat.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.ProductDataSource;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.enums.SyncMarkEnum;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;

/**
 * maoche_alimama_union_productService
 * @author YHQ
 * @version 2023-05-05
 */
@Service
public class MaocheAlimamaUnionProductService extends CrudService<MaocheAlimamaUnionProductDao, MaocheAlimamaUnionProductDO> {

	/**
	 * 获取单条数据
	 * @param maocheAlimamaUnionProductDO
	 * @return
	 */
	@Override
	public MaocheAlimamaUnionProductDO get(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO) {
		return super.get(maocheAlimamaUnionProductDO);
	}

	/**
	 * 查询分页数据
	 * @param maocheAlimamaUnionProductDO 查询条件
	 * @param maocheAlimamaUnionProductDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheAlimamaUnionProductDO> findPage(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO) {
		return super.findPage(maocheAlimamaUnionProductDO);
	}

	/**
	 * 查询列表数据
	 * @param maocheAlimamaUnionProductDO
	 * @return
	 */
	@Override
	public List<MaocheAlimamaUnionProductDO> findList(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO) {
		return super.findList(maocheAlimamaUnionProductDO);
	}

	/**
	 * 保存数据（插入或更新）
	 * @param maocheAlimamaUnionProductDO
	 */
	@Override
	@Transactional
	public void save(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO) {
		super.save(maocheAlimamaUnionProductDO);
	}

	/**
	 * 更新状态
	 * @param maocheAlimamaUnionProductDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO) {
		super.updateStatus(maocheAlimamaUnionProductDO);
	}

	/**
	 * 删除数据
	 * @param maocheAlimamaUnionProductDO
	 */
	@Override
	@Transactional
	public void delete(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO) {
		super.delete(maocheAlimamaUnionProductDO);
	}

	/**
	 * 如果是入库的，标记需要同步数据
	 * @param ids
	 * @param auditStatus
	 * @return
	 */
	public boolean updateAuditStatus(List<Long> ids, int auditStatus) {

		Integer syncMark = null;
		if (auditStatus == AuditStatusEnum.PASS.getStatus()) {
			syncMark = SyncMarkEnum.PASS.getType();
		}

		int i = dao.updateAuditStatus(ids, auditStatus, syncMark);

		return i > 0;
	}

	/**
	 * 如果是入库的，标记需要同步数据
	 * @param ids
	 * @param auditStatus
	 * @return
	 */
	public boolean updateSaleAuditStatus(List<Long> ids, Integer auditStatus, Long saleStatus) {

		Integer syncMark = null;
		if (Objects.equals(auditStatus, AuditStatusEnum.PASS.getStatus())) {
			syncMark = SyncMarkEnum.PASS.getType();
		}
		String onShelfDate = null;
		if (Objects.equals(saleStatus, SaleStatusEnum.ON_SHELF.getStatus())) {
			onShelfDate = new Timestamp(System.currentTimeMillis()).toString();
		}

		int i = dao.updateSaleAuditStatus(ids, saleStatus, onShelfDate, auditStatus, syncMark);

		return i > 0;
	}

	public List<MaocheAlimamaUnionProductDO> listByMaocheInnerIds(List<String> innerIds, ProductDataSource source) {
		if (CollectionUtils.isEmpty(innerIds) || source == null) {
			return new ArrayList<>();
		}

		List<MaocheAlimamaUnionProductDO> productDOs = new ArrayList<>();
		List<List<String>> partition = Lists.partition(innerIds, 20);
		for (List<String> p : partition) {
			MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
			query.setMaocheInnerId_in(p.toArray(new String[0]));
			query.setDataSource(source.getSource());
			List<MaocheAlimamaUnionProductDO> items = dao.findList(query);
			if (CollectionUtils.isNotEmpty(items)) {
				productDOs.addAll(items);
			}
		}

		return productDOs;
	}

	public List<MaocheAlimamaUnionProductDO> listByMaocheInnerIds(List<String> innerIds,
																  ProductDataSource source,
																  String levelOneCategoryName,
																  String status) {
		if (CollectionUtils.isEmpty(innerIds) || source == null) {
			return new ArrayList<>();
		}

		List<MaocheAlimamaUnionProductDO> productDOs = new ArrayList<>();
		List<List<String>> partition = Lists.partition(innerIds, 20);
		for (List<String> p : partition) {
			MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
			query.setMaocheInnerId_in(p.toArray(new String[0]));
			query.setDataSource(source.getSource());
			query.setLevelOneCategoryName(levelOneCategoryName);
			query.setStatus(status);
			List<MaocheAlimamaUnionProductDO> items = dao.findList(query);
			if (CollectionUtils.isNotEmpty(items)) {
				productDOs.addAll(items);
			}
		}

		return productDOs;
	}


	public List<MaocheAlimamaUnionProductDO> listByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return new ArrayList<>();
		}

		List<MaocheAlimamaUnionProductDO> productDOs = new ArrayList<>();
		List<List<Long>> partition = Lists.partition(ids, 20);
		for (List<Long> p : partition) {
			MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
			query.setIid_in(p.toArray(new Long[0]));
			List<MaocheAlimamaUnionProductDO> items = dao.findList(query);
			if (CollectionUtils.isNotEmpty(items)) {
				productDOs.addAll(items);
			}
		}

		return productDOs;
	}

	/**
	 * @param ids
	 * @return
	 */
	public boolean updateProductStatus(List<Long> ids, String status) {

		int i = dao.updateProductStatus(ids, status);

		return i > 0;
	}

	// 修改利益点
	public boolean updateCustomBenefit(List<Long> ids, String customBenefit) {
		int i = dao.updateCustomBenefit(ids, customBenefit);
		return i > 0;
	}

	public List<MaocheAlimamaUnionProductDO> getByItemIdSuffix(String itemIdSuffix, String status) {
		if (StringUtils.isBlank(itemIdSuffix)) {
			return new ArrayList<>();
		}

		List<MaocheAlimamaUnionProductDO> productDOs = new ArrayList<>();
		MaocheAlimamaUnionProductDO query = new MaocheAlimamaUnionProductDO();
		query.setItemIdSuffix(itemIdSuffix);
		query.setStatus(status);
		List<MaocheAlimamaUnionProductDO> items = dao.findList(query);
		if (CollectionUtils.isNotEmpty(items)) {
			productDOs.addAll(items);
		}

		return productDOs;
	}
}