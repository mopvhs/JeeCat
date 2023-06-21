package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheChatroomInfoDO;
import com.jeesite.modules.cat.dao.MaocheChatroomInfoDao;

/**
 * 群组消息Service
 * @author YHQ
 * @version 2023-06-21
 */
@Service
public class MaocheChatroomInfoService extends CrudService<MaocheChatroomInfoDao, MaocheChatroomInfoDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheChatroomInfoDO
	 * @return
	 */
	@Override
	public MaocheChatroomInfoDO get(MaocheChatroomInfoDO maocheChatroomInfoDO) {
		return super.get(maocheChatroomInfoDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheChatroomInfoDO 查询条件
	 * @param maocheChatroomInfoDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheChatroomInfoDO> findPage(MaocheChatroomInfoDO maocheChatroomInfoDO) {
		return super.findPage(maocheChatroomInfoDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheChatroomInfoDO
	 * @return
	 */
	@Override
	public List<MaocheChatroomInfoDO> findList(MaocheChatroomInfoDO maocheChatroomInfoDO) {
		return super.findList(maocheChatroomInfoDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheChatroomInfoDO
	 */
	@Override
	@Transactional
	public void save(MaocheChatroomInfoDO maocheChatroomInfoDO) {
		super.save(maocheChatroomInfoDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheChatroomInfoDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheChatroomInfoDO maocheChatroomInfoDO) {
		super.updateStatus(maocheChatroomInfoDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheChatroomInfoDO
	 */
	@Override
	@Transactional
	public void delete(MaocheChatroomInfoDO maocheChatroomInfoDO) {
		super.delete(maocheChatroomInfoDO);
	}
	
}