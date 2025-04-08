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

    MaocheRobotCrawlerMessageDO getById(@Param("id") Long id);

    List<MaocheRobotCrawlerMessageDO> startById(@Param("id") Long id, @Param("limit") Integer limit, @Param("affTypes") List<String> affTypes);

    List<MaocheRobotCrawlerMessageDO> listRelationMessage();

    List<MaocheRobotCrawlerMessageDO> listFinishedRelationMessage();

    int relationMessage(@Param("ids") List<Long> ids, @Param("relationId") Long relationId);

    int updateStatus(@Param("ids") List<Long> ids, @Param("status") String status);
}