package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * maoche_productDAO接口
 * @author YHQ
 * @version 2023-06-16
 */
@MyBatisDao(dataSourceName = "ds2")
public interface MaocheProductDao extends CrudDao<MaocheProductDO> {

    List<MaocheProductDO> findAll(@Param("id") Long id, @Param("limit") Integer limit);

}