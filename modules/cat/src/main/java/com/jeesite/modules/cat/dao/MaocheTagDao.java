package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheTagDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * maoche_tagDAO接口
 * @author YHQ
 * @version 2023-10-29
 */
@MyBatisDao
public interface MaocheTagDao extends CrudDao<MaocheTagDO> {

    int add(MaocheTagDO maocheTagDO);

    List<MaocheTagDO> findAll(@Param("id") Long id, @Param("limit") Integer limit);

    List<MaocheTagDO> listByNames(@Param("names") List<String> names);

}