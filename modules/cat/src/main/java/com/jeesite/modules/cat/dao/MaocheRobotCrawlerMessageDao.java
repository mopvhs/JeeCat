package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 信息采集表DAO接口
 * @author YHQ
 * @version 2023-04-30
 */
@MyBatisDao
public interface MaocheRobotCrawlerMessageDao extends CrudDao<MaocheRobotCrawlerMessageDO> {


    List<MaocheRobotCrawlerMessageDO> findAll(@Param("id") Long id, @Param("limit") Integer limit);
}