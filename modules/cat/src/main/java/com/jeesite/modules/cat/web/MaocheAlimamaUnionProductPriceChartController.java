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
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductPriceChartDO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductPriceChartService;

/**
 * maoche_alimama_union_product_price_chartController
 * @author YHQ
 * @version 2023-07-15
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheAlimamaUnionProductPriceChartDO")
public class MaocheAlimamaUnionProductPriceChartController extends BaseController {

	@Autowired
	private MaocheAlimamaUnionProductPriceChartService maocheAlimamaUnionProductPriceChartDOService;
//
//	/**
//	 * 获取数据
//	 */
//	@ModelAttribute
//	public MaocheAlimamaUnionProductPriceChartDO get(Long id, boolean isNewRecord) {
//		return maocheAlimamaUnionProductPriceChartDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductPriceChartDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO, Model model) {
		model.addAttribute("maocheAlimamaUnionProductPriceChartDO", maocheAlimamaUnionProductPriceChartDO);
		return "modules/cat/maocheAlimamaUnionProductPriceChartDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductPriceChartDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheAlimamaUnionProductPriceChartDO> listData(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO, HttpServletRequest request, HttpServletResponse response) {
		maocheAlimamaUnionProductPriceChartDO.setPage(new Page<>(request, response));
		Page<MaocheAlimamaUnionProductPriceChartDO> page = maocheAlimamaUnionProductPriceChartDOService.findPage(maocheAlimamaUnionProductPriceChartDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductPriceChartDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO, Model model) {
		model.addAttribute("maocheAlimamaUnionProductPriceChartDO", maocheAlimamaUnionProductPriceChartDO);
		return "modules/cat/maocheAlimamaUnionProductPriceChartDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductPriceChartDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO) {
		maocheAlimamaUnionProductPriceChartDOService.save(maocheAlimamaUnionProductPriceChartDO);
		return renderResult(Global.TRUE, text("保存maoche_alimama_union_product_price_chart成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductPriceChartDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheAlimamaUnionProductPriceChartDO maocheAlimamaUnionProductPriceChartDO) {
		maocheAlimamaUnionProductPriceChartDOService.delete(maocheAlimamaUnionProductPriceChartDO);
		return renderResult(Global.TRUE, text("删除maoche_alimama_union_product_price_chart成功！"));
	}
	
}