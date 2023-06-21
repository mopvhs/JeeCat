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
import com.jeesite.modules.cat.entity.MaocheDataokeProductDO;
import com.jeesite.modules.cat.service.MaocheDataokeProductService;

/**
 * maoche_dataoke_productController
 * @author YHQ
 * @version 2023-06-04
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheDataokeProductDO")
public class MaocheDataokeProductDOController extends BaseController {

	@Autowired
	private MaocheDataokeProductService maocheDataokeProductDOService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public MaocheDataokeProductDO get(String id, boolean isNewRecord) {
		return maocheDataokeProductDOService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheDataokeProductDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheDataokeProductDO maocheDataokeProductDO, Model model) {
		model.addAttribute("maocheDataokeProductDO", maocheDataokeProductDO);
		return "modules/cat/maocheDataokeProductDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheDataokeProductDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheDataokeProductDO> listData(MaocheDataokeProductDO maocheDataokeProductDO, HttpServletRequest request, HttpServletResponse response) {
		maocheDataokeProductDO.setPage(new Page<>(request, response));
		Page<MaocheDataokeProductDO> page = maocheDataokeProductDOService.findPage(maocheDataokeProductDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheDataokeProductDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheDataokeProductDO maocheDataokeProductDO, Model model) {
		model.addAttribute("maocheDataokeProductDO", maocheDataokeProductDO);
		return "modules/cat/maocheDataokeProductDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheDataokeProductDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheDataokeProductDO maocheDataokeProductDO) {
		maocheDataokeProductDOService.save(maocheDataokeProductDO);
		return renderResult(Global.TRUE, text("保存maoche_dataoke_product成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheDataokeProductDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheDataokeProductDO maocheDataokeProductDO) {
		maocheDataokeProductDOService.delete(maocheDataokeProductDO);
		return renderResult(Global.TRUE, text("删除maoche_dataoke_product成功！"));
	}
	
}