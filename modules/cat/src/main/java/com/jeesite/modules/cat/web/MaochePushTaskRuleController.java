package com.jeesite.modules.cat.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.cat.entity.MaochePushTaskRuleDO;
import com.jeesite.modules.cat.service.MaochePushTaskRuleService;

/**
 * maoche_push_task_ruleController
 * @author YHQ
 * @version 2023-10-28
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maochePushTaskRuleDO")
public class MaochePushTaskRuleController extends BaseController {

	@Autowired
	private MaochePushTaskRuleService maochePushTaskRuleService;

	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maochePushTaskRuleDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaochePushTaskRuleDO maochePushTaskRuleDO, Model model) {
		model.addAttribute("maochePushTaskRuleDO", maochePushTaskRuleDO);
		return "modules/cat/maochePushTaskRuleDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maochePushTaskRuleDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaochePushTaskRuleDO> listData(MaochePushTaskRuleDO maochePushTaskRuleDO, HttpServletRequest request, HttpServletResponse response) {
		maochePushTaskRuleDO.setPage(new Page<>(request, response));
		Page<MaochePushTaskRuleDO> page = maochePushTaskRuleService.findPage(maochePushTaskRuleDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maochePushTaskRuleDO:view")
	@RequestMapping(value = "form")
	public String form(MaochePushTaskRuleDO maochePushTaskRuleDO, Model model) {
		model.addAttribute("maochePushTaskRuleDO", maochePushTaskRuleDO);
		return "modules/cat/maochePushTaskRuleDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maochePushTaskRuleDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaochePushTaskRuleDO maochePushTaskRuleDO) {
		maochePushTaskRuleService.save(maochePushTaskRuleDO);
		return renderResult(Global.TRUE, text("保存maoche_push_task_rule成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maochePushTaskRuleDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaochePushTaskRuleDO maochePushTaskRuleDO) {
		maochePushTaskRuleService.delete(maochePushTaskRuleDO);
		return renderResult(Global.TRUE, text("删除maoche_push_task_rule成功！"));
	}
	
}