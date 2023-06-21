package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.QwConfigInfoDO;

/**
 * 企微配置详情数据DAO接口
 * @author YHQ
 * @version 2023-06-21
 */
@MyBatisDao
public interface QwConfigInfoDao extends CrudDao<QwConfigInfoDO> {
	
}