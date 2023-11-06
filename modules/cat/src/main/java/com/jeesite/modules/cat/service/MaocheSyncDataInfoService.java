package com.jeesite.modules.cat.service;

import java.util.Date;
import java.util.List;

import com.jeesite.common.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheSyncDataInfoDO;
import com.jeesite.modules.cat.dao.MaocheSyncDataInfoDao;

/**
 * 数据同步位点表Service
 * @author YHQ
 * @version 2023-10-31
 */
@Service
public class MaocheSyncDataInfoService extends CrudService<MaocheSyncDataInfoDao, MaocheSyncDataInfoDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheSyncDataInfoDO
	 * @return
	 */
	@Override
	public MaocheSyncDataInfoDO get(MaocheSyncDataInfoDO maocheSyncDataInfoDO) {
		return super.get(maocheSyncDataInfoDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheSyncDataInfoDO 查询条件
	 * @param maocheSyncDataInfoDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheSyncDataInfoDO> findPage(MaocheSyncDataInfoDO maocheSyncDataInfoDO) {
		return super.findPage(maocheSyncDataInfoDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheSyncDataInfoDO
	 * @return
	 */
	@Override
	public List<MaocheSyncDataInfoDO> findList(MaocheSyncDataInfoDO maocheSyncDataInfoDO) {
		return super.findList(maocheSyncDataInfoDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheSyncDataInfoDO
	 */
	@Override
	@Transactional
	public void save(MaocheSyncDataInfoDO maocheSyncDataInfoDO) {
		super.save(maocheSyncDataInfoDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheSyncDataInfoDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheSyncDataInfoDO maocheSyncDataInfoDO) {
		super.updateStatus(maocheSyncDataInfoDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheSyncDataInfoDO
	 */
	@Override
	@Transactional
	public void delete(MaocheSyncDataInfoDO maocheSyncDataInfoDO) {
		super.delete(maocheSyncDataInfoDO);
	}

	// 获取最新位点
	public MaocheSyncDataInfoDO getLatestSyncDataInfo(String tableName) {

		if (StringUtils.isBlank(tableName)) {
			return null;
		}
		MaocheSyncDataInfoDO latestOffset = dao.getLatestOffset(tableName);

		return latestOffset;
	}

	// 获取最新位点
	public boolean addOrUpdateOffset(long id, String tableName, String offset) {

		if (StringUtils.isBlank(tableName) || StringUtils.isBlank(offset)) {
			return false;
		}
		// 新增
		if (id <= 0) {
			MaocheSyncDataInfoDO infoDO = new MaocheSyncDataInfoDO();
			infoDO.setSyncMaxId(offset);
			infoDO.setStep(100);
			infoDO.setTableName(tableName);
			infoDO.setSyncTime(System.currentTimeMillis() / 1000);
			infoDO.setStatus("NORMAL");
			infoDO.setCreateBy("admin");
			infoDO.setUpdateBy("admin");
			infoDO.setCreateDate(new Date());
			infoDO.setUpdateDate(new Date());
			infoDO.setRemarks("");

			dao.add(infoDO);

			if (StringUtils.isBlank(infoDO.getId())) {
				return false;
			}
		} else {
			// 修改
			int i = dao.updateOffset(id, System.currentTimeMillis() / 1000, offset);
			if (i <= 0) {
				return false;
			}
		}

		return true;
	}
	
}