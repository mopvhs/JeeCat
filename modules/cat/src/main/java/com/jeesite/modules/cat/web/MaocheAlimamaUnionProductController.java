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
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;

/**
 * maoche_alimama_union_productController
 * @author YHQ
 * @version 2023-05-05
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheAlimamaUnionProductDO")
public class MaocheAlimamaUnionProductController extends BaseController {

	@Autowired
	private MaocheAlimamaUnionProductService maocheAlimamaUnionProductDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheAlimamaUnionProductDO get(Long id, boolean isNewRecord) {
//		return maocheAlimamaUnionProductDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO, Model model) {
		model.addAttribute("maocheAlimamaUnionProductDO", maocheAlimamaUnionProductDO);
		return "modules/cat/maocheAlimamaUnionProductDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheAlimamaUnionProductDO> listData(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO, HttpServletRequest request, HttpServletResponse response) {
		maocheAlimamaUnionProductDO.setPage(new Page<>(request, response));
		Page<MaocheAlimamaUnionProductDO> page = maocheAlimamaUnionProductDOService.findPage(maocheAlimamaUnionProductDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO, Model model) {
		model.addAttribute("maocheAlimamaUnionProductDO", maocheAlimamaUnionProductDO);
		return "modules/cat/maocheAlimamaUnionProductDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO) {
		maocheAlimamaUnionProductDOService.save(maocheAlimamaUnionProductDO);
		return renderResult(Global.TRUE, text("保存maoche_alimama_union_product成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheAlimamaUnionProductDO maocheAlimamaUnionProductDO) {
		maocheAlimamaUnionProductDOService.delete(maocheAlimamaUnionProductDO);
		return renderResult(Global.TRUE, text("删除maoche_alimama_union_product成功！"));
	}
	
}