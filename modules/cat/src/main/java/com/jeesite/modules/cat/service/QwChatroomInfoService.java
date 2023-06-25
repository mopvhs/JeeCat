package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.QwChatroomInfoDO;
import com.jeesite.modules.cat.dao.QwChatroomInfoDao;

/**
 * 群组消息Service
 * @author YHQ
 * @version 2023-06-23
 */
@Service
public class QwChatroomInfoService extends CrudService<QwChatroomInfoDao, QwChatroomInfoDO> {
	
	/**
	 * 获取单条数据
	 * @param qwChatroomInfoDO
	 * @return
	 */
	@Override
	public QwChatroomInfoDO get(QwChatroomInfoDO qwChatroomInfoDO) {
		return super.get(qwChatroomInfoDO);
	}
	
	/**
	 * 查询分页数据
	 * @param qwChatroomInfoDO 查询条件
	 * @param qwChatroomInfoDO page 分页对象
	 * @return
	 */
	@Override
	public Page<QwChatroomInfoDO> findPage(QwChatroomInfoDO qwChatroomInfoDO) {
		return super.findPage(qwChatroomInfoDO);
	}
	
	/**
	 * 查询列表数据
	 * @param qwChatroomInfoDO
	 * @return
	 */
	@Override
	public List<QwChatroomInfoDO> findList(QwChatroomInfoDO qwChatroomInfoDO) {
		return super.findList(qwChatroomInfoDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param qwChatroomInfoDO
	 */
	@Override
	@Transactional
	public void save(QwChatroomInfoDO qwChatroomInfoDO) {
		super.save(qwChatroomInfoDO);
	}
	
	/**
	 * 更新状态
	 * @param qwChatroomInfoDO
	 */
	@Override
	@Transactional
	public void updateStatus(QwChatroomInfoDO qwChatroomInfoDO) {
		super.updateStatus(qwChatroomInfoDO);
	}
	
	/**
	 * 删除数据
	 * @param qwChatroomInfoDO
	 */
	@Override
	@Transactional
	public void delete(QwChatroomInfoDO qwChatroomInfoDO) {
		super.delete(qwChatroomInfoDO);
	}
	
}