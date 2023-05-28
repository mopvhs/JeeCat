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
import com.jeesite.modules.cat.entity.MaocheCategoryDO;
import com.jeesite.modules.cat.service.MaocheCategoryService;

/**
 * maoche_categoryController
 * @author YHQ
 * @version 2023-05-24
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheCategoryDO")
public class MaocheCategoryController extends BaseController {

	@Autowired
	private MaocheCategoryService maocheCategoryDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheCategoryDO get(Long id, boolean isNewRecord) {
//		return maocheCategoryDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheCategoryDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheCategoryDO maocheCategoryDO, Model model) {
		model.addAttribute("maocheCategoryDO", maocheCategoryDO);
		return "modules/cat/maocheCategoryDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheCategoryDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheCategoryDO> listData(MaocheCategoryDO maocheCategoryDO, HttpServletRequest request, HttpServletResponse response) {
		maocheCategoryDO.setPage(new Page<>(request, response));
		Page<MaocheCategoryDO> page = maocheCategoryDOService.findPage(maocheCategoryDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheCategoryDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheCategoryDO maocheCategoryDO, Model model) {
		model.addAttribute("maocheCategoryDO", maocheCategoryDO);
		return "modules/cat/maocheCategoryDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheCategoryDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheCategoryDO maocheCategoryDO) {
		maocheCategoryDOService.save(maocheCategoryDO);
		return renderResult(Global.TRUE, text("保存maoche_category成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheCategoryDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheCategoryDO maocheCategoryDO) {
		maocheCategoryDOService.delete(maocheCategoryDO);
		return renderResult(Global.TRUE, text("删除maoche_category成功！"));
	}
	
}