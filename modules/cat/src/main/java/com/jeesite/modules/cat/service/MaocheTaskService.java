package com.jeesite.modules.cat.service;

import java.util.Date;
import java.util.List;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.dao.MaocheTaskDao;

/**
 * 任务Service
 * @author YHQ
 * @version 2023-08-04
 */
@Service
public class MaocheTaskService extends CrudService<MaocheTaskDao, MaocheTaskDO> {
	
	/**
	 * 获取单条数据
	 * @param maocheTaskDO
	 * @return
	 */
	@Override
	public MaocheTaskDO get(MaocheTaskDO maocheTaskDO) {
		return super.get(maocheTaskDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maocheTaskDO 查询条件
	 * @param maocheTaskDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheTaskDO> findPage(MaocheTaskDO maocheTaskDO) {
		return super.findPage(maocheTaskDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maocheTaskDO
	 * @return
	 */
	@Override
	public List<MaocheTaskDO> findList(MaocheTaskDO maocheTaskDO) {
		return super.findList(maocheTaskDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maocheTaskDO
	 */
	@Override
	@Transactional
	public void save(MaocheTaskDO maocheTaskDO) {
		super.save(maocheTaskDO);
	}
	
	/**
	 * 更新状态
	 * @param maocheTaskDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheTaskDO maocheTaskDO) {
		super.updateStatus(maocheTaskDO);
	}
	
	/**
	 * 删除数据
	 * @param maocheTaskDO
	 */
	@Override
	@Transactional
	public void delete(MaocheTaskDO maocheTaskDO) {
		super.delete(maocheTaskDO);
	}

	/**
	 *
	 * @param page 从1开始
	 * @param pageSize
	 * @return
	 */
	public List<MaocheTaskDO> getPage(int page, int pageSize) {

		if (page <= 0) {
			page = 1;
		}

		int offset = (page - 1) * pageSize;

		return dao.getPage(offset, pageSize);
	}

	public int getTotal() {
		return dao.getTotal();
	}

	public boolean finishTask(String id) {
		if (StringUtils.isBlank(id)) {
			return false;
		}
		MaocheTaskDO query = new MaocheTaskDO();
		query.setId(id);
		MaocheTaskDO taskDO = dao.get(query);

		TaskStatusEnum statusEnum = TaskStatusEnum.getByName(taskDO.getStatus());
		if (statusEnum == null) {
			return false;
		}

		if (statusEnum == TaskStatusEnum.FINISHED || statusEnum == TaskStatusEnum.EXCEPTION || statusEnum == TaskStatusEnum.DELETE || statusEnum == TaskStatusEnum.INIT) {
			return false;
		}

		return dao.finishTask(taskDO.getId(), DateTimeUtils.getStringDate(new Date())) > 0;
	}
	
}