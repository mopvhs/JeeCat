package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.QwConfigInfoDO;
import com.jeesite.modules.cat.dao.QwConfigInfoDao;

/**
 * 企微配置详情数据Service
 * @author YHQ
 * @version 2023-06-21
 */
@Service
public class QwConfigInfoService extends CrudService<QwConfigInfoDao, QwConfigInfoDO> {
	
	/**
	 * 获取单条数据
	 * @param qwConfigInfoDO
	 * @return
	 */
	@Override
	public QwConfigInfoDO get(QwConfigInfoDO qwConfigInfoDO) {
		return super.get(qwConfigInfoDO);
	}
	
	/**
	 * 查询分页数据
	 * @param qwConfigInfoDO 查询条件
	 * @param qwConfigInfoDO page 分页对象
	 * @return
	 */
	@Override
	public Page<QwConfigInfoDO> findPage(QwConfigInfoDO qwConfigInfoDO) {
		return super.findPage(qwConfigInfoDO);
	}
	
	/**
	 * 查询列表数据
	 * @param qwConfigInfoDO
	 * @return
	 */
	@Override
	public List<QwConfigInfoDO> findList(QwConfigInfoDO qwConfigInfoDO) {
		return super.findList(qwConfigInfoDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param qwConfigInfoDO
	 */
	@Override
	@Transactional
	public void save(QwConfigInfoDO qwConfigInfoDO) {
		super.save(qwConfigInfoDO);
	}
	
	/**
	 * 更新状态
	 * @param qwConfigInfoDO
	 */
	@Override
	@Transactional
	public void updateStatus(QwConfigInfoDO qwConfigInfoDO) {
		super.updateStatus(qwConfigInfoDO);
	}
	
	/**
	 * 删除数据
	 * @param qwConfigInfoDO
	 */
	@Override
	@Transactional
	public void delete(QwConfigInfoDO qwConfigInfoDO) {
		super.delete(qwConfigInfoDO);
	}
	
}