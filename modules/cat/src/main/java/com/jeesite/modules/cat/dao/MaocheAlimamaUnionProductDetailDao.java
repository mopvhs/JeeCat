package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.entity.MaocheCategoryDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * maoche_alimama_union_product_detailDAO接口
 * @author YHQ
 * @version 2023-05-28
 */
@MyBatisDao
public interface MaocheAlimamaUnionProductDetailDao extends CrudDao<MaocheAlimamaUnionProductDetailDO> {

    List<MaocheAlimamaUnionProductDetailDO> findAll(@Param("id") Long id, @Param("limit") Integer limit);

    int updateById(MaocheAlimamaUnionProductDetailDO detailDO);


    List<MaocheAlimamaUnionProductDetailDO> listByIids(@Param(value = "iids") List<String> iids);

}