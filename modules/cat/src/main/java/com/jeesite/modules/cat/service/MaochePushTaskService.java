package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.modules.cat.entity.meta.TaskStatusCount;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import org.apache.commons.collections.CollectionUtils;
import org.beetl.ext.fn.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.dao.MaochePushTaskDao;

/**
 * 推送任务Service
 * @author YHQ
 * @version 2023-08-04
 */
@Service
public class MaochePushTaskService extends CrudService<MaochePushTaskDao, MaochePushTaskDO> {
	
	/**
	 * 获取单条数据
	 * @param maochePushTaskDO
	 * @return
	 */
	@Override
	public MaochePushTaskDO get(MaochePushTaskDO maochePushTaskDO) {
		return super.get(maochePushTaskDO);
	}
	
	/**
	 * 查询分页数据
	 * @param maochePushTaskDO 查询条件
	 * @param maochePushTaskDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaochePushTaskDO> findPage(MaochePushTaskDO maochePushTaskDO) {
		return super.findPage(maochePushTaskDO);
	}
	
	/**
	 * 查询列表数据
	 * @param maochePushTaskDO
	 * @return
	 */
	@Override
	public List<MaochePushTaskDO> findList(MaochePushTaskDO maochePushTaskDO) {
		return super.findList(maochePushTaskDO);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param maochePushTaskDO
	 */
	@Override
	@Transactional
	public void save(MaochePushTaskDO maochePushTaskDO) {
		super.save(maochePushTaskDO);
	}
	
	/**
	 * 更新状态
	 * @param maochePushTaskDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaochePushTaskDO maochePushTaskDO) {
		super.updateStatus(maochePushTaskDO);
	}
	
	/**
	 * 删除数据
	 * @param maochePushTaskDO
	 */
	@Override
	@Transactional
	public void delete(MaochePushTaskDO maochePushTaskDO) {
		super.delete(maochePushTaskDO);
	}

	public List<MaochePushTaskDO> queryList(MaochePushTaskDO maochePushTaskDO) {
		return dao.findList(maochePushTaskDO);
	}

	public int updateStatus(String id, TaskStatusEnum status) {

		if (StringUtils.isBlank(id) || status == null) {
			return 0;
		}

		return dao.updateStatus(new ArrayList<>(Collections.singleton(id)), status.name());
	}

	public int updateStatus(List<String> ids, String status) {

		if (CollectionUtils.isEmpty(ids)) {
			return 0;
		}
		TaskStatusEnum statusEnum = TaskStatusEnum.getByName(status);
		if (statusEnum == null) {
			return 0;
		}

		return dao.updateStatus(ids, statusEnum.name());
	}

	public List<MaochePushTaskDO> listValidTask(int limit) {

		List<MaochePushTaskDO> tasks = dao.queryByStatus(TaskStatusEnum.NORMAL.name(), DateTimeUtils.getStringDate(new Date()), limit);

		return tasks;
	}

	public int updateStatus(String id, String oldStatus, String newStatus) {

		if (StringUtils.isBlank(id)) {
			return 0;
		}
		TaskStatusEnum oldStatusEnum = TaskStatusEnum.getByName(oldStatus);
		TaskStatusEnum newStatusEnum = TaskStatusEnum.getByName(newStatus);
		if (oldStatusEnum == null || newStatusEnum == null) {
			return 0;
		}


		return dao.updateStatusById(id, oldStatusEnum.name(), newStatusEnum.name());
	}

	public int updateStatus(List<String> ids, String status, Date publishDate) {

		if (CollectionUtils.isEmpty(ids)) {
			return 0;
		}

		TaskStatusEnum statusEnum = TaskStatusEnum.getByName(status);
		if (statusEnum == null) {
			return 0;
		}

		ids = ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());

		String date = DateTimeUtils.getStringDate(publishDate);

		return dao.updateStatusPublishDate(ids, statusEnum.name(), date);
	}

	public boolean finishPushTask(MaochePushTaskDO taskDO) {
		if (taskDO == null) {
			return false;
		}

		TaskStatusEnum statusEnum = TaskStatusEnum.getByName(taskDO.getStatus());
		if (statusEnum == null) {
			return false;
		}

		if (statusEnum == TaskStatusEnum.FINISHED || statusEnum == TaskStatusEnum.EXCEPTION || statusEnum == TaskStatusEnum.DELETE || statusEnum == TaskStatusEnum.INIT) {
			return false;
		}

		return dao.finishPushTask(taskDO.getId(), DateTimeUtils.getStringDate(new Date())) > 0;
	}

	public List<MaochePushTaskDO> getByTaskIds(List<String> taskIds) {
		if (CollectionUtils.isEmpty(taskIds)) {
			return new ArrayList<>();
		}
		taskIds = taskIds.stream().distinct().collect(Collectors.toList());

		return dao.getByTaskIds(taskIds);
	}

	public List<TaskStatusCount> countResourceStatus(List<Long> resourceIds, String resourceType, TaskStatusEnum statusEnum) {
		if (CollectionUtils.isEmpty(resourceIds) || StringUtils.isBlank(resourceType) || statusEnum == null) {
			return new ArrayList<>();
		}
		resourceIds = resourceIds.stream().distinct().collect(Collectors.toList());

//		List<String> tempIds = resourceIds.stream().map(i -> "'" + i + "'").toList();
//		List<String> tempIds = resourceIds.stream().map(i -> "\"" + i + "\"").toList();
		List<String> tempIds = resourceIds.stream().map(String::valueOf).toList();

		return dao.countResourceStatus(tempIds, resourceType, statusEnum.name());
	}

	public boolean createPushTask(MaochePushTaskDO pushTaskDO) {
		if (pushTaskDO == null) {
			return false;
		}

		pushTaskDO.setCreateBy("admin");
		pushTaskDO.setUpdateBy("admin");
		pushTaskDO.setCreateDate(new Date());
		pushTaskDO.setUpdateDate(new Date());

		return dao.insert(pushTaskDO) > 0;
	}

	public boolean createOrUpdatePushTask(MaochePushTaskDO pushTaskDO) {
		if (pushTaskDO == null) {
			return false;
		}

		if (StringUtils.isBlank(pushTaskDO.getId())) {
			return createPushTask(pushTaskDO);
		}

		// 更新的话 只允许修改部分字段
		return dao.updateById(pushTaskDO) > 0;
	}

	public List<MaochePushTaskDO> getByIds(List<String> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return new ArrayList<>();
		}

		return dao.getByIds(ids);
	}
}