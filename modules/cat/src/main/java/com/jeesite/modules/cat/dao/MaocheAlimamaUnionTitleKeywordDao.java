package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * maoche_alimama_union_title_keywordDAO接口
 * @author YHQ
 * @version 2023-05-14
 */
@MyBatisDao
public interface MaocheAlimamaUnionTitleKeywordDao extends CrudDao<MaocheAlimamaUnionTitleKeywordDO> {

    int updateTag(@Param("id") Long id, @Param("contentManual") String tag);


}