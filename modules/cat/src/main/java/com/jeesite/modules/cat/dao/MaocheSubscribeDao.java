package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaocheSubscribeDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订阅表DAO接口
 * @author YhQ
 * @version 2024-11-30
 */
@MyBatisDao
public interface MaocheSubscribeDao extends CrudDao<MaocheSubscribeDO> {

    MaocheSubscribeDO getUserSubscribe(@Param("userId") String userId, @Param("subscribeId") String subscribeId, @Param("subscribeType") String subscribeType);

    List<MaocheSubscribeDO> listUserSubscribe(@Param("userId") String userId, @Param("subscribeIds") List<String> subscribeIds, @Param("subscribeType") String subscribeType);

    int updateById(@Param("id") String id, @Param("status") String status, @Param("openSwitch") String openSwitch);

    List<MaocheSubscribeDO> listUserSubscribes(@Param("userId") String userId, @Param("subscribeType") String subscribeType);
}