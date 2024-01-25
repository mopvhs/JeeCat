package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.TreeDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.JsDictDataDO;

/**
 * 字典数据表DAO接口
 * @author YHQ
 * @version 2024-01-12
 */
@MyBatisDao
public interface JsDictDataDao extends TreeDao<JsDictDataDO> {
	
}