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
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.service.MaocheTaskService;

/**
 * 任务Controller
 * @author YHQ
 * @version 2023-08-04
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheTaskDO")
public class MaocheTaskDOController extends BaseController {

	@Autowired
	private MaocheTaskService maocheTaskDOService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public MaocheTaskDO get(String id, boolean isNewRecord) {
		return maocheTaskDOService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheTaskDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheTaskDO maocheTaskDO, Model model) {
		model.addAttribute("maocheTaskDO", maocheTaskDO);
		return "modules/cat/maocheTaskDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheTaskDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheTaskDO> listData(MaocheTaskDO maocheTaskDO, HttpServletRequest request, HttpServletResponse response) {
		maocheTaskDO.setPage(new Page<>(request, response));
		Page<MaocheTaskDO> page = maocheTaskDOService.findPage(maocheTaskDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheTaskDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheTaskDO maocheTaskDO, Model model) {
		model.addAttribute("maocheTaskDO", maocheTaskDO);
		return "modules/cat/maocheTaskDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheTaskDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheTaskDO maocheTaskDO) {
		maocheTaskDOService.save(maocheTaskDO);
		return renderResult(Global.TRUE, text("保存任务成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheTaskDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheTaskDO maocheTaskDO) {
		maocheTaskDOService.delete(maocheTaskDO);
		return renderResult(Global.TRUE, text("删除任务成功！"));
	}
	
}