package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import org.apache.ibatis.annotations.Param;

/**
 * maoche_category_mappingDAO接口
 * @author YHQ
 * @version 2023-06-19
 */
@MyBatisDao
public interface MaocheCategoryMappingDao extends CrudDao<MaocheCategoryMappingDO> {

    MaocheCategoryMappingDO getByName(@Param(value = "name") String name);

    MaocheCategoryMappingDO getById(@Param(value = "id") Long id);
}