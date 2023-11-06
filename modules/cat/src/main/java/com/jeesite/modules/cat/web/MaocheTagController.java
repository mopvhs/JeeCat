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
import com.jeesite.modules.cat.entity.MaocheTagDO;
import com.jeesite.modules.cat.service.MaocheTagService;

/**
 * maoche_tagController
 * @author YHQ
 * @version 2023-10-29
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheTagDO")
public class MaocheTagController extends BaseController {

	@Autowired
	private MaocheTagService maocheTagService;
	
//	/**
//	 * 获取数据
//	 */
//	@ModelAttribute
//	public MaocheTagDO get(Long id, boolean isNewRecord) {
//		return maocheTagDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheTagDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheTagDO maocheTagDO, Model model) {
		model.addAttribute("maocheTagDO", maocheTagDO);
		return "modules/cat/maocheTagDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheTagDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheTagDO> listData(MaocheTagDO maocheTagDO, HttpServletRequest request, HttpServletResponse response) {
		maocheTagDO.setPage(new Page<>(request, response));
		Page<MaocheTagDO> page = maocheTagService.findPage(maocheTagDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheTagDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheTagDO maocheTagDO, Model model) {
		model.addAttribute("maocheTagDO", maocheTagDO);
		return "modules/cat/maocheTagDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheTagDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheTagDO maocheTagDO) {
		maocheTagService.save(maocheTagDO);
		return renderResult(Global.TRUE, text("保存maoche_tag成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheTagDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheTagDO maocheTagDO) {
		maocheTagService.delete(maocheTagDO);
		return renderResult(Global.TRUE, text("删除maoche_tag成功！"));
	}
	
}