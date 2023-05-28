package com.jeesite.modules.cat.service;

import java.util.List;

import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.SyncMarkEnum;
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
	
}