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
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductDetailService;

/**
 * maoche_alimama_union_product_detailController
 * @author YHQ
 * @version 2023-05-28
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheAlimamaUnionProductDetailDO")
public class MaocheAlimamaUnionProductDetailController extends BaseController {

	@Autowired
	private MaocheAlimamaUnionProductDetailService maocheAlimamaUnionProductDetailDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheAlimamaUnionProductDetailDO get(Long id, boolean isNewRecord) {
//		return maocheAlimamaUnionProductDetailDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDetailDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO, Model model) {
		model.addAttribute("maocheAlimamaUnionProductDetailDO", maocheAlimamaUnionProductDetailDO);
		return "modules/cat/maocheAlimamaUnionProductDetailDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDetailDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheAlimamaUnionProductDetailDO> listData(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO, HttpServletRequest request, HttpServletResponse response) {
		maocheAlimamaUnionProductDetailDO.setPage(new Page<>(request, response));
		Page<MaocheAlimamaUnionProductDetailDO> page = maocheAlimamaUnionProductDetailDOService.findPage(maocheAlimamaUnionProductDetailDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDetailDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO, Model model) {
		model.addAttribute("maocheAlimamaUnionProductDetailDO", maocheAlimamaUnionProductDetailDO);
		return "modules/cat/maocheAlimamaUnionProductDetailDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDetailDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO) {
		maocheAlimamaUnionProductDetailDOService.save(maocheAlimamaUnionProductDetailDO);
		return renderResult(Global.TRUE, text("保存maoche_alimama_union_product_detail成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionProductDetailDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheAlimamaUnionProductDetailDO maocheAlimamaUnionProductDetailDO) {
		maocheAlimamaUnionProductDetailDOService.delete(maocheAlimamaUnionProductDetailDO);
		return renderResult(Global.TRUE, text("删除maoche_alimama_union_product_detail成功！"));
	}
	
}