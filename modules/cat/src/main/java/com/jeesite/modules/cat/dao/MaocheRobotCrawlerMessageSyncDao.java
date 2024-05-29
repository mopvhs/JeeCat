package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 信息采集表DAO接口
 * @author YHQ
 * @version 2023-11-01
 */
@MyBatisDao
public interface MaocheRobotCrawlerMessageSyncDao extends CrudDao<MaocheRobotCrawlerMessageSyncDO> {

    int add(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO);

    List<MaocheRobotCrawlerMessageSyncDO> findAll(@Param("id") Long id, @Param("limit") Integer limit);

    int updateBatchById(@Param("data") List<MaocheRobotCrawlerMessageSyncDO> list);

    List<MaocheRobotCrawlerMessageSyncDO> listByIds(@Param("ids") List<Long> ids);

    /**
     * 获取最新的n条相似消息
     * @param date
     * @param limit
     * @return
     */
    List<MaocheRobotCrawlerMessageSyncDO> listSimilar(@Param("date") String date, @Param("limit") int limit);

}