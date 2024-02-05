package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务DAO接口
 * @author YHQ
 * @version 2023-08-04
 */
@MyBatisDao
public interface MaocheTaskDao extends CrudDao<MaocheTaskDO> {

    List<MaocheTaskDO> listByIds(@Param("ids") List<String> ids);

    List<MaocheTaskDO> getPage(int offset, int pageSize);

    int getTotal();

    int finishTask(@Param("id") String id, @Param("finishedDate") String finishedDate);

    int openTask(@Param("id") String id);

    int updateStatusSwitch(@Param("id") String id, @Param("status") String status, @Param("taskSwitch") String taskSwitch);

    int updateById(MaocheTaskDO taskDO);

    /**
     * 获取最新的任务
     * @return
     */
    MaocheTaskDO getLatestTask();
}