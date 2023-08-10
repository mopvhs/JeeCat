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

    List<MaocheTaskDO> getPage(int offset, int pageSize);

    int getTotal();

    int finishTask(@Param("id") String id, @Param("finishedDate") String finishedDate);
}