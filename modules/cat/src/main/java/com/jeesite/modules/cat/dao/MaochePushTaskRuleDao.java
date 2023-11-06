package com.jeesite.modules.cat.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.cat.entity.MaochePushTaskRuleDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * maoche_push_task_ruleDAO接口
 * @author YHQ
 * @version 2023-10-28
 */
@MyBatisDao
public interface MaochePushTaskRuleDao extends CrudDao<MaochePushTaskRuleDO> {

    int updateRule(MaochePushTaskRuleDO maochePushTaskRuleDO);

    int add(MaochePushTaskRuleDO maochePushTaskRuleDO);

    /**
     * 获取形似的关键词
     * @param keyword
     * @return
     */
    List<MaochePushTaskRuleDO> likeKeyword(@Param("keyword") String keyword);
}