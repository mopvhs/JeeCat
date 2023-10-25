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
import com.jeesite.modules.cat.entity.CsOpLogDO;
import com.jeesite.modules.cat.service.CsOpLogService;

/**
 * 操作日志表Controller
 * @author YHQ
 * @version 2023-10-21
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/csOpLogDO")
public class CsOpLogDOController extends BaseController {

	@Autowired
	private CsOpLogService csOpLogDOService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public CsOpLogDO get(String id, boolean isNewRecord) {
		return csOpLogDOService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:csOpLogDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(CsOpLogDO csOpLogDO, Model model) {
		model.addAttribute("csOpLogDO", csOpLogDO);
		return "modules/cat/csOpLogDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:csOpLogDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<CsOpLogDO> listData(CsOpLogDO csOpLogDO, HttpServletRequest request, HttpServletResponse response) {
		csOpLogDO.setPage(new Page<>(request, response));
		Page<CsOpLogDO> page = csOpLogDOService.findPage(csOpLogDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:csOpLogDO:view")
	@RequestMapping(value = "form")
	public String form(CsOpLogDO csOpLogDO, Model model) {
		model.addAttribute("csOpLogDO", csOpLogDO);
		return "modules/cat/csOpLogDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:csOpLogDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated CsOpLogDO csOpLogDO) {
		csOpLogDOService.save(csOpLogDO);
		return renderResult(Global.TRUE, text("保存操作日志表成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:csOpLogDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(CsOpLogDO csOpLogDO) {
		csOpLogDOService.delete(csOpLogDO);
		return renderResult(Global.TRUE, text("删除操作日志表成功！"));
	}
	
}