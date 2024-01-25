package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.TreeService;
import com.jeesite.modules.cat.entity.JsDictDataDO;
import com.jeesite.modules.cat.dao.JsDictDataDao;

/**
 * 字典数据表Service
 * @author YHQ
 * @version 2024-01-12
 */
@Service
public class JsDictDataService extends TreeService<JsDictDataDao, JsDictDataDO> {
	
	/**
	 * 获取单条数据
	 * @param jsDictDataDO
	 * @return
	 */
	@Override
	public JsDictDataDO get(JsDictDataDO jsDictDataDO) {
		return super.get(jsDictDataDO);
	}
	
	/**
	 * 查询分页数据
	 * @param jsDictDataDO 查询条件
	 * @param jsDictDataDO page 分页对象
	 * @return
	 */
	@Override
	public Page<JsDictDataDO> findPage(JsDictDataDO jsDictDataDO) {
		return super.findPage(jsDictDataDO);
	}
	
	/**
	 * 查询列表数据
	 * @param jsDictDataDO
	 * @return
	 */
	@Override
	public List<JsDictDataDO> findList(JsDictDataDO jsDictDataDO) {
		return super.findList(jsDictDataDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param jsDictDataDO
	 */
	@Override
	@Transactional
	public void save(JsDictDataDO jsDictDataDO) {
		super.save(jsDictDataDO);
	}
	
	/**
	 * 更新状态
	 * @param jsDictDataDO
	 */
	@Override
	@Transactional
	public void updateStatus(JsDictDataDO jsDictDataDO) {
		super.updateStatus(jsDictDataDO);
	}
	
	/**
	 * 删除数据
	 * @param jsDictDataDO
	 */
	@Override
	@Transactional
	public void delete(JsDictDataDO jsDictDataDO) {
		super.delete(jsDictDataDO);
	}
	
}