package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheSubscribeDO;
import com.jeesite.modules.cat.dao.MaocheSubscribeDao;

/**
 * 订阅表Service
 * @author YhQ
 * @version 2024-11-30
 */
@Service
public class MaocheSubscribeService extends CrudService<MaocheSubscribeDao, MaocheSubscribeDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheSubscribeDO
	 * @return
	 */
	@Override
	public MaocheSubscribeDO get(MaocheSubscribeDO maocheSubscribeDO) {
		return super.get(maocheSubscribeDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheSubscribeDO 查询条件
	 * @param maocheSubscribeDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheSubscribeDO> findPage(MaocheSubscribeDO maocheSubscribeDO) {
		return super.findPage(maocheSubscribeDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheSubscribeDO
	 * @return
	 */
	@Override
	public List<MaocheSubscribeDO> findList(MaocheSubscribeDO maocheSubscribeDO) {
		return super.findList(maocheSubscribeDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheSubscribeDO
	 */
	@Override
	@Transactional
	public void save(MaocheSubscribeDO maocheSubscribeDO) {
		super.save(maocheSubscribeDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheSubscribeDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheSubscribeDO maocheSubscribeDO) {
		super.updateStatus(maocheSubscribeDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheSubscribeDO
	 */
	@Override
	@Transactional
	public void delete(MaocheSubscribeDO maocheSubscribeDO) {
		super.delete(maocheSubscribeDO);
	}
	
}