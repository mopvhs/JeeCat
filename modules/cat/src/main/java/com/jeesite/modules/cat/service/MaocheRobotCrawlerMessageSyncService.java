package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.List;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.enums.OceanStatusEnum;
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
		// 判断ori_unique_hash是否一样
		String oriUniqueHash = maocheRobotCrawlerMessageSyncDO.getOriUniqueHash();
		List<MaocheRobotCrawlerMessageSyncDO> syncDOs = dao.listByOriUniqueHash(oriUniqueHash, null);

//		List<MaocheRobotCrawlerMessageSyncDO> syncDOs = dao.getByRobotMsgId(robotMsgId);
		// 新增
		if (CollectionUtils.isEmpty(syncDOs)) {
			dao.add(maocheRobotCrawlerMessageSyncDO);
			return StringUtils.isNotBlank(maocheRobotCrawlerMessageSyncDO.getId());
		}

		return false;
	}

	public boolean updateById(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		Long id = maocheRobotCrawlerMessageSyncDO.getUiid();
		if (id == null || id <= 0) {
			return false;
		}

		int i = dao.updateById(maocheRobotCrawlerMessageSyncDO);

		return i > 0;
	}

	// 批量更新
	public boolean updateBatch(List<MaocheRobotCrawlerMessageSyncDO> updateList) {
		if (CollectionUtils.isEmpty(updateList)) {
			return false;
		}

		return dao.updateBatchById(updateList) > 0;
	}

	// 查询列表
	public List<MaocheRobotCrawlerMessageSyncDO> listByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return new ArrayList<>();
		}

		return dao.listByIds(ids);
	}

	// 查询列表
	public List<MaocheRobotCrawlerMessageSyncDO> getByRobotMsgId(Long robotMsgId) {
		if (robotMsgId == null || robotMsgId <= 0) {
			return new ArrayList<>();
		}

		return dao.getByRobotMsgId(robotMsgId);
	}


	// 查询列表
	public List<MaocheRobotCrawlerMessageSyncDO> listStatusAffType(OceanStatusEnum statusEnum, String affType, int limit) {
		if (statusEnum == null || StringUtils.isBlank(affType)) {
			return new ArrayList<>();
		}

		return dao.listStatusAffType(statusEnum.name(), affType, limit);
	}


}