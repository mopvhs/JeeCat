package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductBihaohuoDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductPriceChartDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * maoche_alimama_union_product_bihaohuoDAO接口
 * @author YHQ
 * @version 2023-07-22
 */
@MyBatisDao
public interface MaocheAlimamaUnionProductBihaohuoDao extends CrudDao<MaocheAlimamaUnionProductBihaohuoDO> {

    List<Long> listLatestChartPricesId(@Param("iids") List<String> iids);

    List<Long> listLatestChartPricesIdByProductId(@Param("productIds") List<Long> productIds);

}