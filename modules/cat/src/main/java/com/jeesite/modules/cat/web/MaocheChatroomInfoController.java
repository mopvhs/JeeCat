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
import com.jeesite.modules.cat.entity.MaocheChatroomInfoDO;
import com.jeesite.modules.cat.service.MaocheChatroomInfoService;

/**
 * 群组消息Controller
 * @author YHQ
 * @version 2023-06-21
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheChatroomInfoDO")
public class MaocheChatroomInfoController extends BaseController {

	@Autowired
	private MaocheChatroomInfoService maocheChatroomInfoDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheChatroomInfoDO get(Long id, boolean isNewRecord) {
//		return maocheChatroomInfoDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheChatroomInfoDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheChatroomInfoDO maocheChatroomInfoDO, Model model) {
		model.addAttribute("maocheChatroomInfoDO", maocheChatroomInfoDO);
		return "modules/cat/maocheChatroomInfoDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheChatroomInfoDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheChatroomInfoDO> listData(MaocheChatroomInfoDO maocheChatroomInfoDO, HttpServletRequest request, HttpServletResponse response) {
		maocheChatroomInfoDO.setPage(new Page<>(request, response));
		Page<MaocheChatroomInfoDO> page = maocheChatroomInfoDOService.findPage(maocheChatroomInfoDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheChatroomInfoDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheChatroomInfoDO maocheChatroomInfoDO, Model model) {
		model.addAttribute("maocheChatroomInfoDO", maocheChatroomInfoDO);
		return "modules/cat/maocheChatroomInfoDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheChatroomInfoDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheChatroomInfoDO maocheChatroomInfoDO) {
		maocheChatroomInfoDOService.save(maocheChatroomInfoDO);
		return renderResult(Global.TRUE, text("保存群组消息成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheChatroomInfoDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheChatroomInfoDO maocheChatroomInfoDO) {
		maocheChatroomInfoDOService.delete(maocheChatroomInfoDO);
		return renderResult(Global.TRUE, text("删除群组消息成功！"));
	}
	
}