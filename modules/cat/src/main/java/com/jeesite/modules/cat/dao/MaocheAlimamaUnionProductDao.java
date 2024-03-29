package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * maoche_alimama_union_productDAO接口
 * @author YHQ
 * @version 2023-05-05
 */
@MyBatisDao
public interface MaocheAlimamaUnionProductDao extends CrudDao<MaocheAlimamaUnionProductDO> {

    // 只返回id无其他内容
    List<MaocheAlimamaUnionProductDO> findAll(@Param("id") Long id, @Param("status") String status, @Param("limit") Integer limit);

    List<MaocheAlimamaUnionProductDO> listByIds(@Param("ids") List<Long> ids);

    List<MaocheAlimamaUnionProductDO> listByIids(@Param("iids") List<String> ids);

    List<MaocheAlimamaUnionProductDO> listSimpleByIds(@Param("ids") List<Long> ids);

    List<MaocheAlimamaUnionProductDO> listSimpleNotContentByIds(@Param("ids") List<Long> ids);

    int updateAuditStatus(@Param("ids") List<Long> ids, @Param("auditStatus") Integer auditStatus, @Param("syncMark") Integer syncMark);

    int updateProductStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    int updateCustomBenefit(@Param("ids") List<Long> ids, @Param("customBenefit") String customBenefit);

    int updateSaleAuditStatus(@Param("ids") List<Long> ids,
                            @Param("saleStatus") Long saleStatus,
                            @Param("onShelfDate") String onShelfDate,
                            @Param("auditStatus") Integer auditStatus,
                            @Param("syncMark") Integer syncMark);

    /**
     *
     * @param ids
     * @param saleStatus
     * @param onShelfDate 上架时间，如果是上架，会覆盖这个值，其他的不会
     * @return
     */
    int updateSaleStatus(@Param("ids") List<Long> ids, @Param("saleStatus") Long saleStatus, @Param("onShelfDate") String onShelfDate);

    int updateQualityStatus(@Param("ids") List<Long> ids, @Param("qualityStatus") Long qualityStatus);
}