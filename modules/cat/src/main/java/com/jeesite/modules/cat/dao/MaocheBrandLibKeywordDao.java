package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 品牌库关键词DAO接口
 * @author YHQ
 * @version 2024-08-18
 */
@MyBatisDao
public interface MaocheBrandLibKeywordDao extends CrudDao<MaocheBrandLibKeywordDO> {

    int add(MaocheBrandLibKeywordDO libDO);

    List<MaocheBrandLibKeywordDO> listByLibIds(@Param("libIds") List<Long> libIds);

    List<MaocheBrandLibKeywordDO> listByIds(@Param("ids") List<Long> ids);

    MaocheBrandLibKeywordDO getById(@Param("id") Long id);

    int incrSubscribeCount(@Param("id") Long id, @Param("count") Integer count);

    int decrSubscribeCount(@Param("id") Long id, @Param("count") Integer count);

}