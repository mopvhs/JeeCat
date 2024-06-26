package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheProductV2DO;
import org.apache.ibatis.annotations.Param;

/**
 * maoche_product_v2DAO接口
 * @author YHQ
 * @version 2024-01-02
 */
@MyBatisDao(dataSourceName = "maoche_product")
public interface MaocheProductV2Dao extends CrudDao<MaocheProductV2DO> {

    MaocheProductV2DO getByItemIdSuffix(@Param("suffixId") String suffixId, @Param("status") String status);
}