package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheSenderTaskDO;
import com.jeesite.modules.cat.dao.MaocheSenderTaskDao;

/**
 * 主动发布任务Service
 * @author YHQ
 * @version 2023-05-28
 */
@Service
public class MaocheSenderTaskService extends CrudService<MaocheSenderTaskDao, MaocheSenderTaskDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheSenderTaskDO
	 * @return
	 */
	@Override
	public MaocheSenderTaskDO get(MaocheSenderTaskDO maocheSenderTaskDO) {
		return super.get(maocheSenderTaskDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheSenderTaskDO 查询条件
	 * @param maocheSenderTaskDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheSenderTaskDO> findPage(MaocheSenderTaskDO maocheSenderTaskDO) {
		return super.findPage(maocheSenderTaskDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheSenderTaskDO
	 * @return
	 */
	@Override
	public List<MaocheSenderTaskDO> findList(MaocheSenderTaskDO maocheSenderTaskDO) {
		return super.findList(maocheSenderTaskDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheSenderTaskDO
	 */
	@Override
	@Transactional
	public void save(MaocheSenderTaskDO maocheSenderTaskDO) {
		super.save(maocheSenderTaskDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheSenderTaskDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheSenderTaskDO maocheSenderTaskDO) {
		super.updateStatus(maocheSenderTaskDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheSenderTaskDO
	 */
	@Override
	@Transactional
	public void delete(MaocheSenderTaskDO maocheSenderTaskDO) {
		super.delete(maocheSenderTaskDO);
	}
	
}