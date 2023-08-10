package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 推送任务DAO接口
 * @author YHQ
 * @version 2023-08-04
 */
@MyBatisDao
public interface MaochePushTaskDao extends CrudDao<MaochePushTaskDO> {

    int updateStatus(@Param("ids") List<String> ids, @Param("status") String status);

    int finishPushTask(@Param("id") String id, @Param("finishedDate") String finishedDate);

    int updateStatusById(@Param("id") String id, @Param("oldStatus") String oldStatus, @Param("newStatus") String newStatus);

    List<MaochePushTaskDO> queryByStatus(@Param("status") String status, @Param("publishDate") String publishDate, @Param("limit") int limit);
	
}