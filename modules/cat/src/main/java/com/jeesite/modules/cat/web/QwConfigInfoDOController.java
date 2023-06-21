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
import com.jeesite.modules.cat.entity.QwConfigInfoDO;
import com.jeesite.modules.cat.service.QwConfigInfoService;

/**
 * 企微配置详情数据Controller
 * @author YHQ
 * @version 2023-06-21
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/qwConfigInfoDO")
public class QwConfigInfoDOController extends BaseController {

	@Autowired
	private QwConfigInfoService qwConfigInfoDOService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public QwConfigInfoDO get(Long id, boolean isNewRecord) {
		return qwConfigInfoDOService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:qwConfigInfoDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(QwConfigInfoDO qwConfigInfoDO, Model model) {
		model.addAttribute("qwConfigInfoDO", qwConfigInfoDO);
		return "modules/cat/qwConfigInfoDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:qwConfigInfoDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<QwConfigInfoDO> listData(QwConfigInfoDO qwConfigInfoDO, HttpServletRequest request, HttpServletResponse response) {
		qwConfigInfoDO.setPage(new Page<>(request, response));
		Page<QwConfigInfoDO> page = qwConfigInfoDOService.findPage(qwConfigInfoDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:qwConfigInfoDO:view")
	@RequestMapping(value = "form")
	public String form(QwConfigInfoDO qwConfigInfoDO, Model model) {
		model.addAttribute("qwConfigInfoDO", qwConfigInfoDO);
		return "modules/cat/qwConfigInfoDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:qwConfigInfoDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated QwConfigInfoDO qwConfigInfoDO) {
		qwConfigInfoDOService.save(qwConfigInfoDO);
		return renderResult(Global.TRUE, text("保存企微配置详情数据成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:qwConfigInfoDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(QwConfigInfoDO qwConfigInfoDO) {
		qwConfigInfoDOService.delete(qwConfigInfoDO);
		return renderResult(Global.TRUE, text("删除企微配置详情数据成功！"));
	}
	
}