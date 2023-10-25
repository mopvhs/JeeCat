package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.CsOpLogDO;
import com.jeesite.modules.cat.dao.CsOpLogDao;

/**
 * 操作日志表Service
 * @author YHQ
 * @version 2023-10-21
 */
@Service
public class CsOpLogService extends CrudService<CsOpLogDao, CsOpLogDO> {
	
	/**
	 * 获取单条数据
	 * @param csOpLogDO
	 * @return
	 */
	@Override
	public CsOpLogDO get(CsOpLogDO csOpLogDO) {
		return super.get(csOpLogDO);
	}
	
	/**
	 * 查询分页数据
	 * @param csOpLogDO 查询条件
	 * @param csOpLogDO page 分页对象
	 * @return
	 */
	@Override
	public Page<CsOpLogDO> findPage(CsOpLogDO csOpLogDO) {
		return super.findPage(csOpLogDO);
	}
	
	/**
	 * 查询列表数据
	 * @param csOpLogDO
	 * @return
	 */
	@Override
	public List<CsOpLogDO> findList(CsOpLogDO csOpLogDO) {
		return super.findList(csOpLogDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param csOpLogDO
	 */
	@Override
	@Transactional
	public void save(CsOpLogDO csOpLogDO) {
		super.save(csOpLogDO);
	}
	
	/**
	 * 更新状态
	 * @param csOpLogDO
	 */
	@Override
	@Transactional
	public void updateStatus(CsOpLogDO csOpLogDO) {
		super.updateStatus(csOpLogDO);
	}
	
	/**
	 * 删除数据
	 * @param csOpLogDO
	 */
	@Override
	@Transactional
	public void delete(CsOpLogDO csOpLogDO) {
		super.delete(csOpLogDO);
	}
	
}