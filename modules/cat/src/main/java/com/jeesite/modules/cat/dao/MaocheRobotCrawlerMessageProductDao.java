package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;

/**
 * 信息采集商品表DAO接口
 * @author YHQ
 * @version 2023-11-01
 */
@MyBatisDao
public interface MaocheRobotCrawlerMessageProductDao extends CrudDao<MaocheRobotCrawlerMessageProductDO> {

    int add(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO);
	
}