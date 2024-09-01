package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;

/**
 * 品牌库DAO接口
 * @author YHQ
 * @version 2024-08-18
 */
@MyBatisDao
public interface MaocheBrandLibDao extends CrudDao<MaocheBrandLibDO> {

    int add(MaocheBrandLibDO libDO);
	
}