package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.dao.MaocheBrandDao;

/**
 * maoche_brandService
 * @author YHQ
 * @version 2024-08-19
 */
@Service
public class MaocheBrandMapper extends CrudService<MaocheBrandDao, MaocheBrandDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheBrandDO
	 * @return
	 */
	@Override
	public MaocheBrandDO get(MaocheBrandDO maocheBrandDO) {
		return super.get(maocheBrandDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheBrandDO 查询条件
	 * @param maocheBrandDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheBrandDO> findPage(MaocheBrandDO maocheBrandDO) {
		return super.findPage(maocheBrandDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheBrandDO
	 * @return
	 */
	@Override
	public List<MaocheBrandDO> findList(MaocheBrandDO maocheBrandDO) {
		return super.findList(maocheBrandDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheBrandDO
	 */
	@Override
	@Transactional
	public void save(MaocheBrandDO maocheBrandDO) {
		super.save(maocheBrandDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheBrandDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheBrandDO maocheBrandDO) {
		super.updateStatus(maocheBrandDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheBrandDO
	 */
	@Override
	@Transactional
	public void delete(MaocheBrandDO maocheBrandDO) {
		super.delete(maocheBrandDO);
	}
	
}