package com.jeesite.modules.cat.service;

import java.util.List;

import com.jeesite.common.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.dao.MaocheRobotCrawlerMessageSyncDao;

/**
 * 信息采集表Service
 * @author YHQ
 * @version 2023-11-01
 */
@Service
public class MaocheRobotCrawlerMessageSyncService extends CrudService<MaocheRobotCrawlerMessageSyncDao, MaocheRobotCrawlerMessageSyncDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheRobotCrawlerMessageSyncDO
	 * @return
	 */
	@Override
	public MaocheRobotCrawlerMessageSyncDO get(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		return dao.getByEntity(maocheRobotCrawlerMessageSyncDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheRobotCrawlerMessageSyncDO 查询条件
	 * @param maocheRobotCrawlerMessageSyncDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheRobotCrawlerMessageSyncDO> findPage(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		return super.findPage(maocheRobotCrawlerMessageSyncDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheRobotCrawlerMessageSyncDO
	 * @return
	 */
	@Override
	public List<MaocheRobotCrawlerMessageSyncDO> findList(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		return super.findList(maocheRobotCrawlerMessageSyncDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheRobotCrawlerMessageSyncDO
	 */
	@Override
	@Transactional
	public void save(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		dao.add(maocheRobotCrawlerMessageSyncDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheRobotCrawlerMessageSyncDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		super.updateStatus(maocheRobotCrawlerMessageSyncDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheRobotCrawlerMessageSyncDO
	 */
	@Override
	@Transactional
	public void delete(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		super.delete(maocheRobotCrawlerMessageSyncDO);
	}


	public boolean addIfAbsent(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		Long robotMsgId = maocheRobotCrawlerMessageSyncDO.getRobotMsgId();
		if (robotMsgId == null || robotMsgId <= 0) {
			return false;
		}

		MaocheRobotCrawlerMessageSyncDO query = new MaocheRobotCrawlerMessageSyncDO();
		query.setRobotMsgId(robotMsgId);
		MaocheRobotCrawlerMessageSyncDO syncDO = get(query);

		// 新增
		if (syncDO == null) {
			dao.add(maocheRobotCrawlerMessageSyncDO);
			return StringUtils.isNotBlank(maocheRobotCrawlerMessageSyncDO.getId());
		}

		return false;
	}

	// 批量更新
	public boolean updateBatch(List<MaocheRobotCrawlerMessageSyncDO> updateList) {
		if (CollectionUtils.isEmpty(updateList)) {
			return false;
		}

		return dao.updateBatchById(updateList) > 0;
	}
}