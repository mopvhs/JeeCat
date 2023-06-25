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
import com.jeesite.modules.cat.entity.QwChatroomInfoDO;
import com.jeesite.modules.cat.service.QwChatroomInfoService;

/**
 * 群组消息Controller
 * @author YHQ
 * @version 2023-06-23
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/qwChatroomInfoDO")
public class QwChatroomInfoController extends BaseController {

	@Autowired
	private QwChatroomInfoService qwChatroomInfoDOService;
	
//	/**
//	 * 获取数据
//	 */
//	@ModelAttribute
//	public QwChatroomInfoDO get(Long id, boolean isNewRecord) {
//		return qwChatroomInfoDOService.get(id, isNewRecord);
//	}
//
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:qwChatroomInfoDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(QwChatroomInfoDO qwChatroomInfoDO, Model model) {
		model.addAttribute("qwChatroomInfoDO", qwChatroomInfoDO);
		return "modules/cat/qwChatroomInfoDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:qwChatroomInfoDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<QwChatroomInfoDO> listData(QwChatroomInfoDO qwChatroomInfoDO, HttpServletRequest request, HttpServletResponse response) {
		qwChatroomInfoDO.setPage(new Page<>(request, response));
		Page<QwChatroomInfoDO> page = qwChatroomInfoDOService.findPage(qwChatroomInfoDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:qwChatroomInfoDO:view")
	@RequestMapping(value = "form")
	public String form(QwChatroomInfoDO qwChatroomInfoDO, Model model) {
		model.addAttribute("qwChatroomInfoDO", qwChatroomInfoDO);
		return "modules/cat/qwChatroomInfoDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:qwChatroomInfoDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated QwChatroomInfoDO qwChatroomInfoDO) {
		qwChatroomInfoDOService.save(qwChatroomInfoDO);
		return renderResult(Global.TRUE, text("保存群组消息成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:qwChatroomInfoDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(QwChatroomInfoDO qwChatroomInfoDO) {
		qwChatroomInfoDOService.delete(qwChatroomInfoDO);
		return renderResult(Global.TRUE, text("删除群组消息成功！"));
	}
	
}