package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.WxChatDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志表DAO接口
 * @author YHQ
 * @version 2023-10-21
 */
@MyBatisDao(dataSourceName = "jubo")
public interface WxChatDao extends CrudDao<WxChatDO> {

    WxChatDO getById(@Param("id") Long id);

    List<WxChatDO> listByWxChatIds(@Param("wxChatIds") List<String> wxChatIds);
}