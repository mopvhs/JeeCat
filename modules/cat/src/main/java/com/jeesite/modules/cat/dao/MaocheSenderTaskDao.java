package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheSenderTaskDO;

/**
 * 主动发布任务DAO接口
 * @author YHQ
 * @version 2023-05-28
 */
@MyBatisDao
public interface MaocheSenderTaskDao extends CrudDao<MaocheSenderTaskDO> {
	
}