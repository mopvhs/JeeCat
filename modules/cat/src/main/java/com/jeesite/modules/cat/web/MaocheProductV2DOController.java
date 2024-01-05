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
import com.jeesite.modules.cat.entity.MaocheProductV2DO;
import com.jeesite.modules.cat.service.MaocheProductV2Service;

/**
 * maoche_product_v2Controller
 * @author YHQ
 * @version 2024-01-02
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheProductV2DO")
public class MaocheProductV2DOController extends BaseController {

	@Autowired
	private MaocheProductV2Service maocheProductV2DOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheProductV2DO get(Long id, boolean isNewRecord) {
//		return maocheProductV2DOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheProductV2DO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheProductV2DO maocheProductV2DO, Model model) {
		model.addAttribute("maocheProductV2DO", maocheProductV2DO);
		return "modules/cat/maocheProductV2DOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheProductV2DO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheProductV2DO> listData(MaocheProductV2DO maocheProductV2DO, HttpServletRequest request, HttpServletResponse response) {
		maocheProductV2DO.setPage(new Page<>(request, response));
		Page<MaocheProductV2DO> page = maocheProductV2DOService.findPage(maocheProductV2DO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheProductV2DO:view")
	@RequestMapping(value = "form")
	public String form(MaocheProductV2DO maocheProductV2DO, Model model) {
		model.addAttribute("maocheProductV2DO", maocheProductV2DO);
		return "modules/cat/maocheProductV2DOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheProductV2DO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheProductV2DO maocheProductV2DO) {
		maocheProductV2DOService.save(maocheProductV2DO);
		return renderResult(Global.TRUE, text("保存maoche_product_v2成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheProductV2DO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheProductV2DO maocheProductV2DO) {
		maocheProductV2DOService.delete(maocheProductV2DO);
		return renderResult(Global.TRUE, text("删除maoche_product_v2成功！"));
	}
	
}