package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import lombok.Data;
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

    MaocheRobotCrawlerMessageSyncDO getById(@Param("id") Long id);

    List<MaocheRobotCrawlerMessageSyncDO> listByOriUniqueHash(@Param("oriUniqueHash") String oriUniqueHash, @Param("statusList") List<String> status);

    List<MaocheRobotCrawlerMessageSyncDO> listByProductUniqueHash(@Param("productHash") String productHash, @Param("status") String status, @Param("startTime") String startTime);

    int updateById(MaocheRobotCrawlerMessageSyncDO update);

    /**
     * 获取最新的n条相似消息
     * @param date
     * @param limit
     * @return
     */
    List<MaocheRobotCrawlerMessageSyncDO> listSimilar(@Param("date") String date, @Param("limit") int limit);

    List<MaocheRobotCrawlerMessageSyncDO> getByRobotMsgId(@Param("robotMsgId") Long robotMsgId);

    List<MaocheRobotCrawlerMessageSyncDO> listRobotMsgIds(@Param("robotMsgIds") List<Long> robotMsgIds, @Param("status") String status);

    List<MaocheRobotCrawlerMessageSyncDO> listRobotStatusMsgIds(@Param("robotMsgIds") List<Long> robotMsgIds, @Param("statusList") List<String> statusList);


    /**
     *
     * @param status
     * @param limit
     * @return
     */
    List<MaocheRobotCrawlerMessageSyncDO> listStatusAffType(@Param("status") String status, @Param("affType") String affType, @Param("limit") int limit);


    List<MaocheRobotCrawlerMessageSyncDO> findSimHashMessages(@Param("uniqueHash") String uniqueHash, @Param("status") String status, @Param("wxTime") String startWxTime);

}