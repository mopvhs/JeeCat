package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductPriceChartDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * maoche_alimama_union_product_price_chartDAO接口
 * @author YHQ
 * @version 2023-07-15
 */
@MyBatisDao
public interface MaocheAlimamaUnionProductPriceChartDao extends CrudDao<MaocheAlimamaUnionProductPriceChartDO> {

    List<MaocheAlimamaUnionProductPriceChartDO> listLatestChartPrices(@Param("iids") List<String> iids);

    List<MaocheAlimamaUnionProductPriceChartDO> getAllGroupByIid();


	
}