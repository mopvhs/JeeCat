package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageDao;

/**
 * 信息采集表Service
 * @author YHQ
 * @version 2023-04-30
 */
@Service
public class MaocheRobotCrawlerMessageService extends CrudService<MaocheRobotCrawlerMessageDao, MaocheRobotCrawlerMessageDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheRobotCrawlerMessageDO
	 * @return
	 */
	@Override
	public MaocheRobotCrawlerMessageDO get(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO) {
		return super.get(maocheRobotCrawlerMessageDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheRobotCrawlerMessageDO 查询条件
	 * @param maocheRobotCrawlerMessageDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheRobotCrawlerMessageDO> findPage(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO) {
		return super.findPage(maocheRobotCrawlerMessageDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheRobotCrawlerMessageDO
	 * @return
	 */
	@Override
	public List<MaocheRobotCrawlerMessageDO> findList(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO) {
		return super.findList(maocheRobotCrawlerMessageDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheRobotCrawlerMessageDO
	 */
	@Override
	@Transactional
	public void save(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO) {
		super.save(maocheRobotCrawlerMessageDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheRobotCrawlerMessageDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO) {
		super.updateStatus(maocheRobotCrawlerMessageDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheRobotCrawlerMessageDO
	 */
	@Override
	@Transactional
	public void delete(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO) {
		super.delete(maocheRobotCrawlerMessageDO);
	}
	
}