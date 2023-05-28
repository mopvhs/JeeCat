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
import com.jeesite.modules.cat.entity.MaocheSenderTaskDO;
import com.jeesite.modules.cat.service.MaocheSenderTaskService;

/**
 * 主动发布任务Controller
 * @author YHQ
 * @version 2023-05-28
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheSenderTaskDO")
public class MaocheSenderTaskController extends BaseController {

	@Autowired
	private MaocheSenderTaskService maocheSenderTaskDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheSenderTaskDO get(Long id, boolean isNewRecord) {
//		return maocheSenderTaskDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheSenderTaskDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheSenderTaskDO maocheSenderTaskDO, Model model) {
		model.addAttribute("maocheSenderTaskDO", maocheSenderTaskDO);
		return "modules/cat/maocheSenderTaskDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheSenderTaskDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheSenderTaskDO> listData(MaocheSenderTaskDO maocheSenderTaskDO, HttpServletRequest request, HttpServletResponse response) {
		maocheSenderTaskDO.setPage(new Page<>(request, response));
		Page<MaocheSenderTaskDO> page = maocheSenderTaskDOService.findPage(maocheSenderTaskDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheSenderTaskDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheSenderTaskDO maocheSenderTaskDO, Model model) {
		model.addAttribute("maocheSenderTaskDO", maocheSenderTaskDO);
		return "modules/cat/maocheSenderTaskDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheSenderTaskDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheSenderTaskDO maocheSenderTaskDO) {
		maocheSenderTaskDOService.save(maocheSenderTaskDO);
		return renderResult(Global.TRUE, text("保存主动发布任务成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheSenderTaskDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheSenderTaskDO maocheSenderTaskDO) {
		maocheSenderTaskDOService.delete(maocheSenderTaskDO);
		return renderResult(Global.TRUE, text("删除主动发布任务成功！"));
	}
	
}