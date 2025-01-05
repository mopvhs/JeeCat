package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 品牌库DAO接口
 * @author YHQ
 * @version 2024-08-18
 */
@MyBatisDao
public interface MaocheBrandLibDao extends CrudDao<MaocheBrandLibDO> {

    int add(MaocheBrandLibDO libDO);

    int updateById(MaocheBrandLibDO libDO);

    List<MaocheBrandLibDO> listByIds(@Param("ids") List<Long> ids);

    List<MaocheBrandLibDO> listByBrandIds(@Param("brandIds") List<Long> brandIds);

    MaocheBrandLibDO getById(@Param("id") Long id);

}