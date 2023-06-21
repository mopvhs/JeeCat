package com.jeesite.modules.cat.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheProductDO;
import com.jeesite.modules.cat.dao.MaocheProductDao;

/**
 * maoche_productService
 * @author YHQ
 * @version 2023-06-16
 */
@Service
public class MaocheProductService extends CrudService<MaocheProductDao, MaocheProductDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheProductDO
	 * @return
	 */
	@Override
	public MaocheProductDO get(MaocheProductDO maocheProductDO) {
		return super.get(maocheProductDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheProductDO 查询条件
	 * @param maocheProductDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheProductDO> findPage(MaocheProductDO maocheProductDO) {
		return super.findPage(maocheProductDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheProductDO
	 * @return
	 */
	@Override
	public List<MaocheProductDO> findList(MaocheProductDO maocheProductDO) {
		return super.findList(maocheProductDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheProductDO
	 */
	@Override
	@Transactional
	public void save(MaocheProductDO maocheProductDO) {
		super.save(maocheProductDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheProductDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheProductDO maocheProductDO) {
		super.updateStatus(maocheProductDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheProductDO
	 */
	@Override
	@Transactional
	public void delete(MaocheProductDO maocheProductDO) {
		super.delete(maocheProductDO);
	}
	
}