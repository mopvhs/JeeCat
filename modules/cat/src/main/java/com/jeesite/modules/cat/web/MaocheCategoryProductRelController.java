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
import com.jeesite.modules.cat.entity.MaocheCategoryProductRelDO;
import com.jeesite.modules.cat.service.MaocheCategoryProductRelService;

/**
 * maoche_category_product_relController
 * @author YHQ
 * @version 2023-05-24
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheCategoryProductRelDO")
public class MaocheCategoryProductRelController extends BaseController {

	@Autowired
	private MaocheCategoryProductRelService maocheCategoryProductRelDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheCategoryProductRelDO get(Long id, boolean isNewRecord) {
//		return maocheCategoryProductRelDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheCategoryProductRelDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheCategoryProductRelDO maocheCategoryProductRelDO, Model model) {
		model.addAttribute("maocheCategoryProductRelDO", maocheCategoryProductRelDO);
		return "modules/cat/maocheCategoryProductRelDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheCategoryProductRelDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheCategoryProductRelDO> listData(MaocheCategoryProductRelDO maocheCategoryProductRelDO, HttpServletRequest request, HttpServletResponse response) {
		maocheCategoryProductRelDO.setPage(new Page<>(request, response));
		Page<MaocheCategoryProductRelDO> page = maocheCategoryProductRelDOService.findPage(maocheCategoryProductRelDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheCategoryProductRelDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheCategoryProductRelDO maocheCategoryProductRelDO, Model model) {
		model.addAttribute("maocheCategoryProductRelDO", maocheCategoryProductRelDO);
		return "modules/cat/maocheCategoryProductRelDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheCategoryProductRelDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheCategoryProductRelDO maocheCategoryProductRelDO) {
		maocheCategoryProductRelDOService.save(maocheCategoryProductRelDO);
		return renderResult(Global.TRUE, text("保存maoche_category_product_rel成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheCategoryProductRelDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheCategoryProductRelDO maocheCategoryProductRelDO) {
		maocheCategoryProductRelDOService.delete(maocheCategoryProductRelDO);
		return renderResult(Global.TRUE, text("删除maoche_category_product_rel成功！"));
	}
	
}