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
import com.jeesite.modules.cat.entity.MaocheSubscribeDO;
import com.jeesite.modules.cat.service.MaocheSubscribeService;

/**
 * 订阅表Controller
 * @author YhQ
 * @version 2024-11-30
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheSubscribeDO")
public class MaocheSubscribeController extends BaseController {

	@Autowired
	private MaocheSubscribeService maocheSubscribeDOService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public MaocheSubscribeDO get(String id, boolean isNewRecord) {
		return maocheSubscribeDOService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheSubscribeDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheSubscribeDO maocheSubscribeDO, Model model) {
		model.addAttribute("maocheSubscribeDO", maocheSubscribeDO);
		return "modules/cat/maocheSubscribeDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheSubscribeDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheSubscribeDO> listData(MaocheSubscribeDO maocheSubscribeDO, HttpServletRequest request, HttpServletResponse response) {
		maocheSubscribeDO.setPage(new Page<>(request, response));
		Page<MaocheSubscribeDO> page = maocheSubscribeDOService.findPage(maocheSubscribeDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheSubscribeDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheSubscribeDO maocheSubscribeDO, Model model) {
		model.addAttribute("maocheSubscribeDO", maocheSubscribeDO);
		return "modules/cat/maocheSubscribeDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheSubscribeDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheSubscribeDO maocheSubscribeDO) {
		maocheSubscribeDOService.save(maocheSubscribeDO);
		return renderResult(Global.TRUE, text("保存订阅表成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheSubscribeDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheSubscribeDO maocheSubscribeDO) {
		maocheSubscribeDOService.delete(maocheSubscribeDO);
		return renderResult(Global.TRUE, text("删除订阅表成功！"));
	}
	
}