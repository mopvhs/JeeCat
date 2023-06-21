package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheDataokeProductDO;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

/**
 * maoche_dataoke_productDAO接口
 * @author YHQ
 * @version 2023-06-04
 */
@MyBatisDao
public interface MaocheDataokeProductDao extends CrudDao<MaocheDataokeProductDO> {

    int updateProduct(@Param("id") String id,
                      @Param("originalPrice") Long originalPrice,
                      @Param("actualPrice") Long actualPrice,
                      @Param("couponPrice") Long couponPrice,
                      @Param("commissionRate") Long commissionRate,
                      @Param("monthSales") Long monthSales,
                      @Param("specialText") String specialText,
                      @Param("couponRemainCount") Long couponRemainCount,
                      @Param("couponReceiveNum") Long couponReceiveNum,
                      @Param("updateBy") String updateBy
    );

    int updateStatus(@Param("dtkIds") List<Long> dtkIds, @Param("status") String status);
}