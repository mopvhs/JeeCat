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
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionGoodPriceDO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionGoodPriceService;

/**
 * maoche_alimama_union_good_priceController
 * @author YHQ
 * @version 2023-05-14
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheAlimamaUnionGoodPriceDO")
public class MaocheAlimamaUnionGoodPriceController extends BaseController {

	@Autowired
	private MaocheAlimamaUnionGoodPriceService maocheAlimamaUnionGoodPriceDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheAlimamaUnionGoodPriceDO get(Long id, boolean isNewRecord) {
//		return maocheAlimamaUnionGoodPriceDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionGoodPriceDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO, Model model) {
		model.addAttribute("maocheAlimamaUnionGoodPriceDO", maocheAlimamaUnionGoodPriceDO);
		return "modules/cat/maocheAlimamaUnionGoodPriceDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionGoodPriceDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheAlimamaUnionGoodPriceDO> listData(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO, HttpServletRequest request, HttpServletResponse response) {
		maocheAlimamaUnionGoodPriceDO.setPage(new Page<>(request, response));
		Page<MaocheAlimamaUnionGoodPriceDO> page = maocheAlimamaUnionGoodPriceDOService.findPage(maocheAlimamaUnionGoodPriceDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionGoodPriceDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO, Model model) {
		model.addAttribute("maocheAlimamaUnionGoodPriceDO", maocheAlimamaUnionGoodPriceDO);
		return "modules/cat/maocheAlimamaUnionGoodPriceDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionGoodPriceDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO) {
		maocheAlimamaUnionGoodPriceDOService.save(maocheAlimamaUnionGoodPriceDO);
		return renderResult(Global.TRUE, text("保存maoche_alimama_union_good_price成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionGoodPriceDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheAlimamaUnionGoodPriceDO maocheAlimamaUnionGoodPriceDO) {
		maocheAlimamaUnionGoodPriceDOService.delete(maocheAlimamaUnionGoodPriceDO);
		return renderResult(Global.TRUE, text("删除maoche_alimama_union_good_price成功！"));
	}
	
}