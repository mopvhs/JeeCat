package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.enums.task.TaskSwitchEnum;
import org.apache.commons.collections.CollectionUtils;
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
		return dao.getByEntity(maocheTaskDO);
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

	public boolean updateStatusSwitch(String id, TaskStatusEnum statusEnum, TaskSwitchEnum taskSwitchEnum) {
		if (StringUtils.isBlank(id) || statusEnum == null || taskSwitchEnum == null) {
			return false;
		}

		return dao.updateStatusSwitch(id, statusEnum.name(), taskSwitchEnum.name()) > 0;
	}

	public boolean openTask(String id) {
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
		if (statusEnum == TaskStatusEnum.FINISHED || statusEnum == TaskStatusEnum.EXCEPTION || statusEnum == TaskStatusEnum.DELETE) {
			return false;
		}

		return dao.openTask(taskDO.getId()) > 0;
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

	public List<MaocheTaskDO> listByIds(List<String> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return new ArrayList<>();
		}

		return dao.listByIds(ids);
	}

	public boolean createTask(MaocheTaskDO task) {
		if (task == null) {
			return false;
		}

		task.setCreateBy("admin");
		task.setUpdateBy("admin");
		task.setCreateDate(new Date());
		task.setUpdateDate(new Date());
		task.setStatus(TaskStatusEnum.NORMAL.name());
		task.setTaskSwitch(TaskSwitchEnum.CLOSE.name());
		task.setSwitchDate(null);

		super.save(task);

		return StringUtils.isNotBlank(task.getId());
	}


	public boolean createOrUpdateTask(MaocheTaskDO task) {
		if (task == null) {
			return false;
		}
		if (StringUtils.isBlank(task.getId())) {
			return createTask(task);
		}

		return dao.updateById(task) > 0;
	}
}