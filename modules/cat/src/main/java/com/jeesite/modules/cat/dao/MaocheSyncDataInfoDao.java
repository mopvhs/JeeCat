package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheSyncDataInfoDO;
import org.apache.ibatis.annotations.Param;

/**
 * 数据同步位点表DAO接口
 * @author YHQ
 * @version 2023-10-31
 */
@MyBatisDao
public interface MaocheSyncDataInfoDao extends CrudDao<MaocheSyncDataInfoDO> {

    int add(MaocheSyncDataInfoDO dataInfoDO);

    MaocheSyncDataInfoDO getLatestOffset(@Param("tableName") String tableName);

    // 更新位点
    int updateOffset(@Param("id") Long id, @Param("syncTime") Long syncTime, @Param("offset") String offset);
}