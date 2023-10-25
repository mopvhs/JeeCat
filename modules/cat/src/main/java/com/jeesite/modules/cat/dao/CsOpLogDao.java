package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.CsOpLogDO;

/**
 * 操作日志表DAO接口
 * @author YHQ
 * @version 2023-10-21
 */
@MyBatisDao
public interface CsOpLogDao extends CrudDao<CsOpLogDO> {
	
}