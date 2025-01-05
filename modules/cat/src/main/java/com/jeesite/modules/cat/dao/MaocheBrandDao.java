package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * maoche_brandDAO接口
 * @author YHQ
 * @version 2024-08-19
 */
@MyBatisDao
public interface MaocheBrandDao extends CrudDao<MaocheBrandDO> {

    int add(MaocheBrandDO brandDO);

    List<MaocheBrandDO> listByIds(@Param("ids") List<Long> ids);

    MaocheBrandDO getById(@Param("id") Long id);

    MaocheBrandDO getByName(@Param("name") String name);

    int updateById(MaocheBrandDO brandDO);
}