package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheSenderTaskDO;
import org.apache.ibatis.annotations.Param;

/**
 * 主动发布任务DAO接口
 * @author YHQ
 * @version 2023-05-28
 */
@MyBatisDao
public interface MaocheSenderTaskDao extends CrudDao<MaocheSenderTaskDO> {

    public int updateContentById(@Param("id") Long id, @Param("content") String content);
	
}