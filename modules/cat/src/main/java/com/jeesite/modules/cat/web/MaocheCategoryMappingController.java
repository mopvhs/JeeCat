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
import com.jeesite.modules.cat.entity.MaocheCategoryMappingDO;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;

/**
 * maoche_category_mappingController
 * @author YHQ
 * @version 2023-06-19
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheCategoryMappingDO")
public class MaocheCategoryMappingController extends BaseController {

	@Autowired
	private MaocheCategoryMappingService maocheCategoryMappingDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheCategoryMappingDO get(Long id, boolean isNewRecord) {
//		return maocheCategoryMappingDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheCategoryMappingDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheCategoryMappingDO maocheCategoryMappingDO, Model model) {
		model.addAttribute("maocheCategoryMappingDO", maocheCategoryMappingDO);
		return "modules/cat/maocheCategoryMappingDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheCategoryMappingDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheCategoryMappingDO> listData(MaocheCategoryMappingDO maocheCategoryMappingDO, HttpServletRequest request, HttpServletResponse response) {
		maocheCategoryMappingDO.setPage(new Page<>(request, response));
		Page<MaocheCategoryMappingDO> page = maocheCategoryMappingDOService.findPage(maocheCategoryMappingDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheCategoryMappingDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheCategoryMappingDO maocheCategoryMappingDO, Model model) {
		model.addAttribute("maocheCategoryMappingDO", maocheCategoryMappingDO);
		return "modules/cat/maocheCategoryMappingDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheCategoryMappingDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheCategoryMappingDO maocheCategoryMappingDO) {
		maocheCategoryMappingDOService.save(maocheCategoryMappingDO);
		return renderResult(Global.TRUE, text("保存maoche_category_mapping成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheCategoryMappingDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheCategoryMappingDO maocheCategoryMappingDO) {
		maocheCategoryMappingDOService.delete(maocheCategoryMappingDO);
		return renderResult(Global.TRUE, text("删除maoche_category_mapping成功！"));
	}
	
}