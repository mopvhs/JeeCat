package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheCategoryDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * maoche_categoryDAO接口
 * @author YHQ
 * @version 2023-05-24
 */
@MyBatisDao
public interface MaocheCategoryDao extends CrudDao<MaocheCategoryDO> {

    List<MaocheCategoryDO> findAll(@Param("id") Long id, @Param("limit") Integer limit);

}