package com.jeesite.modules.cat.service;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaochePushTaskRuleDO;
import com.jeesite.modules.cat.dao.MaochePushTaskRuleDao;

/**
 * maoche_push_task_ruleService
 * @author YHQ
 * @version 2023-10-28
 */
@Service
public class MaochePushTaskRuleService extends CrudService<MaochePushTaskRuleDao, MaochePushTaskRuleDO> {

	/**
	 * 获取单条数据
	 * @param maochePushTaskRuleDO
	 * @return
	 */
	@Override
	public MaochePushTaskRuleDO get(MaochePushTaskRuleDO maochePushTaskRuleDO) {
		return dao.getByEntity(maochePushTaskRuleDO);
	}

	/**
	 * 查询分页数据
	 * @param maochePushTaskRuleDO 查询条件
	 * @param maochePushTaskRuleDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaochePushTaskRuleDO> findPage(MaochePushTaskRuleDO maochePushTaskRuleDO) {
		return super.findPage(maochePushTaskRuleDO);
	}

	/**
	 * 查询列表数据
	 * @param maochePushTaskRuleDO
	 * @return
	 */
	@Override
	public List<MaochePushTaskRuleDO> findList(MaochePushTaskRuleDO maochePushTaskRuleDO) {
		return super.findList(maochePushTaskRuleDO);
	}

	/**
	 * 保存数据（插入或更新）
	 * @param maochePushTaskRuleDO
	 */
	@Override
	@Transactional
	public void save(MaochePushTaskRuleDO maochePushTaskRuleDO) {
		dao.add(maochePushTaskRuleDO);
	}

	/**
	 * 更新状态
	 * @param maochePushTaskRuleDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaochePushTaskRuleDO maochePushTaskRuleDO) {
		super.updateStatus(maochePushTaskRuleDO);
	}

	/**
	 * 删除数据
	 * @param maochePushTaskRuleDO
	 */
	@Override
	@Transactional
	public void delete(MaochePushTaskRuleDO maochePushTaskRuleDO) {
		super.delete(maochePushTaskRuleDO);
	}

	public boolean updateRule(MaochePushTaskRuleDO maochePushTaskRuleDO) {
		if (maochePushTaskRuleDO == null) {
			return false;
		}

		if (StringUtils.isBlank(maochePushTaskRuleDO.getId())) {
			return false;
		}

		return dao.updateRule(maochePushTaskRuleDO) > 0;
	}

	/**
	 * true: keyword is valid
	 * false: keyword is invalid
	 * @param keyword
	 * @return
	 */
	public boolean checkKeyword(String keyword) {
		if (StringUtils.isBlank(keyword)) {
			return false;
		}

		List<MaochePushTaskRuleDO> rules = dao.likeKeyword(keyword);
		if (CollectionUtils.isEmpty(rules)) {
			return true;
		}
		for (MaochePushTaskRuleDO rule : rules) {
			String content = rule.getKeyword();
			if (StringUtils.isBlank(content)) {
				continue;
			}
			List<String> items = JsonUtils.toReferenceType(content, new TypeReference<List<String>>() {
			});
			if (CollectionUtils.isEmpty(items)) {
				continue;
			}
			for (String item : items) {
				if (keyword.equals(item)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * @return
	 */
	public List<String> getKeywords(Long id) {

		if (id == null || id <= 0) {
			return new ArrayList<>();
		}
		MaochePushTaskRuleDO ruleQuery = new MaochePushTaskRuleDO();
		ruleQuery.setId(String.valueOf(id));
		MaochePushTaskRuleDO rule = dao.getByEntity(ruleQuery);
		if (rule == null || StringUtils.isBlank(rule.getKeyword())) {
			return new ArrayList<>();
		}

		try {
			List<String> keywords = JsonUtils.toReferenceType(rule.getKeyword(), new TypeReference<List<String>>() {
			});
			if (CollectionUtils.isNotEmpty(keywords)) {
				return keywords;
			}

		} catch (Exception e) {
			logger.error("getKeywords error, rule:{}", JsonUtils.toJSONString(rule), e);
		}

		return new ArrayList<>();
	}

	/**
	 * 获取所有的品牌库
	 * @return
	 */
	public List<MaochePushTaskRuleDO> getAllBrandLib() {
		MaochePushTaskRuleDO query = new MaochePushTaskRuleDO();
		query.setStatus("NORMAL");

		return findList(query);
	}

}