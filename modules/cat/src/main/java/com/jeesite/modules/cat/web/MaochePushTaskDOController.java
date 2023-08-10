package com.jeesite.modules.cat.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.service.MaochePushTaskService;

/**
 * 推送任务Controller
 * @author YHQ
 * @version 2023-08-04
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maochePushTaskDO")
public class MaochePushTaskDOController extends BaseController {

	@Autowired
	private MaochePushTaskService maochePushTaskDOService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public MaochePushTaskDO get(String id, boolean isNewRecord) {
		return maochePushTaskDOService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maochePushTaskDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaochePushTaskDO maochePushTaskDO, Model model) {
		model.addAttribute("maochePushTaskDO", maochePushTaskDO);
		return "modules/cat/maochePushTaskDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maochePushTaskDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaochePushTaskDO> listData(MaochePushTaskDO maochePushTaskDO, HttpServletRequest request, HttpServletResponse response) {
		maochePushTaskDO.setPage(new Page<>(request, response));
		Page<MaochePushTaskDO> page = maochePushTaskDOService.findPage(maochePushTaskDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maochePushTaskDO:view")
	@RequestMapping(value = "form")
	public String form(MaochePushTaskDO maochePushTaskDO, Model model) {
		model.addAttribute("maochePushTaskDO", maochePushTaskDO);
		return "modules/cat/maochePushTaskDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maochePushTaskDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaochePushTaskDO maochePushTaskDO) {
		maochePushTaskDOService.save(maochePushTaskDO);
		return renderResult(Global.TRUE, text("保存推送任务成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maochePushTaskDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaochePushTaskDO maochePushTaskDO) {
		maochePushTaskDOService.delete(maochePushTaskDO);
		return renderResult(Global.TRUE, text("删除推送任务成功！"));
	}
	
}