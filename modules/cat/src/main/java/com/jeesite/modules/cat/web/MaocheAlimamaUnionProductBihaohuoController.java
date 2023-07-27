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
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductBihaohuoDO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductBihaohuoService;

/**
 * maoche_alimama_union_product_bihaohuoController
 * @author YHQ
 * @version 2023-07-22
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheAlimamaUnionProductBihaohuoDO")
public class MaocheAlimamaUnionProductBihaohuoController extends BaseController {

	@Autowired
	private MaocheAlimamaUnionProductBihaohuoService maocheAlimamaUnionProductBihaohuoDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheAlimamaUnionProductBihaohuoDO get(Long id, boolean isNewRecord) {
//		return maocheAlimamaUnionProductBihaohuoDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductBihaohuoDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO, Model model) {
		model.addAttribute("maocheAlimamaUnionProductBihaohuoDO", maocheAlimamaUnionProductBihaohuoDO);
		return "modules/cat/maocheAlimamaUnionProductBihaohuoDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductBihaohuoDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheAlimamaUnionProductBihaohuoDO> listData(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO, HttpServletRequest request, HttpServletResponse response) {
		maocheAlimamaUnionProductBihaohuoDO.setPage(new Page<>(request, response));
		Page<MaocheAlimamaUnionProductBihaohuoDO> page = maocheAlimamaUnionProductBihaohuoDOService.findPage(maocheAlimamaUnionProductBihaohuoDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductBihaohuoDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO, Model model) {
		model.addAttribute("maocheAlimamaUnionProductBihaohuoDO", maocheAlimamaUnionProductBihaohuoDO);
		return "modules/cat/maocheAlimamaUnionProductBihaohuoDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductBihaohuoDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO) {
		maocheAlimamaUnionProductBihaohuoDOService.save(maocheAlimamaUnionProductBihaohuoDO);
		return renderResult(Global.TRUE, text("保存maoche_alimama_union_product_bihaohuo成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductBihaohuoDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheAlimamaUnionProductBihaohuoDO maocheAlimamaUnionProductBihaohuoDO) {
		maocheAlimamaUnionProductBihaohuoDOService.delete(maocheAlimamaUnionProductBihaohuoDO);
		return renderResult(Global.TRUE, text("删除maoche_alimama_union_product_bihaohuo成功！"));
	}
	
}