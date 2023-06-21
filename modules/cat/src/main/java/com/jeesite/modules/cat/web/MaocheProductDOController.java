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
import com.jeesite.modules.cat.entity.MaocheProductDO;
import com.jeesite.modules.cat.service.MaocheProductService;

/**
 * maoche_productController
 * @author YHQ
 * @version 2023-06-16
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheProductDO")
public class MaocheProductDOController extends BaseController {

	@Autowired
	private MaocheProductService maocheProductDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheProductDO get(Long id, boolean isNewRecord) {
//		return maocheProductDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheProductDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheProductDO maocheProductDO, Model model) {
		model.addAttribute("maocheProductDO", maocheProductDO);
		return "modules/cat/maocheProductDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheProductDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheProductDO> listData(MaocheProductDO maocheProductDO, HttpServletRequest request, HttpServletResponse response) {
		maocheProductDO.setPage(new Page<>(request, response));
		Page<MaocheProductDO> page = maocheProductDOService.findPage(maocheProductDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheProductDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheProductDO maocheProductDO, Model model) {
		model.addAttribute("maocheProductDO", maocheProductDO);
		return "modules/cat/maocheProductDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheProductDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheProductDO maocheProductDO) {
		maocheProductDOService.save(maocheProductDO);
		return renderResult(Global.TRUE, text("保存maoche_product成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheProductDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheProductDO maocheProductDO) {
		maocheProductDOService.delete(maocheProductDO);
		return renderResult(Global.TRUE, text("删除maoche_product成功！"));
	}
	
}