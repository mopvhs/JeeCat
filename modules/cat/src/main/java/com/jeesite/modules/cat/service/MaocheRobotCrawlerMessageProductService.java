package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageProductDao;

/**
 * 信息采集商品表Service
 * @author YHQ
 * @version 2023-11-01
 */
@Service
public class MaocheRobotCrawlerMessageProductService extends CrudService<MaocheRobotCrawlerMessageProductDao, MaocheRobotCrawlerMessageProductDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheRobotCrawlerMessageProductDO
	 * @return
	 */
	@Override
	public MaocheRobotCrawlerMessageProductDO get(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO) {
		return dao.getByEntity(maocheRobotCrawlerMessageProductDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheRobotCrawlerMessageProductDO 查询条件
	 * @param maocheRobotCrawlerMessageProductDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheRobotCrawlerMessageProductDO> findPage(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO) {
		return super.findPage(maocheRobotCrawlerMessageProductDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheRobotCrawlerMessageProductDO
	 * @return
	 */
	@Override
	public List<MaocheRobotCrawlerMessageProductDO> findList(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO) {
		return super.findList(maocheRobotCrawlerMessageProductDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheRobotCrawlerMessageProductDO
	 */
	@Override
	@Transactional
	public void save(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO) {
		dao.add(maocheRobotCrawlerMessageProductDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheRobotCrawlerMessageProductDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO) {
		super.updateStatus(maocheRobotCrawlerMessageProductDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheRobotCrawlerMessageProductDO
	 */
	@Override
	@Transactional
	public void delete(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO) {
		super.delete(maocheRobotCrawlerMessageProductDO);
	}
	
}